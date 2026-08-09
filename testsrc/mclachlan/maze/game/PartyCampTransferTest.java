/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.game;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.balance.HeadlessUi;
import mclachlan.maze.game.event.ForcePartySplitEvent;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.CharacterSelection;
import mclachlan.maze.stat.HighestModifierSelection;
import mclachlan.maze.stat.PartyCampManager;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerCharacterSelection;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for party camp transfers on a headless {@link Maze}.
 */
public class PartyCampTransferTest extends MazeTestSupport
{
	private InMemoryLoader loader;
	private Database db;
	private Maze maze;

	@BeforeEach
	void setUp() throws Exception
	{
		PartyCampManager.resetForTesting();
		loader = new InMemoryLoader();
		MazeTexture campTexture = new MazeTexture();
		campTexture.setName(PartyCampManager.PARTY_CAMP_TEXTURE);
		loader.mazeTextures.put(PartyCampManager.PARTY_CAMP_TEXTURE, campTexture);
		db = TestData.buildDatabase(loader);
		maze = HeadlessMaze.boot(db);
		maze.setZoneAndTileForTesting("Woods", new Point(3, 4));

		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Leader"));
		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Scout"));
		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Guard"));
	}

	@AfterEach
	void tearDownLocal()
	{
		PartyCampManager.resetForTesting();
		loader = null;
		db = null;
		maze = null;
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void transferToCampAndBack()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		maze.transferPlayerCharacterToCamp(scout);

		assertEquals(2, maze.getParty().size());
		assertTrue(mgr.hasCamp());
		assertEquals(List.of("Scout"), mgr.getCamp().getCharacterNames());

		assertTrue(maze.transferPlayerCharacterFromCamp(scout));
		assertEquals(3, maze.getParty().size());
		assertFalse(mgr.hasCamp());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void cannotEmptyFieldPartyIntoCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);
		PlayerCharacter guard = maze.getParty().getPlayerCharacter(2);

		assertTrue(maze.transferPlayerCharacterToCamp(scout));
		assertTrue(maze.transferPlayerCharacterToCamp(guard));
		assertFalse(maze.transferPlayerCharacterToCamp(leader));

		assertEquals(1, maze.getParty().size());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals(2, mgr.getCamp().getCharacterNames().size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void cannotCreateSecondCampElsewhere()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		PartyCamp elsewhere = mgr.createCamp("Elsewhere", new Point(0, 0));
		mgr.addCharacter(elsewhere, "Leftover");

		assertFalse(maze.transferPlayerCharacterToCamp(scout));
		assertEquals(3, maze.getParty().size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitCreatesSecondCampWhileVoluntaryCampRemains()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);
		PlayerCharacter guard = maze.getParty().getPlayerCharacter(2);

		PartyCamp voluntaryCamp = mgr.createCamp("Woods", new Point(1, 1));
		mgr.addCharacter(voluntaryCamp, "Leftover");

		maze.setZoneAndTileForTesting("Caves", new Point(7, 8));
		new ForcePartySplitEvent(List.of(leader)).resolve();

		assertEquals(1, maze.getParty().size());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals(2, mgr.getCamps().size());

		PartyCamp forcedCamp = mgr.findCampAt("Caves", new Point(7, 8));
		assertNotNull(forcedCamp);
		assertEquals(List.of("Scout", "Guard"), forcedCamp.getCharacterNames());
		assertEquals(List.of("Leftover"), voluntaryCamp.getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitWithNoPriorCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);

		new ForcePartySplitEvent(List.of(leader)).resolve();

		assertEquals(1, maze.getParty().size());
		assertEquals(1, mgr.getCamps().size());
		PartyCamp camp = mgr.findCampAt("Woods", new Point(3, 4));
		assertNotNull(camp);
		assertEquals(List.of("Scout", "Guard"), camp.getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitOnExistingCampTileAppendsToThatCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		PartyCamp existing = mgr.createCamp("Woods", new Point(3, 4));
		mgr.addCharacter(existing, "AlreadyHere");

		new ForcePartySplitEvent(List.of(leader)).resolve();

		assertEquals(1, maze.getParty().size());
		assertEquals(1, mgr.getCamps().size());
		assertEquals(List.of("AlreadyHere", "Scout", "Guard"), existing.getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void emptyingOneCampLeavesOther()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		maze.setZoneAndTileForTesting("Woods", new Point(1, 1));
		PlayerCharacter alpha = TestData.newLevel1Pc("Alpha");
		maze.addPlayerCharacterToParty(alpha);
		assertTrue(maze.transferPlayerCharacterToCamp(alpha));

		PartyCamp campA = mgr.findCampAt("Woods", new Point(1, 1));
		PartyCamp campB = mgr.createCamp("Caves", new Point(2, 2));
		mgr.addCharacter(campB, "Beta");

		assertTrue(maze.transferPlayerCharacterFromCamp(alpha, campA));

		assertNull(mgr.findCampAt("Woods", new Point(1, 1)));
		assertTrue(mgr.isCampAt("Caves", new Point(2, 2)));
		assertEquals(List.of("Beta"), campB.getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitViaCharacterSelectionAutomatic()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);

		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 10);
		leader.applyPermanentStatModifier(sm);

		CharacterSelection selection = new CharacterSelection(
			List.of(new HighestModifierSelection(Stats.Modifier.BRAWN)),
			List.of());

		resolveSplitEvent(new ForcePartySplitEvent(selection));

		assertEquals(1, maze.getParty().size());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals(1, mgr.getCamps().size());
		assertEquals(List.of("Scout", "Guard"), mgr.getMostRecentCamp().getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitViaCharacterSelectionPlayerPicks() throws Exception
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		maze.initUi(new AutoPickUi(new ArrayDeque<>(List.of(leader, scout))));

		CharacterSelection selection = new CharacterSelection(
			List.of(new PlayerCharacterSelection(), new PlayerCharacterSelection()),
			List.of());

		resolveSplitEvent(new ForcePartySplitEvent(selection));

		assertEquals(2, maze.getParty().size());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals("Scout", maze.getParty().getPlayerCharacter(1).getName());
		assertEquals(1, mgr.getCamps().size());
		assertEquals(List.of("Guard"), mgr.getMostRecentCamp().getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcedSplitViaCharacterSelectionBlocksUntilWhoComplete() throws Exception
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);

		maze.initUi(new AutoPickUi(new ArrayDeque<>(List.of(leader))));

		CharacterSelection selection = new CharacterSelection(
			List.of(new PlayerCharacterSelection()),
			List.of());

		AtomicBoolean markerRan = new AtomicBoolean(false);
		List<MazeEvent> script = new ArrayList<>();
		List<MazeEvent> splitEvents = new ForcePartySplitEvent(selection).resolve();
		if (splitEvents != null)
		{
			script.addAll(splitEvents);
		}
		script.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				markerRan.set(true);
				assertEquals(1, maze.getParty().size(),
					"split must finish before the next script event runs");
				assertEquals(1, mgr.getCamps().size());
				return null;
			}
		});

		maze.resolveEventsForTesting(script);

		assertTrue(markerRan.get());
		assertTrue(maze.isEventQueueEmptyForTesting(),
			"Who selection must not queue continuation events");
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals(List.of("Scout", "Guard"), mgr.getMostRecentCamp().getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void excludedCharacterWhoPickShowsMessageAndStaysOpen() throws Exception
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 10);
		leader.applyPermanentStatModifier(sm);

		loader.textRepository.putHotString(
			"event",
			"msg.character.cannot.be.chosen",
			"%s can't be chosen");

		RecordingAutoPickUi ui = new RecordingAutoPickUi(
			new ArrayDeque<>(List.of(leader, scout)));
		maze.initUi(ui);

		CharacterSelection selection = new CharacterSelection(
			List.of(new PlayerCharacterSelection()),
			List.of(new HighestModifierSelection(Stats.Modifier.BRAWN)));

		resolveSplitEvent(new ForcePartySplitEvent(selection));

		assertEquals(List.of("Leader can't be chosen"), ui.getMessages());
		assertEquals(1, maze.getParty().size());
		assertEquals("Scout", maze.getParty().getPlayerCharacter(0).getName());
		assertEquals(1, mgr.getCamps().size());
		assertEquals(List.of("Leader", "Guard"), mgr.getMostRecentCamp().getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void emptyingForcedSplitCampRemovesMapObject()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);
		PlayerCharacter guard = maze.getParty().getPlayerCharacter(2);

		PartyCamp voluntaryCamp = mgr.createCamp("Woods", new Point(1, 1));
		mgr.addCharacter(voluntaryCamp, "Leftover");

		Point forcedTile = new Point(7, 8);
		maze.setZoneAndTileForTesting("Caves", forcedTile);
		new ForcePartySplitEvent(List.of(leader)).resolve();

		PartyCamp forcedCamp = mgr.findCampAt("Caves", forcedTile);
		assertNotNull(forcedCamp);
		assertTrue(hasCampObjectOnTile(maze.getCurrentZone(), forcedTile));

		assertTrue(maze.transferPlayerCharacterFromCamp(scout, forcedCamp));
		assertTrue(hasCampObjectOnTile(maze.getCurrentZone(), forcedTile));

		assertTrue(maze.transferPlayerCharacterFromCamp(guard, forcedCamp));
		assertNull(mgr.findCampAt("Caves", forcedTile));
		assertFalse(hasCampObjectOnTile(maze.getCurrentZone(), forcedTile));
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));
	}

	/*-------------------------------------------------------------------------*/
	private boolean hasCampObjectOnTile(Zone zone, Point tile)
	{
		if (zone == null || zone.getMap() == null)
		{
			return false;
		}

		int tileIndex = zone.getTileIndex(tile);
		for (EngineObject obj : zone.getMap().getObjects(tileIndex))
		{
			if (obj.getName().startsWith(PartyCampManager.ENGINE_OBJECT_NAME_PREFIX))
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void resolveSplitEvent(ForcePartySplitEvent event)
	{
		List<MazeEvent> pending = event.resolve();
		if (pending != null)
		{
			maze.resolveEventsForTesting(pending);
		}
		for (int i = 0; i < 8 && !maze.isEventQueueEmptyForTesting(); i++)
		{
			maze.resolveQueuedEventsForTesting();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class RecordingAutoPickUi extends HeadlessUi
	{
		private final Deque<PlayerCharacter> picks;
		private final List<String> messages = new ArrayList<>();

		RecordingAutoPickUi(Deque<PlayerCharacter> picks)
		{
			this.picks = picks;
		}

		List<String> getMessages()
		{
			return messages;
		}

		@Override
		public void addMessage(String msg, boolean shouldJournal)
		{
			messages.add(msg);
		}

		@Override
		public void chooseACharacter(ChooseCharacterCallback callback)
		{
			PlayerCharacter pc = picks.removeFirst();
			int index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);
			callback.characterChosen(pc, index);
			callback.afterCharacterChosen();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class AutoPickUi extends HeadlessUi
	{
		private final Deque<PlayerCharacter> picks;

		AutoPickUi(Deque<PlayerCharacter> picks)
		{
			this.picks = picks;
		}

		@Override
		public void chooseACharacter(ChooseCharacterCallback callback)
		{
			PlayerCharacter pc = picks.removeFirst();
			int index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);
			callback.characterChosen(pc, index);
			callback.afterCharacterChosen();
		}
	}
}

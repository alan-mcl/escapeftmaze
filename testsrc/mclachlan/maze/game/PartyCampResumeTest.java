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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.event.ResumeFromPartyCampEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic tests for resuming play at a party camp after a field-party wipe.
 */
public class PartyCampResumeTest extends MazeTestSupport
{
	private Database db;
	private Maze maze;

	@BeforeEach
	void setUp() throws Exception
	{
		PartyCampManager.resetForTesting();
		InMemoryLoader loader = new InMemoryLoader();
		MazeTexture campTexture = new MazeTexture();
		campTexture.setName(PartyCampManager.PARTY_CAMP_TEXTURE);
		loader.mazeTextures.put(PartyCampManager.PARTY_CAMP_TEXTURE, campTexture);
		db = TestData.buildDatabase(loader);
		maze = HeadlessMaze.boot(db);
		maze.setZoneAndTileForTesting("Woods", new Point(5, 6));

		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Leader"));
		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Scout"));
		maze.addPlayerCharacterToParty(TestData.newLevel1Pc("Guard"));
	}

	@AfterEach
	void tearDownLocal()
	{
		PartyCampManager.resetForTesting();
		db = null;
		maze = null;
	}

	/*-------------------------------------------------------------------------*/
	private void killFieldParty(Maze maze)
	{
		for (PlayerCharacter pc : maze.getParty().getPlayerCharacters())
		{
			pc.getStats().getHitPoints().setCurrent(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	private Item testItem(String name)
	{
		ItemTemplate template = new ItemTemplate();
		template.setName(name);
		template.setMaxItemsPerStack(10);
		return new Item(template);
	}

	/*-------------------------------------------------------------------------*/
	private void completeResumeAfterFade(int gold, int supplies)
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PartyCamp camp = mgr.getMostRecentCamp();
		assertNotNull(camp);
		List<PlayerCharacter> campPcs = mgr.getCampPlayerCharacters(maze, camp);
		maze.completePartyResumeFromCamp(
			camp, campPcs, gold, supplies, maze.getParty().getFormation(),
			camp.getZone(), camp.getTile());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void fadeResolveKeepsWaitDelayAndDoesNotRebuildParty()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);
		maze.setZoneAndTileForTesting("Woods", new Point(1, 1));
		assertTrue(maze.transferPlayerCharacterToCamp(scout));
		maze.setZoneAndTileForTesting("Woods", new Point(5, 6));
		killFieldParty(maze);

		ResumeFromPartyCampEvent fade =
			(ResumeFromPartyCampEvent) maze.partyResumesFromMostRecentCamp().get(0);

		assertEquals(MazeEvent.Delay.WAIT_ON_CLICK, fade.getDelay());
		assertNull(fade.resolve());
		// resolveEvent reads delay AFTER resolve(); must still wait on fade
		assertEquals(MazeEvent.Delay.WAIT_ON_CLICK, fade.getDelay());

		assertEquals(0, maze.getParty().numAlive());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void wipeWithCampElsewhereResumesAtCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);
		PlayerCharacter guard = maze.getParty().getPlayerCharacter(2);

		maze.getParty().setGold(500);
		maze.getParty().setSupplies(42);

		maze.setZoneAndTileForTesting("Woods", new Point(1, 1));
		assertTrue(maze.transferPlayerCharacterToCamp(scout));
		assertTrue(maze.transferPlayerCharacterToCamp(guard));

		maze.setZoneAndTileForTesting("Woods", new Point(5, 6));
		killFieldParty(maze);

		assertTrue(maze.wouldResumeFromCampOnWipe());

		ResumeFromPartyCampEvent fade =
			(ResumeFromPartyCampEvent) maze.partyResumesFromMostRecentCamp().get(0);
		assertFalse(fade.shouldCheckPartyStatus());

		assertEquals(1, maze.getParty().size());
		assertEquals("Leader", maze.getParty().getPlayerCharacter(0).getName());
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));

		assertNull(fade.resolve());
		assertEquals(MazeEvent.Delay.WAIT_ON_CLICK, fade.getDelay());
		assertFalse(maze.checkPartyStatus());
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));

		completeResumeAfterFade(500, 42);

		assertEquals(2, maze.getParty().size());
		assertEquals(List.of("Scout", "Guard"), maze.getParty().getPartyNames());
		assertEquals(500, maze.getParty().getGold());
		assertEquals(42, maze.getParty().getSupplies());
		assertNull(mgr.findCampAt("Woods", new Point(1, 1)));
		assertTrue(mgr.getCamps().isEmpty());
		assertTrue(maze.checkPartyStatus());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void wipeWithTwoCampsResumesAtMostRecent()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		PlayerCharacter alpha = TestData.newLevel1Pc("Alpha");
		PlayerCharacter beta = TestData.newLevel1Pc("Beta");
		maze.getPlayerCharacters().put("Alpha", alpha);
		maze.getPlayerCharacters().put("Beta", beta);

		PartyCamp older = mgr.createCamp("Woods", new Point(1, 1));
		mgr.addCharacter(older, "Alpha");
		PartyCamp newer = mgr.createCamp("Woods", new Point(9, 9));
		mgr.addCharacter(newer, "Beta");

		killFieldParty(maze);

		ResumeFromPartyCampEvent fade =
			(ResumeFromPartyCampEvent) maze.partyResumesFromMostRecentCamp().get(0);
		assertNull(fade.resolve());
		assertEquals(0, maze.getParty().numAlive());

		completeResumeAfterFade(maze.getParty().getGold(), maze.getParty().getSupplies());

		assertEquals(List.of("Beta"), maze.getParty().getPartyNames());
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));
		assertNull(mgr.findCampAt("Woods", new Point(9, 9)));
		assertFalse(mgr.getCamps().stream().anyMatch(c ->
			c.getTile().equals(new Point(9, 9)) && c.getCharacterNames().isEmpty()));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void wipeWithNoCampWouldNotResume()
	{
		killFieldParty(maze);

		assertFalse(maze.wouldResumeFromCampOnWipe());
		assertFalse(PartyCampManager.getInstance().hasAnyCamp());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void deadFieldPartyGearDroppedOnDeathTile()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();
		PlayerCharacter leader = maze.getParty().getPlayerCharacter(0);
		PlayerCharacter scout = maze.getParty().getPlayerCharacter(1);

		Item loot = testItem("fallen-loot");
		leader.getInventory().add(loot);

		maze.setZoneAndTileForTesting("Woods", new Point(2, 2));
		assertTrue(maze.transferPlayerCharacterToCamp(scout));

		Point deathTile = new Point(7, 7);
		maze.setZoneAndTileForTesting("Woods", deathTile);
		killFieldParty(maze);

		ResumeFromPartyCampEvent fade =
			(ResumeFromPartyCampEvent) maze.partyResumesFromMostRecentCamp().get(0);
		assertNull(fade.resolve());
		assertTrue(leader.getAllItems().isEmpty());
		assertEquals(0, maze.getParty().numAlive());

		completeResumeAfterFade(maze.getParty().getGold(), maze.getParty().getSupplies());

		List<Item> onTile = ItemCacheManager.getInstance().getItemsOnTile(maze.getCurrentZone(), deathTile);
		assertNotNull(onTile);
		assertEquals(1, onTile.size());
		assertEquals("fallen-loot", onTile.get(0).getName());
		assertNull(mgr.findCampAt("Woods", new Point(2, 2)));
	}
}

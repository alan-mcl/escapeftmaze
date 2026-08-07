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
import mclachlan.maze.stat.PartyCampManager;
import mclachlan.maze.stat.PlayerCharacter;
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
		maze.setZoneAndTileForTesting("Woods", new Point(3, 4));

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

		mgr.createCamp("Elsewhere", new Point(0, 0));
		mgr.addCharacter("Leftover");

		assertFalse(maze.transferPlayerCharacterToCamp(scout));
		assertEquals(3, maze.getParty().size());
	}
}

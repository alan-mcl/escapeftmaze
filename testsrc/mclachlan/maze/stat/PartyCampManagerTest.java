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

package mclachlan.maze.stat;

import java.awt.Point;
import java.io.*;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.SingletonSilo;
import mclachlan.maze.data.v2.V2Utils;
import mclachlan.maze.game.PartyCamp;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.getPartyCampSerialiser;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic tests for the temporary party camp feature.
 */
public class PartyCampManagerTest extends MazeTestSupport
{
	private Database db;

	@BeforeEach
	void setUp() throws Exception
	{
		PartyCampManager.resetForTesting();
		db = TestData.buildEmptyDatabase();
	}

	@AfterEach
	void tearDownLocal()
	{
		PartyCampManager.resetForTesting();
		db = null;
	}

	/*-------------------------------------------------------------------------*/
	private Map<String, Object> normalise(Map<String, Object> map)
	{
		try
		{
			StringWriter sw = new StringWriter();
			V2Utils.writeJson(map, sw);
			return V2Utils.getMap(new BufferedReader(new StringReader(sw.toString())));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void createCampAndLeaveCharacterBehind()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		mgr.createCamp("Woods", new Point(4, 7));
		mgr.addCharacter("Scout");

		assertTrue(mgr.hasCamp());
		assertTrue(mgr.isCampAt("Woods", new Point(4, 7)));
		assertEquals(List.of("Scout"), mgr.getCamp().getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void reconstituteUntilEmptyClearsCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		mgr.createCamp("Woods", new Point(1, 2));
		mgr.addCharacter("Alpha");
		mgr.removeCharacter("Alpha");
		mgr.clearIfEmpty();

		assertFalse(mgr.hasCamp());
		assertNull(mgr.getCamp());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rejectsSecondCampAtDifferentLocation()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		mgr.createCamp("Woods", new Point(1, 1));
		mgr.addCharacter("Alpha");

		assertTrue(mgr.hasCamp());
		assertFalse(mgr.isCampAt("Woods", new Point(9, 9)));
		assertFalse(mgr.isCampAt("Caves", new Point(1, 1)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void partyCampSerialiserRoundTrip()
	{
		PartyCamp original = new PartyCamp(
			"Gatehouse",
			new Point(12, 8),
			new ArrayList<>(List.of("Brunswick", "Wa Na")));

		SingletonSilo<PartyCamp> silo = new SingletonSilo<>(getPartyCampSerialiser());

		Map<String, Object> before = getPartyCampSerialiser().toObject(original, db);
		PartyCamp restored = getPartyCampSerialiser().fromObject(before, db);
		Map<String, Object> after = getPartyCampSerialiser().toObject(restored, db);

		assertEquals(normalise(before), normalise(after));
		assertEquals("Gatehouse", restored.getZone());
		assertEquals(new Point(12, 8), restored.getTile());
		assertEquals(List.of("Brunswick", "Wa Na"), restored.getCharacterNames());
	}
}

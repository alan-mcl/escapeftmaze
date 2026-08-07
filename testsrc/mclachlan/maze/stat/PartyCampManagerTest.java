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

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.getPartyCampListSerialiser;
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

		PartyCamp camp = mgr.createCamp("Woods", new Point(4, 7));
		mgr.addCharacter(camp, "Scout");

		assertTrue(mgr.hasCamp());
		assertTrue(mgr.isCampAt("Woods", new Point(4, 7)));
		assertEquals(List.of("Scout"), camp.getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void reconstituteUntilEmptyClearsCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		PartyCamp camp = mgr.createCamp("Woods", new Point(1, 2));
		mgr.addCharacter(camp, "Alpha");
		mgr.removeCharacter(camp, "Alpha");
		mgr.removeCampIfEmpty(camp);

		assertFalse(mgr.hasCamp());
		assertNull(mgr.findCampAt("Woods", new Point(1, 2)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void allowsMultipleCampsAtDifferentLocations()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		PartyCamp campA = mgr.createCamp("Woods", new Point(1, 1));
		mgr.addCharacter(campA, "Alpha");
		PartyCamp campB = mgr.createCamp("Caves", new Point(9, 9));
		mgr.addCharacter(campB, "Beta");

		assertTrue(mgr.hasCamp());
		assertTrue(mgr.isCampAt("Woods", new Point(1, 1)));
		assertTrue(mgr.isCampAt("Caves", new Point(9, 9)));
		assertEquals(2, mgr.getCamps().size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void ensureCampAtReusesExistingCamp()
	{
		PartyCampManager mgr = PartyCampManager.getInstance();

		PartyCamp first = mgr.ensureCampAt("Woods", new Point(3, 3));
		mgr.addCharacter(first, "Scout");
		PartyCamp second = mgr.ensureCampAt("Woods", new Point(3, 3));

		assertSame(first, second);
		assertEquals(List.of("Scout"), second.getCharacterNames());
		assertEquals(1, mgr.getCamps().size());
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

	/*-------------------------------------------------------------------------*/
	@Test
	void partyCampListSerialiserRoundTrip()
	{
		List<PartyCamp> original = List.of(
			new PartyCamp("Woods", new Point(1, 2), new ArrayList<>(List.of("Alpha"))),
			new PartyCamp("Caves", new Point(9, 9), new ArrayList<>(List.of("Beta", "Gamma"))));

		Object before = getPartyCampListSerialiser().toObject(original, db);
		List<PartyCamp> restored = getPartyCampListSerialiser().fromObject(before, db);
		Object after = getPartyCampListSerialiser().toObject(restored, db);

		assertEquals(before, after);
		assertEquals(2, restored.size());
		assertEquals("Woods", restored.get(0).getZone());
		assertEquals(List.of("Beta", "Gamma"), restored.get(1).getCharacterNames());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void legacySingletonJsonLoadsAsOneElementList()
	{
		PartyCamp original = new PartyCamp(
			"Gatehouse",
			new Point(5, 6),
			new ArrayList<>(List.of("Rowan")));

		Map<String, Object> singleton = getPartyCampSerialiser().toObject(original, db);
		ArrayList<Object> list = new ArrayList<>();
		list.add(singleton);
		List<PartyCamp> asList = getPartyCampListSerialiser().fromObject(list, db);

		assertEquals(1, asList.size());
		assertEquals("Gatehouse", asList.get(0).getZone());
		assertEquals(List.of("Rowan"), asList.get(0).getCharacterNames());
	}
}

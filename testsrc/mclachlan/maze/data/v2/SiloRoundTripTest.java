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

package mclachlan.maze.data.v2;

import java.io.*;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.serialisers.V2SerialiserFactory;
import mclachlan.maze.stat.*;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 2: silo round-trips through in-memory readers/writers (no files).
 */
public class SiloRoundTripTest extends MazeTestSupport
{
	private static Database db;

	@BeforeAll
	void setUp() throws Exception
	{
		db = TestData.buildEmptyDatabase();
	}

	@AfterAll
	void tearDown()
	{
		Database.resetInstanceForTesting();
		db = null;
	}

	/*-------------------------------------------------------------------------*/
	private StatModifier mods(int brawn)
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, brawn);
		return sm;
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void simpleMapSiloRoundTrip() throws Exception
	{
		Map<String, Gender> original = new HashMap<>();
		original.put("a", new Gender("a", mods(1), new StatModifier(), new StatModifier()));
		original.put("b", new Gender("b", mods(2), new StatModifier(), new StatModifier()));

		SimpleMapSilo<Gender> silo =
			new SimpleMapSilo<>(V2SerialiserFactory.getGenderSerialiser());

		StringWriter sw = new StringWriter();
		silo.save(new BufferedWriter(sw), original, db);

		Map<String, Gender> reloaded = silo.load(
			new BufferedReader(new StringReader(sw.toString())), db);

		assertEquals(original, reloaded);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void singletonSiloRoundTrip() throws Exception
	{
		ExperienceTable original = new ExperienceTableArray("xp",
			new int[]{0, 0, 1000, 3000}, 5000);

		SingletonSilo<ExperienceTable> silo =
			new SingletonSilo<>(V2SerialiserFactory.getExperienceTableSerialiser());

		StringWriter sw = new StringWriter();
		silo.save(new BufferedWriter(sw), original, db);

		ExperienceTable reloaded = silo.load(
			new BufferedReader(new StringReader(sw.toString())), db);

		assertEquals(original, reloaded);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void mapSingletonSiloRoundTrip() throws Exception
	{
		Map<String, String> original = new HashMap<>();
		original.put("x", "1");
		original.put("y", "2");

		MapSingletonSilo silo = new MapSingletonSilo();

		StringWriter sw = new StringWriter();
		silo.save(new BufferedWriter(sw), original, db);

		Map reloaded = silo.load(
			new BufferedReader(new StringReader(sw.toString())), db);

		assertEquals(original, reloaded);
	}
}

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
import mclachlan.maze.game.journal.Journal;
import mclachlan.maze.game.journal.JournalEntry;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.getJournalSerialiser;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 2: round-trip tests for save-game slices edited in {@code SaveGamePanel}.
 */
public class SaveGameSliceRoundTripTest extends MazeTestSupport
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
	private Map normalise(Map map)
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
	void journalSerialiserRoundTrip()
	{
		Journal journal = new Journal("quest");
		journal.getContents().put("main.quest", List.of(
			new JournalEntry(42, "Found the gate key"),
			new JournalEntry(43, "Returned to the guild")));

		Map before = getJournalSerialiser().toObject(journal, db);
		Journal restored = getJournalSerialiser().fromObject(before, db);
		Map after = getJournalSerialiser().toObject(restored, db);

		assertEquals(normalise(before), normalise(after));
	}
}

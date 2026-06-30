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

package mclachlan.maze.test.support;

import mclachlan.maze.data.Database;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity test for the in-memory {@link Database} fixture itself.
 */
public class DatabaseBootstrapTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void buildsEmptyDatabaseAsSingleton() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();
		assertSame(db, Database.getInstance());
		assertTrue(db.getGenders().isEmpty());
		assertTrue(db.getItemTemplates().isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void populatedLoaderIsVisibleThroughDatabase() throws Exception
	{
		InMemoryLoader loader = new InMemoryLoader();
		loader.genders.put("Neuter",
			new mclachlan.maze.stat.Gender("Neuter", null, null, null));

		Database db = TestData.buildDatabase(loader);
		assertEquals(1, db.getGenders().size());
		assertEquals("Neuter", db.getGender("Neuter").getName());
	}
}

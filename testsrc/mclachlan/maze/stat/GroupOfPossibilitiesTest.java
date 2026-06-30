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

import java.util.*;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.util.MazeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link GroupOfPossibilities}. A 100% entry is always
 * included and a 0% entry never is, regardless of the roll, which keeps these
 * deterministic.
 */
public class GroupOfPossibilitiesTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void certainAndImpossibleEntries()
	{
		GroupOfPossibilities<String> g = new GroupOfPossibilities<>();
		g.add("always", 100);
		g.add("never", 0);

		for (int i = 0; i < 100; i++)
		{
			assertEquals(Collections.singletonList("always"), g.getRandom());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void getRandomRespectsMaxNr()
	{
		GroupOfPossibilities<String> g = new GroupOfPossibilities<>();
		g.add("a", 100);
		g.add("b", 100);
		g.add("c", 100);

		List<String> result = g.getRandom(2);
		assertEquals(2, result.size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void getPercentageAndValidation()
	{
		GroupOfPossibilities<String> g = new GroupOfPossibilities<>();
		g.add("a", 25);
		assertEquals(25, g.getPercentage("a"));
		assertThrows(MazeException.class, () -> g.getPercentage("missing"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void addNullThrows()
	{
		GroupOfPossibilities<String> g = new GroupOfPossibilities<>();
		assertThrows(MazeException.class, () -> g.add(null, 50));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void emptyGroup()
	{
		GroupOfPossibilities<String> g = new GroupOfPossibilities<>();
		assertTrue(g.isEmpty());
		assertTrue(g.getRandom().isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void addAllMerges()
	{
		GroupOfPossibilities<String> a = new GroupOfPossibilities<>();
		a.add("a", 10);
		GroupOfPossibilities<String> b = new GroupOfPossibilities<>();
		b.add("b", 20);

		a.addAll(b);
		assertEquals(10, a.getPercentage("a"));
		assertEquals(20, a.getPercentage("b"));
	}
}

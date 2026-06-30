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
 * Pure-logic tests for {@link PercentageTable}. Random selection is made
 * deterministic by using 0%/100% entries (which do not depend on the roll) and
 * by seeding {@link Dice} for the distribution check.
 */
public class PercentageTableTest extends MazeTestSupport
{
	private PercentageTable<String> twoItems()
	{
		return new PercentageTable<>(
			new String[]{"A", "B"},
			new Integer[]{30, 70},
			true);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void percentagesRoundTrip()
	{
		PercentageTable<String> t = twoItems();
		assertEquals(Arrays.asList(30, 70), t.getPercentages());
		assertEquals(30, t.getPercentage("A"));
		assertEquals(70, t.getPercentage("B"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void getPercentageInvalidKeyThrows()
	{
		assertThrows(MazeException.class, () -> twoItems().getPercentage("Z"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void cumulativePartialSumOver100Throws()
	{
		assertThrows(MazeException.class, () -> new PercentageTable<>(
			Arrays.asList("A", "B", "C"),
			Arrays.asList(60, 60, 60),
			true));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void invalidPercentThrows()
	{
		assertThrows(MazeException.class, () -> new PercentageTable<>(
			Collections.singletonList("A"),
			Collections.singletonList(150),
			true));
		assertThrows(MazeException.class, () -> new PercentageTable<>(
			Collections.singletonList("A"),
			Collections.singletonList(-5),
			true));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void singleHundredPercentItemAlwaysReturned()
	{
		PercentageTable<String> t = new PercentageTable<>(
			new String[]{"only"}, new Integer[]{100}, true);
		for (int i = 0; i < 100; i++)
		{
			assertEquals("only", t.getRandomItem());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void zeroPercentNonSummingTableReturnsNull()
	{
		PercentageTable<String> t = new PercentageTable<>(
			Arrays.asList("A"), Arrays.asList(0), false);
		for (int i = 0; i < 100; i++)
		{
			assertNull(t.getRandomItem());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void addBuildsCumulative()
	{
		PercentageTable<String> t = new PercentageTable<>(false);
		t.add("A", 100);
		assertEquals("A", t.getRandomItem());
		assertEquals(100, t.getPercentage("A"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void seededDistributionStaysWithinTable()
	{
		PercentageTable<String> t = twoItems();
		Map<String, Integer> counts = new HashMap<>();
		for (int i = 0; i < 5000; i++)
		{
			String item = t.getRandomItem();
			assertTrue(item.equals("A") || item.equals("B"), "unexpected item: " + item);
			counts.merge(item, 1, Integer::sum);
		}
		// both outcomes should occur given a 30/70 split over 5000 trials
		assertTrue(counts.getOrDefault("A", 0) > 0);
		assertTrue(counts.getOrDefault("B", 0) > 0);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		assertEquals(twoItems(), twoItems());
		assertEquals(twoItems().hashCode(), twoItems().hashCode());
	}
}

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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link Dice}.
 */
public class DiceTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void rangeMethods()
	{
		Dice d = new Dice(2, 6, 3);
		assertEquals(5, d.getMinPossible());     // 2 dice + 3
		assertEquals(15, d.getMaxPossible());    // 2*6 + 3
		assertEquals(9.0, d.getAverage(), 0.0001); // 2*6/2 + 3
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rollsAlwaysWithinBounds()
	{
		Dice d = new Dice(3, 6, 2);
		for (int i = 0; i < 1000; i++)
		{
			int r = d.roll("test");
			assertTrue(r >= d.getMinPossible(), "roll below min: " + r);
			assertTrue(r <= d.getMaxPossible(), "roll above max: " + r);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rollIsNeverNegativeEvenWithLargeNegativeModifier()
	{
		Dice d = new Dice(1, 6, -100);
		for (int i = 0; i < 200; i++)
		{
			assertEquals(0, d.roll("clamped"), "negative results must clamp to 0");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void seedingMakesRollsReproducible()
	{
		Dice d = new Dice(1, 100, 0);

		Dice.setRandomSeed(42L);
		List<Integer> first = new ArrayList<>();
		for (int i = 0; i < 50; i++)
		{
			first.add(d.roll("a"));
		}

		Dice.setRandomSeed(42L);
		List<Integer> second = new ArrayList<>();
		for (int i = 0; i < 50; i++)
		{
			second.add(d.roll("b"));
		}

		assertEquals(first, second);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nextIntIsWithinBounds()
	{
		for (int i = 0; i < 1000; i++)
		{
			int n = Dice.nextInt(10);
			assertTrue(n >= 0 && n < 10, "nextInt out of range: " + n);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void toStringFormatsModifier()
	{
		assertEquals("2d6+3", new Dice(2, 6, 3).toString());
		assertEquals("1d6", new Dice(1, 6, 0).toString());
		assertEquals("1d6-2", new Dice(1, 6, -2).toString());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		Dice a = new Dice(1, 8, 1);
		Dice b = new Dice(1, 8, 1);
		Dice c = new Dice(2, 8, 1);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a, c);
	}
}

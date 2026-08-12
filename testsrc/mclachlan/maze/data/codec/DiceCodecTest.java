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

package mclachlan.maze.data.codec;

import mclachlan.maze.stat.Dice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * String round-trip tests for {@link DiceCodec}.
 */
public class DiceCodecTest
{
	private void roundTrip(int n, int sides, int mod)
	{
		Dice d = new Dice(n, sides, mod);
		String s = DiceCodec.toString(d);
		assertEquals(d, DiceCodec.fromString(s), "round trip for " + s);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void roundTrips()
	{
		roundTrip(1, 6, 0);
		roundTrip(2, 6, 3);
		roundTrip(1, 4, -1);
		roundTrip(10, 100, 25);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nullAndEmpty()
	{
		assertEquals("", DiceCodec.toString(null));
		assertNull(DiceCodec.fromString(""));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void canonicalStrings()
	{
		assertEquals("2d6+3", DiceCodec.toString(new Dice(2, 6, 3)));
		assertEquals("1d4-1", DiceCodec.toString(new Dice(1, 4, -1)));
	}
}

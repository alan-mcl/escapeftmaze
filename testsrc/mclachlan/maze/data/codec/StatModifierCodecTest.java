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

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * String round-trip tests for {@link StatModifierCodec}, including the signed-byte
 * boundary behaviour of the hex encoding.
 */
public class StatModifierCodecTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void roundTrip()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 5);
		sm.setModifier(Stats.Modifier.SWING, -3);

		String s = StatModifierCodec.toString(sm);
		assertEquals(sm, StatModifierCodec.fromString(s));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void boundaryValues()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 127);
		sm.setModifier(Stats.Modifier.SWING, -128);

		StatModifier result = StatModifierCodec.fromString(StatModifierCodec.toString(sm));
		assertEquals(127, result.getModifier(Stats.Modifier.BRAWN));
		assertEquals(-128, result.getModifier(Stats.Modifier.SWING));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nullAndEmpty()
	{
		assertEquals("", StatModifierCodec.toString((StatModifier)null));
		assertNull(StatModifierCodec.fromString(""));
		assertNull(StatModifierCodec.fromString(null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void outOfRangeValueThrows()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 200);
		assertThrows(MazeException.class, () -> StatModifierCodec.toString(sm));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void emptyModifierSerialisesWithoutValues()
	{
		// an empty modifier still produces a (bitmap-only) string that round-trips
		StatModifier sm = new StatModifier();
		String s = StatModifierCodec.toString(sm);
		StatModifier result = StatModifierCodec.fromString(s);
		assertTrue(result == null || result.isEmpty());
	}
}

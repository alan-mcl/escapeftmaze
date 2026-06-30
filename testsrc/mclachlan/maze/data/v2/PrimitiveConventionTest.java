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

import mclachlan.maze.data.v2.serialisers.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 2: the "primitive" serialisers store domain values as plain strings.
 * These tests assert both the string convention and round-trip fidelity.
 */
public class PrimitiveConventionTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void statModifierStoredAsString()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 2);
		sm.setModifier(Stats.Modifier.SWING, -3);

		StatModifierSerialiser s = new StatModifierSerialiser();
		Object serialised = s.toObject(sm, null);

		assertInstanceOf(String.class, serialised);
		assertEquals(sm, s.fromObject(serialised, null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void diceStoredAsString()
	{
		Dice d = new Dice(2, 6, 1);

		DiceSerialiser s = new DiceSerialiser();
		Object serialised = s.toObject(d, null);

		assertInstanceOf(String.class, serialised);
		assertEquals("2d6+1", serialised);
		assertEquals(d, s.fromObject(serialised, null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void curMaxStoredAsString()
	{
		CurMax cm = new CurMax(7, 10);

		CurMaxSerialiser s = new CurMaxSerialiser();
		Object serialised = s.toObject(cm, null);

		assertInstanceOf(String.class, serialised);
		assertEquals(cm, s.fromObject(serialised, null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void curMaxSubStoredAsString()
	{
		CurMaxSub cms = new CurMaxSub(7, 10, 3);

		CurMaxSubSerialiser s = new CurMaxSubSerialiser();
		Object serialised = s.toObject(cms, null);

		assertInstanceOf(String.class, serialised);
		assertEquals(cms, s.fromObject(serialised, null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nullsAreToleratedByPrimitiveSerialisers()
	{
		assertNull(new CurMaxSerialiser().toObject(null, null));
		assertNull(new CurMaxSubSerialiser().toObject(null, null));
		assertNull(new CurMaxSerialiser().fromObject(null, null));
		assertNull(new CurMaxSubSerialiser().fromObject(null, null));
	}
}

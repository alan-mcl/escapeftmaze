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

package mclachlan.maze.stat.magic;

import mclachlan.maze.stat.Stats;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.StubActor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ValueList} composition.
 */
public class ValueListTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void emptyListComputesToZero()
	{
		assertEquals(0, new ValueList().compute(new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void sumsConstantValues()
	{
		ValueList vl = new ValueList(
			new Value(2, Value.SCALE.NONE),
			new Value(3, Value.SCALE.NONE));
		assertEquals(5, vl.compute(new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void mixesScalingAndModifier()
	{
		StubActor a = new StubActor("a").withModifier(Stats.Modifier.BRAWN, 4);
		ValueList vl = new ValueList(
			new Value(1, Value.SCALE.SCALE_WITH_CASTING_LEVEL),
			new ModifierValue(Stats.Modifier.BRAWN));
		// 1*castingLevel(2) + brawn(4) = 6
		assertEquals(6, vl.compute(a, 2));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void deepCopyConstructorIsIndependent()
	{
		ValueList original = new ValueList(new Value(5, Value.SCALE.NONE));
		ValueList copy = new ValueList(original);

		copy.getValues().get(0).setValue(99);
		assertEquals(5, original.compute(new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		ValueList a = new ValueList(new Value(1, Value.SCALE.NONE));
		ValueList b = new ValueList(new Value(1, Value.SCALE.NONE));
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}

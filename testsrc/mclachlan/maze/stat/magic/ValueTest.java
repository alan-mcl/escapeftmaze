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

import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.StubActor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Value} scaling and its subclasses, using a {@link StubActor}.
 */
public class ValueTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void constantValueIgnoresScaling()
	{
		Value v = new Value(5, Value.SCALE.NONE);
		StubActor a = new StubActor("a").withLevel(7);
		assertEquals(5, v.compute(a));
		assertEquals(5, v.compute(a, 10));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scaleWithCastingLevel()
	{
		Value v = new Value(5, Value.SCALE.SCALE_WITH_CASTING_LEVEL);
		StubActor a = new StubActor("a");
		assertEquals(15, v.compute(a, 3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scaleWithCharacterLevel()
	{
		Value v = new Value(2, Value.SCALE.SCALE_WITH_CHARACTER_LEVEL);
		StubActor a = new StubActor("a").withLevel(4);
		assertEquals(8, v.compute(a, 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scaleWithClassLevel()
	{
		Value v = new Value(2, Value.SCALE.SCALE_WITH_CLASS_LEVEL);
		v.setReference("Mage");
		StubActor a = new StubActor("a").withClassLevel("Mage", 3);
		assertEquals(6, v.compute(a, 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scaleWithModifier()
	{
		Value v = new Value(2, Value.SCALE.SCALE_WITH_MODIFIER);
		v.setReference(Stats.Modifier.BRAWN.name());
		StubActor a = new StubActor("a").withModifier(Stats.Modifier.BRAWN, 4);
		assertEquals(8, v.compute(a, 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scaleWithPartySizeNoGroupDefaultsToOne()
	{
		Value v = new Value(5, Value.SCALE.SCALE_WITH_PARTY_SIZE);
		StubActor a = new StubActor("a");
		assertEquals(5, v.compute(a, 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void negate()
	{
		Value v = new Value(5, Value.SCALE.NONE);
		v.setShouldNegate(true);
		StubActor a = new StubActor("a");
		assertEquals(-5, v.compute(a));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void modifierValue()
	{
		ModifierValue mv = new ModifierValue(Stats.Modifier.BRAWN);
		StubActor a = new StubActor("a").withModifier(Stats.Modifier.BRAWN, 6);
		assertEquals(6, mv.compute(a, 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void diceValueIsDeterministicUnderSeed()
	{
		StubActor a = new StubActor("a");

		seed(99L);
		int computed = new DiceValue(new mclachlan.maze.stat.Dice(1, 6, 0)).compute(a, 1);

		seed(99L);
		int expected = new mclachlan.maze.stat.Dice(1, 6, 0).roll("expected");

		assertEquals(expected, computed);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void diceValueScalesIterationsWithCastingLevel()
	{
		StubActor a = new StubActor("a");
		DiceValue dv = new DiceValue(new Dice(1, 6, 0));
		dv.setScaling(Value.SCALE.SCALE_WITH_CASTING_LEVEL);

		int result = dv.compute(a, 3); // sum of 3 d6 rolls
		assertTrue(result >= 3 && result <= 18, "3d6 out of range: " + result);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		Value a = new Value(5, Value.SCALE.NONE);
		Value b = new Value(5, Value.SCALE.NONE);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a, new Value(6, Value.SCALE.NONE));
	}
}

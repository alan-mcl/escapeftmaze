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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link StatModifier}.
 */
public class StatModifierTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void getModifierDefaultsToZero()
	{
		StatModifier sm = new StatModifier();
		assertEquals(0, sm.getModifier(Stats.Modifier.BRAWN));
		assertTrue(sm.isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void setAndGet()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 5);
		assertEquals(5, sm.getModifier(Stats.Modifier.BRAWN));
		assertFalse(sm.isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void incModifierAccumulates()
	{
		StatModifier sm = new StatModifier();
		sm.incModifier(Stats.Modifier.SWING, 2);
		sm.incModifier(Stats.Modifier.SWING, 3);
		assertEquals(5, sm.getModifier(Stats.Modifier.SWING));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void addModifiersMergesValues()
	{
		StatModifier a = new StatModifier();
		a.setModifier(Stats.Modifier.BRAWN, 2);
		a.setModifier(Stats.Modifier.SWING, 1);

		StatModifier b = new StatModifier();
		b.setModifier(Stats.Modifier.BRAWN, 3);
		b.setModifier(Stats.Modifier.THRUST, 4);

		a.addModifiers(b);

		assertEquals(5, a.getModifier(Stats.Modifier.BRAWN));
		assertEquals(1, a.getModifier(Stats.Modifier.SWING));
		assertEquals(4, a.getModifier(Stats.Modifier.THRUST));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void copyConstructorIsIndependent()
	{
		StatModifier a = new StatModifier();
		a.setModifier(Stats.Modifier.BRAWN, 7);

		StatModifier copy = new StatModifier(a);
		copy.setModifier(Stats.Modifier.BRAWN, 1);

		assertEquals(7, a.getModifier(Stats.Modifier.BRAWN));
		assertEquals(1, copy.getModifier(Stats.Modifier.BRAWN));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		StatModifier a = new StatModifier();
		a.setModifier(Stats.Modifier.BRAWN, 3);
		StatModifier b = new StatModifier();
		b.setModifier(Stats.Modifier.BRAWN, 3);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}

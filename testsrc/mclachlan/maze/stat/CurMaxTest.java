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
 * Pure-logic tests for {@link CurMax}.
 */
public class CurMaxTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void incCurrentClampsToMaximum()
	{
		CurMax cm = new CurMax(5, 10);
		cm.incCurrent(10);
		assertEquals(10, cm.getCurrent());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void incCurrentClampsToZeroFloor()
	{
		CurMax cm = new CurMax(5, 10);
		cm.incCurrent(-100);
		assertEquals(0, cm.getCurrent());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void decCurrentIsNotFloored()
	{
		CurMax cm = new CurMax(5, 10);
		cm.decCurrent(8);
		assertEquals(-3, cm.getCurrent());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void missingAndRatio()
	{
		CurMax cm = new CurMax(4, 10);
		assertEquals(6, cm.getMissing());
		assertEquals(0.4, cm.getRatio(), 0.0001);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void setCurrentToMax()
	{
		CurMax cm = new CurMax(3, 10);
		cm.setCurrentToMax();
		assertEquals(10, cm.getCurrent());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void incMaximum()
	{
		CurMax cm = new CurMax(3, 10);
		cm.incMaximum(5);
		assertEquals(15, cm.getMaximum());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void singleArgConstructorSetsBoth()
	{
		CurMax cm = new CurMax(7);
		assertEquals(7, cm.getCurrent());
		assertEquals(7, cm.getMaximum());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		assertEquals(new CurMax(3, 10), new CurMax(3, 10));
		assertEquals(new CurMax(3, 10).hashCode(), new CurMax(3, 10).hashCode());
		assertNotEquals(new CurMax(3, 10), new CurMax(4, 10));
	}
}

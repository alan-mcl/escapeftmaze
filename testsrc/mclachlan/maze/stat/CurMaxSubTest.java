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
 * Pure-logic tests for {@link CurMaxSub}, focusing on the sub-value invariant
 * {@code 0 <= sub <= current}.
 */
public class CurMaxSubTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void setCurrentBelowSubClampsSub()
	{
		CurMaxSub cms = new CurMaxSub(10, 10, 10);
		cms.setCurrent(5);
		assertEquals(5, cms.getCurrent());
		assertEquals(5, cms.getSub(), "sub must not exceed current");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void decCurrentClampsSub()
	{
		CurMaxSub cms = new CurMaxSub(10, 10, 8);
		cms.decCurrent(5);
		assertEquals(5, cms.getCurrent());
		assertEquals(5, cms.getSub());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void incSubClampsToCurrent()
	{
		CurMaxSub cms = new CurMaxSub(5, 10, 0);
		cms.incSub(100);
		assertEquals(5, cms.getSub());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void incSubClampsToZeroFloor()
	{
		CurMaxSub cms = new CurMaxSub(5, 10, 2);
		cms.incSub(-100);
		assertEquals(0, cms.getSub());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void subRatio()
	{
		CurMaxSub cms = new CurMaxSub(10, 10, 5);
		assertEquals(0.5, cms.getSubRatio(), 0.0001);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsConsidersSub()
	{
		assertEquals(new CurMaxSub(5, 10, 2), new CurMaxSub(5, 10, 2));
		assertNotEquals(new CurMaxSub(5, 10, 2), new CurMaxSub(5, 10, 3));
		assertEquals(new CurMaxSub(5, 10, 2).hashCode(), new CurMaxSub(5, 10, 2).hashCode());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void inheritsCurMaxClamping()
	{
		CurMaxSub cms = new CurMaxSub(5, 10, 0);
		cms.incCurrent(100);
		assertEquals(10, cms.getCurrent());
	}
}

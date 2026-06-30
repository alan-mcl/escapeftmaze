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

package mclachlan.maze.data.v1;

import mclachlan.maze.stat.CurMax;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * String round-trip tests for {@link V1CurMax} and {@link V1CurMaxSub}.
 */
public class V1CurMaxTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void curMaxRoundTrip()
	{
		CurMax cm = new CurMax(3, 10);
		assertEquals("3-10", V1CurMax.toString(cm));
		assertEquals(cm, V1CurMax.fromString("3-10"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void curMaxNullAndEmpty()
	{
		assertEquals("", V1CurMax.toString(null));
		assertNull(V1CurMax.fromString(""));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void curMaxSubRoundTrip()
	{
		mclachlan.maze.stat.CurMaxSub cms = new mclachlan.maze.stat.CurMaxSub(3, 10, 2);
		String s = V1CurMaxSub.toString(cms);
		assertEquals(cms, V1CurMaxSub.fromString(s));
	}
}

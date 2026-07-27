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

package mclachlan.maze.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Pure-logic tests for {@link StringUtil}.
 */
public class StringUtilTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void normalizeLineBreaksConvertsLiteralBackslashN()
	{
		assertEquals(
			"Line one" + System.lineSeparator() + "Line two",
			StringUtil.normalizeLineBreaks("Line one\\nLine two"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void normalizeLineBreaksLeavesOtherStringsAlone()
	{
		assertNull(StringUtil.normalizeLineBreaks(null));
		assertEquals("", StringUtil.normalizeLineBreaks(""));
		assertEquals("no breaks", StringUtil.normalizeLineBreaks("no breaks"));
		assertEquals(
			"already" + System.lineSeparator() + "broken",
			StringUtil.normalizeLineBreaks("already" + System.lineSeparator() + "broken"));
	}
}

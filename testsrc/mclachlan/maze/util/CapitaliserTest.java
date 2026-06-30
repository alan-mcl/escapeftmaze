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

package mclachlan.maze.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link Capitaliser}.
 */
public class CapitaliserTest
{
	/*-------------------------------------------------------------------------*/
	@Test
	void capitaliseWord()
	{
		assertEquals("Sword", Capitaliser.capitaliseWord("sword"));
		assertEquals("", Capitaliser.capitaliseWord(""));
		assertEquals("A", Capitaliser.capitaliseWord("a"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void capitaliseEachWord()
	{
		assertEquals("Long Sword", Capitaliser.capitalise("long sword"));
		assertEquals("Potion Of Healing", Capitaliser.capitalise("potion of healing"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void capitaliseNullReturnsNull()
	{
		assertNull(Capitaliser.capitalise(null));
	}
}

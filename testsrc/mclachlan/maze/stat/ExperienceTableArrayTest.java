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
 * Pure-logic tests for {@link ExperienceTableArray}, including past-table
 * "post-Gygax" extrapolation.
 */
public class ExperienceTableArrayTest
{
	// index is currentLevel+1, so the level-1->2 threshold lives at index 2
	private ExperienceTableArray table()
	{
		return new ExperienceTableArray(
			"test", new int[]{0, 0, 1000, 3000, 6000}, 10000);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nextLevelUpWithinTable()
	{
		ExperienceTableArray t = table();
		assertEquals(1000, t.getNextLevelUp(1));
		assertEquals(3000, t.getNextLevelUp(2));
		assertEquals(6000, t.getNextLevelUp(3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nextLevelUpPastTableExtrapolates()
	{
		ExperienceTableArray t = table();
		// level 5 -> 6 : 1 increment past the last table entry
		assertEquals(16000, t.getNextLevelUp(5));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void lastLevelUp()
	{
		ExperienceTableArray t = table();
		assertEquals(0, t.getLastLevelUp(1));
		assertEquals(3000, t.getLastLevelUp(3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void equalsAndHashCode()
	{
		assertEquals(table(), table());
		assertEquals(table().hashCode(), table().hashCode());
	}
}

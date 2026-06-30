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

import mclachlan.maze.util.MazeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link ExperienceTableFormulaic}.
 */
public class ExperienceTableFormulaicTest
{
	// levelOneToTwo=1000, baseIncrement=1000, multiplier=1.0, gygax=10, postGygax=100000
	private ExperienceTableFormulaic table()
	{
		return new ExperienceTableFormulaic("test", 1000, 1000, 1.0, 10, 100000);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void earlyLevels()
	{
		ExperienceTableFormulaic t = table();
		assertEquals(1000, t.getNextLevelUp(1));
		assertEquals(2000, t.getNextLevelUp(2));
		assertEquals(4000, t.getNextLevelUp(3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void pastGygaxConstantAddsFlatIncrement()
	{
		ExperienceTableFormulaic t = table();
		// level 10 -> 11: 1000 + 1000*(1+2+...+8) + (11-10)*100000
		assertEquals(137000, t.getNextLevelUp(10));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void lastLevelUp()
	{
		ExperienceTableFormulaic t = table();
		assertEquals(0, t.getLastLevelUp(1));
		assertEquals(2000, t.getLastLevelUp(3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void invalidLevelThrows()
	{
		assertThrows(MazeException.class, () -> table().getNextLevelUp(0));
	}
}

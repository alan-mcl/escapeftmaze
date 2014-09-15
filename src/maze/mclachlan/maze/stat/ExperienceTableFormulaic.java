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

/**
 * An experience table implemented as a formula.
 */
public class ExperienceTableFormulaic implements ExperienceTable
{
	String name;
	
	/** the experience required to get to lvl 2 */
	int levelOneToTwo;

	/** the base increment after lvl 2 */
	int baseIncrement;

	/** the multiplier to apply to each increment */
	double multiplier;

	/** the lvl at which to stop applying the increment */
	int gygaxConstant;

	/** how much xp to advance a lvl after the gygax constant is reached */
	int postGygaxIncrement;

	/*-------------------------------------------------------------------------*/
	public ExperienceTableFormulaic(
		String name,
		int levelOneToTwo,
		int baseIncrement,
		double multiplier,
		int gygaxConstant,
		int postGygaxIncrement)
	{
		this.name = name;
		this.levelOneToTwo = levelOneToTwo;
		this.baseIncrement = baseIncrement;
		this.multiplier = multiplier;
		this.gygaxConstant = gygaxConstant;
		this.postGygaxIncrement = postGygaxIncrement;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public int getNextLevelUp(int currentLevel)
	{
		if (currentLevel < 1)
		{
			throw new MazeException("Invalid level "+currentLevel);
		}

		int nextLevel = currentLevel+1;

		int result = levelOneToTwo;
		int ceil = Math.min(nextLevel, gygaxConstant);
		for (int i=2; i<ceil; i++)
		{
			result += baseIncrement * (i-1) * multiplier;
		}

		if (nextLevel > gygaxConstant)
		{
			result += ((nextLevel - gygaxConstant) * postGygaxIncrement);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getLastLevelUp(int currentLevel)
	{
		if (currentLevel == 1)
		{
			return 0;
		}
		else
		{
			return getNextLevelUp(currentLevel-1);
		}
	}
}

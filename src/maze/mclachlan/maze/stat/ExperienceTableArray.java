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

import java.util.*;

/**
 * An experience table implemented with an array
 */
public class ExperienceTableArray extends ExperienceTable
{
	String name;

	/**
	 * The experience totals required to advance to each level
	 */
	int[] levels;

	/**
	 * The amount of experience required to advance each level after the table.
	 */
	int postGygaxIncrement;

	public ExperienceTableArray()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param levels
	 * 	The experience totals required to advance to each level.
	 * @param postGygaxIncrement
	 * 	The amount of experience required to advance each level after the table.
	 */
	public ExperienceTableArray(String name, int[] levels, int postGygaxIncrement)
	{
		this.name = name;
		this.levels = levels;
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

	/*-------------------------------------------------------------------------*/
	public int getNextLevelUp(int currentLevel)
	{
		if (currentLevel+1 < levels.length)
		{
			return levels[currentLevel+1];
		}
		else
		{
			int diff = currentLevel+1 - levels.length;
			return levels[levels.length-1] + diff * postGygaxIncrement;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int[] getLevels()
	{
		return levels;
	}

	/*-------------------------------------------------------------------------*/
	public int getPostGygaxIncrement()
	{
		return postGygaxIncrement;
	}

	/*-------------------------------------------------------------------------*/
	public void setLevels(int[] levels)
	{
		this.levels = levels;
	}

	/*-------------------------------------------------------------------------*/
	public void setPostGygaxIncrement(int postGygaxIncrement)
	{
		this.postGygaxIncrement = postGygaxIncrement;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		ExperienceTableArray that = (ExperienceTableArray)o;

		if (getPostGygaxIncrement() != that.getPostGygaxIncrement())
		{
			return false;
		}
		if (!getName().equals(that.getName()))
		{
			return false;
		}
		return Arrays.equals(getLevels(), that.getLevels());
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + Arrays.hashCode(getLevels());
		result = 31 * result + getPostGygaxIncrement();
		return result;
	}
}

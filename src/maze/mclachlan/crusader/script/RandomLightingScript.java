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

package mclachlan.crusader.script;

import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.MapScript;

/**
 * Randomly varies the light level.
 */
public class RandomLightingScript extends MapScript
{
	private int[] affectedTiles;
	private int minLightLevel, maxLightLevel, frequency;

	public RandomLightingScript()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param affectedTiles
	 * 	Indices of tiles affected by this script.
	 * @param frequency
	 * 	How rapidly the light level should vary.  The higher this number is,
	 * 	the faster it will vary.
	 * @param minLightLevel
	 * 	The lower bound
	 * @param maxLightLevel
	 * 	The upper bound
	 */ 
	public RandomLightingScript(
		int[] affectedTiles,
		int frequency,
		int minLightLevel,
		int maxLightLevel)
	{
		this.affectedTiles = affectedTiles;
		this.frequency = frequency;
		this.maxLightLevel = maxLightLevel;
		this.minLightLevel = minLightLevel;
	}

	/*-------------------------------------------------------------------------*/
	public void execute(long framecount, Map map)
	{
		if (rand(0,100)<frequency)
		{
			int newLightLevel = rand(minLightLevel,maxLightLevel);
			
			for (int i = 0; i < affectedTiles.length; i++)
			{
				map.getTiles()[this.affectedTiles[i]].setCurrentLightLevel(newLightLevel); 
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private int rand(int min, int max)
	{
		double f = Math.random();
		return (int)((f * (max - min)) + min);
	}

	/*-------------------------------------------------------------------------*/
	public int[] getAffectedTiles()
	{
		return affectedTiles;
	}

	public int getFrequency()
	{
		return frequency;
	}

	public int getMaxLightLevel()
	{
		return maxLightLevel;
	}

	public int getMinLightLevel()
	{
		return minLightLevel;
	}

	public void setAffectedTiles(int[] affectedTiles)
	{
		this.affectedTiles = affectedTiles;
	}

	public void setMinLightLevel(int minLightLevel)
	{
		this.minLightLevel = minLightLevel;
	}

	public void setMaxLightLevel(int maxLightLevel)
	{
		this.maxLightLevel = maxLightLevel;
	}

	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof RandomLightingScript))
		{
			return false;
		}

		RandomLightingScript that = (RandomLightingScript)o;

		if (getMinLightLevel() != that.getMinLightLevel())
		{
			return false;
		}
		if (getMaxLightLevel() != that.getMaxLightLevel())
		{
			return false;
		}
		if (getFrequency() != that.getFrequency())
		{
			return false;
		}
		return Arrays.equals(getAffectedTiles(), that.getAffectedTiles());
	}

	@Override
	public int hashCode()
	{
		int result = Arrays.hashCode(getAffectedTiles());
		result = 31 * result + getMinLightLevel();
		result = 31 * result + getMaxLightLevel();
		result = 31 * result + getFrequency();
		return result;
	}
}

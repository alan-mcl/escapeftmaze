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

import mclachlan.crusader.Map;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Tile;

/**
 * Varies light level light along sine curve.
 */
public class SinusoidalLightingScript extends MapScript
{
	private int[] affectedTiles;
	private int diff;
	private int frequency;
	private int minLightLevel;
	private int maxLightLevel;
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @param affectedTiles
	 * 	Indices of tiles affected by this script.
	 * @param frequency
	 * 	How rapidly the light level should vary.  The higher this number is,
	 * 	the faster it will vary.
	 * @param minLightLevel
	 * 	The lower bound.
	 * @param maxLightLevel
	 * 	The upper bound.
	 */ 
	public SinusoidalLightingScript(
		int[] affectedTiles,
		int frequency,
		int minLightLevel,
		int maxLightLevel)
	{
		this.affectedTiles = affectedTiles;
		this.diff = (maxLightLevel-minLightLevel)/2;
		this.frequency = frequency;
		this.minLightLevel = minLightLevel;
		this.maxLightLevel = maxLightLevel;
	}
	
	/*-------------------------------------------------------------------------*/
	public void execute(int framecount, Map map)
	{
		double delta = this.diff*Math.sin(framecount*frequency*Math.PI/180.0);
		for (int i = 0; i < affectedTiles.length; i++)
		{
			Tile t = map.getTiles()[this.affectedTiles[i]];
			t.setCurrentLightLevel((int)(t.getLightLevel() +delta));
		}
	}

	/*-------------------------------------------------------------------------*/
	public int[] getAffectedTiles()
	{
		return affectedTiles;
	}

	public int getMaxLightLevel()
	{
		return maxLightLevel;
	}

	public int getMinLightLevel()
	{
		return minLightLevel;
	}

	public int getFrequency()
	{
		return frequency;
	}
}

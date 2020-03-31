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

package mclachlan.crusader;

/**
 * Represents a single tile.
 */
public class Tile
{
	Texture northWallTexture;
	Texture southWallTexture;
	Texture eastWallTexture;
	Texture westWallTexture;
	Texture floorTexture;
	Texture floorMaskTexture;
	Texture ceilingTexture;
	Texture ceilingMaskTexture;
	int lightLevel;
	int currentLightLevel;
	int ceilingHeight;

	/*-------------------------------------------------------------------------*/
	public Tile(
		Texture ceilingTexture,
		Texture ceilingMaskTexture,
		Texture floorTexture,
		Texture floorMaskTexture,
		Texture northWallTexture,
		Texture southWallTexture,
		Texture eastWallTexture,
		Texture westWallTexture,
		int lightLevel,
		int ceilingHeight)
	{
		this.ceilingTexture = ceilingTexture;
		this.ceilingMaskTexture = ceilingMaskTexture;
		this.floorTexture = floorTexture;
		this.floorMaskTexture = floorMaskTexture;
		this.northWallTexture = northWallTexture;
		this.southWallTexture = southWallTexture;
		this.eastWallTexture = eastWallTexture;
		this.westWallTexture = westWallTexture;
		this.lightLevel = this.currentLightLevel = lightLevel;
	}
	
	/*-------------------------------------------------------------------------*/
	public Tile(
		Texture ceilingTexture,
		Texture floorTexture,
		int lightLevel
	)
	{
		this(
			ceilingTexture,
			null,
			floorTexture,
			null,
			Map.NO_WALL,
			Map.NO_WALL,
			Map.NO_WALL,
			Map.NO_WALL,
			lightLevel,
			1);
	}

	public Texture getNorthWallTexture()
	{
		return northWallTexture;
	}

	public Texture getSouthWallTexture()
	{
		return southWallTexture;
	}

	public Texture getEastWallTexture()
	{
		return eastWallTexture;
	}

	public Texture getWestWallTexture()
	{
		return westWallTexture;
	}

	public void setNorthWallTexture(Texture northWallTexture)
	{
		this.northWallTexture = northWallTexture;
	}

	public void setSouthWallTexture(Texture southWallTexture)
	{
		this.southWallTexture = southWallTexture;
	}

	public void setEastWallTexture(Texture eastWallTexture)
	{
		this.eastWallTexture = eastWallTexture;
	}

	public void setWestWallTexture(Texture westWallTexture)
	{
		this.westWallTexture = westWallTexture;
	}

	public Texture getFloorTexture()
	{
		return floorTexture;
	}

	public Texture getCeilingTexture()
	{
		return ceilingTexture;
	}

	public int getLightLevel()
	{
		return lightLevel;
	}

	public int getCurrentLightLevel()
	{
		return currentLightLevel;
	}

	public void setCurrentLightLevel(int value)
	{
		this.currentLightLevel = value;
	}

	public void setCeilingTexture(Texture ceilingTexture)
	{
		this.ceilingTexture = ceilingTexture;
	}

	public void setFloorTexture(Texture floorTexture)
	{
		this.floorTexture = floorTexture;
	}

	public void setLightLevel(int lightLevel)
	{
		this.lightLevel = lightLevel;
	}

	public Texture getFloorMaskTexture()
	{
		return floorMaskTexture;
	}

	public void setFloorMaskTexture(Texture floorMaskTexture)
	{
		this.floorMaskTexture = floorMaskTexture;
	}

	public Texture getCeilingMaskTexture()
	{
		return ceilingMaskTexture;
	}

	public void setCeilingMaskTexture(Texture ceilingMaskTexture)
	{
		this.ceilingMaskTexture = ceilingMaskTexture;
	}

	public int getCeilingHeight()
	{
		return ceilingHeight;
	}

	public void setCeilingHeight(int ceilingHeight)
	{
		this.ceilingHeight = ceilingHeight;
	}
}

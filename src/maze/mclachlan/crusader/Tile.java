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

	public Tile()
	{
	}

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
		this.ceilingHeight = ceilingHeight;
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
		this.lightLevel = this.currentLightLevel = lightLevel;
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

	/*-------------------------------------------------------------------------*/

/*	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Tile))
		{
			return false;
		}

		Tile tile = (Tile)o;

		if (getLightLevel() != tile.getLightLevel())
		{
			return false;
		}
		if (getCeilingHeight() != tile.getCeilingHeight())
		{
			return false;
		}
		if (getNorthWallTexture() != null ? !getNorthWallTexture().equals(tile.getNorthWallTexture()) : tile.getNorthWallTexture() != null)
		{
			return false;
		}
		if (getSouthWallTexture() != null ? !getSouthWallTexture().equals(tile.getSouthWallTexture()) : tile.getSouthWallTexture() != null)
		{
			return false;
		}
		if (getEastWallTexture() != null ? !getEastWallTexture().equals(tile.getEastWallTexture()) : tile.getEastWallTexture() != null)
		{
			return false;
		}
		if (getWestWallTexture() != null ? !getWestWallTexture().equals(tile.getWestWallTexture()) : tile.getWestWallTexture() != null)
		{
			return false;
		}
		if (getFloorTexture() != null ? !getFloorTexture().equals(tile.getFloorTexture()) : tile.getFloorTexture() != null)
		{
			return false;
		}
		if (getFloorMaskTexture() != null ? !getFloorMaskTexture().equals(tile.getFloorMaskTexture()) : tile.getFloorMaskTexture() != null)
		{
			return false;
		}
		if (getCeilingTexture() != null ? !getCeilingTexture().equals(tile.getCeilingTexture()) : tile.getCeilingTexture() != null)
		{
			return false;
		}
		return getCeilingMaskTexture() != null ? getCeilingMaskTexture().equals(tile.getCeilingMaskTexture()) : tile.getCeilingMaskTexture() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getNorthWallTexture() != null ? getNorthWallTexture().hashCode() : 0;
		result = 31 * result + (getSouthWallTexture() != null ? getSouthWallTexture().hashCode() : 0);
		result = 31 * result + (getEastWallTexture() != null ? getEastWallTexture().hashCode() : 0);
		result = 31 * result + (getWestWallTexture() != null ? getWestWallTexture().hashCode() : 0);
		result = 31 * result + (getFloorTexture() != null ? getFloorTexture().hashCode() : 0);
		result = 31 * result + (getFloorMaskTexture() != null ? getFloorMaskTexture().hashCode() : 0);
		result = 31 * result + (getCeilingTexture() != null ? getCeilingTexture().hashCode() : 0);
		result = 31 * result + (getCeilingMaskTexture() != null ? getCeilingMaskTexture().hashCode() : 0);
		result = 31 * result + getLightLevel();
		result = 31 * result + getCeilingHeight();
		return result;
	}*/
}

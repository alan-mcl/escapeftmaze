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

package mclachlan.maze.editor.swing.map;

import java.util.*;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class SingleTileProxy extends TileProxy
{
	mclachlan.crusader.Tile crusaderTile;
	mclachlan.maze.map.Tile mazeTile;

	public SingleTileProxy(Tile crusaderTile, mclachlan.maze.map.Tile mazeTile)
	{
		this.crusaderTile = crusaderTile;
		this.mazeTile = mazeTile;
	}
	
	// Crusader tile properties
	public Texture getFloorTexture()
	{
		return crusaderTile.getFloorTexture();
	}

	public Texture getFloorMaskTexture()
	{
		return crusaderTile.getFloorMaskTexture();
	}

	public Texture getCeilingMaskTexture()
	{
		return crusaderTile.getCeilingMaskTexture();
	}

	public Texture getCeilingTexture()
	{
		return crusaderTile.getCeilingTexture();
	}

	public int getLightLevel()
	{
		return crusaderTile.getLightLevel();
	}

	public void setCeilingTexture(Texture ceilingTexture)
	{
		crusaderTile.setCeilingTexture(ceilingTexture);
	}

	public void setCeilingMaskTexture(Texture ceilingTexture)
	{
		crusaderTile.setCeilingMaskTexture(ceilingTexture);
	}

	public void setFloorTexture(Texture floorTexture)
	{
		crusaderTile.setFloorTexture(floorTexture);
	}

	public void setFloorMaskTexture(Texture floorTexture)
	{
		crusaderTile.setFloorMaskTexture(floorTexture);
	}

	public void setLightLevel(int lightLevel)
	{
		crusaderTile.setLightLevel(lightLevel);
	}
	
	// Maze tile properties
	public StatModifier getStatModifier()
	{
		return mazeTile.getStatModifier();
	}

	public String getTerrainSubType()
	{
		return mazeTile.getTerrainSubType();
	}

	public String getTerrainType()
	{
		return mazeTile.getTerrainType();
	}

	public int getRandomEncounterChance()
	{
		return mazeTile.getRandomEncounterChance();
	}

	public EncounterTable getRandomEncounters()
	{
		return mazeTile.getRandomEncounters();
	}

	public List<TileScript> getScripts()
	{
		return mazeTile.getScripts();
	}

	public void setRandomEncounterChance(int randomEncounterChance)
	{
		mazeTile.setRandomEncounterChance(randomEncounterChance);
	}

	public void setRandomEncounters(EncounterTable randomEncounters)
	{
		mazeTile.setRandomEncounters(randomEncounters);
	}

	public void setScripts(List<TileScript> scripts)
	{
		mazeTile.setScripts(scripts);
	}

	public void setStatModifier(StatModifier statModifier)
	{
		mazeTile.setStatModifier(statModifier);
	}

	public void setTerrainSubType(String terrainSubType)
	{
		mazeTile.setTerrainSubType(terrainSubType);
	}

	public void setTerrainType(String terrainType)
	{
		mazeTile.setTerrainType(terrainType);
	}
}

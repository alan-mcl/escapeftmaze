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
	@Override
	public Texture getFloorTexture()
	{
		return crusaderTile.getFloorTexture();
	}

	@Override
	public Texture getFloorMaskTexture()
	{
		return crusaderTile.getFloorMaskTexture();
	}

	@Override
	public Texture getCeilingMaskTexture()
	{
		return crusaderTile.getCeilingMaskTexture();
	}

	@Override
	public Texture getCeilingTexture()
	{
		return crusaderTile.getCeilingTexture();
	}

	@Override
	public int getLightLevel()
	{
		return crusaderTile.getLightLevel();
	}

	@Override
	public int getCeilingHeight()
	{
		return crusaderTile.getCeilingHeight();
	}

	@Override
	public void setCeilingTexture(Texture ceilingTexture)
	{
		crusaderTile.setCeilingTexture(ceilingTexture);
	}

	@Override
	public void setCeilingMaskTexture(Texture ceilingTexture)
	{
		crusaderTile.setCeilingMaskTexture(ceilingTexture);
	}

	@Override
	public void setFloorTexture(Texture floorTexture)
	{
		crusaderTile.setFloorTexture(floorTexture);
	}

	@Override
	public void setFloorMaskTexture(Texture floorTexture)
	{
		crusaderTile.setFloorMaskTexture(floorTexture);
	}

	@Override
	public void setLightLevel(int lightLevel)
	{
		crusaderTile.setLightLevel(lightLevel);
	}

	@Override
	public void setCeilingHeight(int height)
	{
		crusaderTile.setCeilingHeight(height);
	}

	@Override
	public mclachlan.maze.map.Tile.RestingDanger getRestingDanger()
	{
		return mazeTile.getRestingDanger();
	}

	@Override
	public mclachlan.maze.map.Tile.RestingEfficiency getRestingEfficiency()
	{
		return mazeTile.getRestingEfficiency();
	}

	@Override
	public void setRestingDanger(
		mclachlan.maze.map.Tile.RestingDanger restingDanger)
	{
		mazeTile.setRestingDanger(restingDanger);
	}

	@Override
	public void setRestingEfficiency(
		mclachlan.maze.map.Tile.RestingEfficiency restingEfficiency)
	{
		mazeTile.setRestingEfficiency(restingEfficiency);
	}

	// Maze tile properties
	@Override
	public StatModifier getStatModifier()
	{
		return mazeTile.getStatModifier();
	}

	@Override
	public String getTerrainSubType()
	{
		return mazeTile.getTerrainSubType();
	}

	@Override
	public mclachlan.maze.map.Tile.TerrainType getTerrainType()
	{
		return mazeTile.getTerrainType();
	}

	@Override
	public int getRandomEncounterChance()
	{
		return mazeTile.getRandomEncounterChance();
	}

	@Override
	public EncounterTable getRandomEncounters()
	{
		return mazeTile.getRandomEncounters();
	}

	@Override
	public List<TileScript> getScripts()
	{
		return mazeTile.getScripts();
	}

	@Override
	public void setRandomEncounterChance(int randomEncounterChance)
	{
		mazeTile.setRandomEncounterChance(randomEncounterChance);
	}

	@Override
	public void setRandomEncounters(EncounterTable randomEncounters)
	{
		mazeTile.setRandomEncounters(randomEncounters);
	}

	@Override
	public void setScripts(List<TileScript> scripts)
	{
		mazeTile.setScripts(scripts);
	}

	@Override
	public void setStatModifier(StatModifier statModifier)
	{
		mazeTile.setStatModifier(statModifier);
	}

	@Override
	public void setTerrainSubType(String terrainSubType)
	{
		mazeTile.setTerrainSubType(terrainSubType);
	}

	@Override
	public void setTerrainType(mclachlan.maze.map.Tile.TerrainType terrainType)
	{
		mazeTile.setTerrainType(terrainType);
	}
}

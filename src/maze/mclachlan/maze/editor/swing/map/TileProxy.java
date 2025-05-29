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
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Texture;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public abstract class TileProxy
{
	// Crusader tile properties
	public abstract Texture getFloorTexture();

	public abstract Texture getFloorMaskTexture();

	public abstract Texture getCeilingTexture();

	public abstract Texture getCeilingMaskTexture();

	public abstract int getLightLevel();

	public abstract int getCeilingHeight();

	public abstract void setCeilingTexture(Texture ceilingTexture);

	public abstract void setCeilingMaskTexture(Texture ceilingTexture);

	public abstract void setFloorTexture(Texture floorTexture);

	public abstract void setFloorMaskTexture(Texture floorTexture);

	public abstract void setLightLevel(int lightLevel);

	public abstract void setCeilingHeight(int height);

	// Maze tile properties
	public abstract StatModifier getStatModifier();

	public abstract String getTerrainSubType();

	public abstract Tile.TerrainType getTerrainType();

	public abstract int getRandomEncounterChance();

	public abstract EncounterTable getRandomEncounters();

	public abstract List<TileScript> getScripts();

	public abstract void setRandomEncounterChance(int randomEncounterChance);

	public abstract void setRandomEncounters(EncounterTable randomEncounters);

	public abstract void setScripts(List<TileScript> scripts);

	public abstract void setStatModifier(StatModifier statModifier);

	public abstract void setTerrainSubType(String terrainSubType);

	public abstract void setTerrainType(Tile.TerrainType terrainType);

	public abstract Tile.RestingDanger getRestingDanger();

	public abstract Tile.RestingEfficiency getRestingEfficiency();

	public abstract void setRestingDanger(Tile.RestingDanger restingDanger);

	public abstract void setRestingEfficiency(
		Tile.RestingEfficiency restingEfficiency);

	public abstract void setSector(String sector);

	public abstract String getSector();

	public abstract MouseClickScript getFloorMouseClickScript();

	public abstract void setFloorMouseClickScript(
		MouseClickScript floorMouseClickScript);

	public abstract MouseClickScript getFloorMaskTextureMouseClickScript();

	public abstract void setFloorMaskTextureMouseClickScript(
		MouseClickScript floorMaskTextureMouseClickScript);

	public abstract MouseClickScript getCeilingMouseClickScript();

	public abstract void setCeilingMouseClickScript(
		MouseClickScript ceilingMouseClickScript);

	public abstract MouseClickScript getCeilingMaskTextureMouseClickScript();

	public abstract void setCeilingMaskTextureMouseClickScript(
		MouseClickScript ceilingMaskTextureMouseClickScript);
}

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

import java.util.Arrays;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;

/**
 * Applies cloned map element state onto live Crusader / maze objects.
 */
public class MapElementApplier
{
	/*-------------------------------------------------------------------------*/
	public static void applyCrusaderTile(Tile dest, Tile src)
	{
		Tile copy = MapElementCloner.cloneCrusaderTile(src);
		dest.setFloorTexture(copy.getFloorTexture());
		dest.setFloorMaskTexture(copy.getFloorMaskTexture());
		dest.setCeilingTexture(copy.getCeilingTexture());
		dest.setCeilingMaskTexture(copy.getCeilingMaskTexture());
		dest.setLightLevel(copy.getLightLevel());
		dest.setCeilingHeight(copy.getCeilingHeight());
		dest.setFloorMouseClickScript(copy.getFloorMouseClickScript());
		dest.setFloorMaskTextureMouseClickScript(copy.getFloorMaskTextureMouseClickScript());
		dest.setCeilingMouseClickScript(copy.getCeilingMouseClickScript());
		dest.setCeilingMaskTextureMouseClickScript(copy.getCeilingMaskTextureMouseClickScript());
	}

	/*-------------------------------------------------------------------------*/
	public static void applyMazeTile(
		mclachlan.maze.map.Tile dest,
		mclachlan.maze.map.Tile src)
	{
		mclachlan.maze.map.Tile copy = MapElementCloner.cloneMazeTile(src);
		dest.setStatModifier(copy.getStatModifier());
		dest.setTerrainSubType(copy.getTerrainSubType());
		dest.setTerrainType(copy.getTerrainType());
		dest.setRandomEncounterChance(copy.getRandomEncounterChance());
		dest.setRandomEncounters(copy.getRandomEncounters());
		dest.setScripts(copy.getScripts());
		dest.setRestingDanger(copy.getRestingDanger());
		dest.setRestingEfficiency(copy.getRestingEfficiency());
		dest.setSector(copy.getSector());
	}

	/*-------------------------------------------------------------------------*/
	public static void applyWall(Wall dest, Wall src)
	{
		Wall copy = MapElementCloner.cloneWall(src);
		dest.setVisible(copy.isVisible());
		dest.setSolid(copy.isSolid());
		dest.setHeight(copy.getHeight());
		dest.setTextures(copyTextureArray(copy.getTextures()));
		dest.setMaskTextures(copyTextureArray(copy.getMaskTextures()));
		dest.setMouseClickScript(copyScript(copy.getMouseClickScript()));
		dest.setMaskTextureMouseClickScript(copyScript(copy.getMaskTextureMouseClickScript()));
		dest.setInternalScript(copyScript(copy.getInternalScript()));
	}

	/*-------------------------------------------------------------------------*/
	private static Texture[] copyTextureArray(Texture[] textures)
	{
		if (textures == null)
		{
			return null;
		}

		return Arrays.copyOf(textures, textures.length);
	}

	/*-------------------------------------------------------------------------*/
	private static MouseClickScript copyScript(MouseClickScript script)
	{
		return MapElementCloner.cloneMouseClickScript(script);
	}
}

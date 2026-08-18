/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.dungeongen.fragment;

import java.awt.Rectangle;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Copies a rectangular region from a fragment zone onto a live floor zone
 * (Crusader map + maze tiles). Mirrors editor paste logic without editor deps.
 */
public final class FragmentStamp
{
	private enum WallSide
	{
		NORTH, SOUTH, EAST, WEST
	}

	private FragmentStamp()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void stamp(
		Zone floor,
		Zone fragment,
		int destX,
		int destY)
	{
		int fragW = fragment.getWidth();
		int fragL = fragment.getLength();
		Map floorMap = floor.getMap();
		Map fragMap = fragment.getMap();

		for (int x = 0; x < fragW; x++)
		{
			for (int y = 0; y < fragL; y++)
			{
				int tx = destX + x;
				int ty = destY + y;
				if (!inBounds(floor, tx, ty))
				{
					continue;
				}

				applyMazeTile(
					floor.getTiles()[tx][ty],
					fragment.getTiles()[x][y]);

				int floorIndex = ty * floorMap.getWidth() + tx;
				int fragIndex = y * fragMap.getWidth() + x;
				applyCrusaderTile(
					floorMap.getTiles()[floorIndex],
					fragMap.getTiles()[fragIndex]);

				for (WallSide side : WallSide.values())
				{
					Wall destWall = wallAt(floorMap, tx, ty, side);
					Wall srcWall = wallAt(fragMap, x, y, side);
					if (destWall != null && srcWall != null)
					{
						applyWall(destWall, srcWall);
					}
				}
			}
		}

		stampObjects(floor, fragment, destX, destY, fragMap, floorMap);
	}

	/*-------------------------------------------------------------------------*/
	private static void stampObjects(
		Zone floor,
		Zone fragment,
		int destX,
		int destY,
		Map fragMap,
		Map floorMap)
	{
		List<EngineObject> objects = fragMap.getExpandedObjects();
		if (objects == null || objects.isEmpty())
		{
			return;
		}

		int floorW = floorMap.getWidth();
		int baseImageSize = floorMap.getBaseImageSize();

		for (EngineObject src : objects)
		{
			int fragW = fragMap.getWidth();
			int localX = src.getGridX();
			int localY = src.getGridY();
			if (localX <= 0 && localY <= 0 && src.getTileIndex() >= 0)
			{
				localX = src.getTileIndex() % fragW;
				localY = src.getTileIndex() / fragW;
			}
			if (localX < 0 || localY < 0
				|| localX >= fragment.getWidth() || localY >= fragment.getLength())
			{
				continue;
			}

			int worldX = destX + localX;
			int worldY = destY + localY;
			if (!inBounds(floor, worldX, worldY))
			{
				continue;
			}

			EngineObject copy = src.copyObject();
			int tileIndex = worldY * floorW + worldX;
			copy.setTileIndex(tileIndex);
			copy.setGridX(worldX);
			copy.setGridY(worldY);

			int offsetX = (worldX - localX) * baseImageSize;
			int offsetY = (worldY - localY) * baseImageSize;
			copy.setXPos(src.getXPos() + offsetX);
			copy.setYPos(src.getYPos() + offsetY);

			floorMap.addObject(copy);
		}

		floorMap.init();
	}

	/*-------------------------------------------------------------------------*/
	public static Rectangle bounds(int destX, int destY, Zone fragment)
	{
		return new Rectangle(destX, destY, fragment.getWidth(), fragment.getLength());
	}

	/*-------------------------------------------------------------------------*/
	private static void applyMazeTile(
		mclachlan.maze.map.Tile dest,
		mclachlan.maze.map.Tile src)
	{
		mclachlan.maze.map.Tile copy = src.copyTile();
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
	private static void applyCrusaderTile(Tile dest, Tile src)
	{
		Tile copy = src.copyTile();
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
	private static void applyWall(Wall dest, Wall src)
	{
		Wall copy = src.copyWall();
		dest.setVisible(copy.isVisible());
		dest.setSolid(copy.isSolid());
		dest.setHeight(copy.getHeight());
		dest.setTextures(copy.getTextures());
		dest.setMaskTextures(copy.getMaskTextures());
		dest.setMouseClickScript(copy.getMouseClickScript());
		dest.setMaskTextureMouseClickScript(copy.getMaskTextureMouseClickScript());
		dest.setInternalScript(copy.getInternalScript());
	}

	/*-------------------------------------------------------------------------*/
	private static Wall wallAt(Map map, int x, int y, WallSide side)
	{
		int tileIndex = y * map.getWidth() + x;
		int wallIndex;
		switch (side)
		{
			case NORTH ->
			{
				wallIndex = map.getNorthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			}
			case SOUTH ->
			{
				wallIndex = map.getSouthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			}
			case EAST ->
			{
				wallIndex = map.getEastWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			}
			case WEST ->
			{
				wallIndex = map.getWestWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			}
			default -> throw new IllegalStateException("side " + side);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean inBounds(Zone zone, int x, int y)
	{
		return x >= 0 && y >= 0 && x < zone.getWidth() && y < zone.getLength();
	}
}

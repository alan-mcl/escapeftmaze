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

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.*;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.Zone;

/**
 * Deep-clones and rotates authored fragment zones in memory (90° clockwise per
 * quarter turn). Rotated variants are never written to disk.
 */
public final class FragmentRotate
{
	private enum WallSide
	{
		NORTH, SOUTH, EAST, WEST
	}

	private FragmentRotate()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static Zone rotate(Zone source, int quarterTurns, String cloneName)
	{
		int turns = ((quarterTurns % 4) + 4) % 4;
		Zone clone = deepClone(source);
		for (int i = 0; i < turns; i++)
		{
			clone = rotateQuarterCW(clone);
		}
		clone.setName(cloneName);
		return clone;
	}

	/*-------------------------------------------------------------------------*/
	private static Zone deepClone(Zone source)
	{
		int w = source.getWidth();
		int l = source.getLength();
		mclachlan.crusader.Map oldMap = source.getMap();

		mclachlan.maze.map.Tile[][] newLogic = new mclachlan.maze.map.Tile[w][l];
		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < l; y++)
			{
				mclachlan.maze.map.Tile copy = source.getTiles()[x][y].copyTile();
				copy.setCoords(new Point(x, y));
				newLogic[x][y] = copy;
			}
		}

		Tile[] newCrusader = new Tile[oldMap.getTiles().length];
		for (int i = 0; i < newCrusader.length; i++)
		{
			newCrusader[i] = oldMap.getTiles()[i].copyTile();
		}

		Wall[] newHoriz = new Wall[oldMap.getHorizontalWalls().length];
		for (int i = 0; i < newHoriz.length; i++)
		{
			newHoriz[i] = oldMap.getHorizontalWalls()[i].copyWall();
		}

		Wall[] newVert = new Wall[oldMap.getVerticalWalls().length];
		for (int i = 0; i < newVert.length; i++)
		{
			newVert[i] = oldMap.getVerticalWalls()[i].copyWall();
		}

		List<EngineObject> newObjects = new ArrayList<>();
		if (oldMap.getExpandedObjects() != null)
		{
			for (EngineObject obj : oldMap.getExpandedObjects())
			{
				newObjects.add(obj.copyObject());
			}
		}

		mclachlan.crusader.Map newMap = new mclachlan.crusader.Map(
			l,
			w,
			oldMap.getBaseImageSize(),
			newCrusader,
			oldMap.getTextures(),
			newHoriz,
			newVert,
			oldMap.getSkyConfigs(),
			newObjects,
			oldMap.getScripts());
		newMap.init();

		Zone zone = copyZoneShell(source);
		zone.setMap(newMap);
		zone.setTiles(newLogic);
		if (source.getPlayerOrigin() != null)
		{
			zone.setPlayerOrigin(new Point(source.getPlayerOrigin()));
		}
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	private static Zone copyZoneShell(Zone source)
	{
		Zone zone = new Zone();
		zone.setName(source.getName());
		zone.setDisplayName(source.getDisplayName());
		zone.setPortals(new mclachlan.maze.map.Portal[0]);
		zone.setScript(source.getScript() != null ? source.getScript() : new DefaultZoneScript());
		zone.setMetadata(source.getMetadata() == null ? null : new LinkedHashMap<>(source.getMetadata()));
		zone.setShadeTargetColor(source.getShadeTargetColor());
		zone.setTransparentColor(source.getTransparentColor());
		zone.setDoShading(source.isDoShading());
		zone.setDoLighting(source.isDoLighting());
		zone.setShadingDistance(source.getShadingDistance());
		zone.setShadingMultiplier(source.getShadingMultiplier());
		zone.setProjectionPlaneOffset(source.getProjectionPlaneOffset());
		zone.setPlayerFieldOfView(source.getPlayerFieldOfView());
		zone.setScaleDistFromProjPlane(source.getScaleDistFromProjPlane());
		zone.setOrder(source.getOrder());
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	private static Zone rotateQuarterCW(Zone source)
	{
		int oldW = source.getWidth();
		int oldL = source.getLength();
		int newW = oldL;
		int newL = oldW;
		mclachlan.crusader.Map oldMap = source.getMap();
		int tileSize = oldMap.getBaseImageSize();

		mclachlan.maze.map.Tile[][] oldTiles = source.getTiles();
		mclachlan.maze.map.Tile[][] newTiles = new mclachlan.maze.map.Tile[newW][newL];
		Tile[] newCrusaderTiles = new Tile[newW * newL];
		Wall[] newHoriz = new Wall[newW * (newL + 1)];
		Wall[] newVert = new Wall[(newW + 1) * newL];
		Arrays.fill(newHoriz, solidPlaceholder(oldMap));
		Arrays.fill(newVert, solidPlaceholder(oldMap));

		for (int nx = 0; nx < newW; nx++)
		{
			for (int ny = 0; ny < newL; ny++)
			{
				int sx = ny;
				int sy = oldL - 1 - nx;

				mclachlan.maze.map.Tile srcLogic = oldTiles[sx][sy];
				mclachlan.maze.map.Tile destLogic = srcLogic.copyTile();
				destLogic.setCoords(new Point(nx, ny));
				newTiles[nx][ny] = destLogic;

				int oldIndex = sy * oldW + sx;
				int newIndex = ny * newW + nx;
				newCrusaderTiles[newIndex] = oldMap.getTiles()[oldIndex].copyTile();

				for (WallSide destSide : WallSide.values())
				{
					WallSide srcSide = wallSourceSide(destSide);
					Wall srcWall = wallAt(oldMap, sx, sy, srcSide);
					if (srcWall != null)
					{
						setWall(newHoriz, newVert, newW, nx, ny, destSide, srcWall.copyWall());
					}
				}
			}
		}

		List<EngineObject> newObjects = new ArrayList<>();
		if (oldMap.getExpandedObjects() != null)
		{
			for (EngineObject src : oldMap.getExpandedObjects())
			{
				newObjects.add(rotateObject(src, oldW, oldL, newW, tileSize));
			}
		}

		mclachlan.crusader.Map newMap = new mclachlan.crusader.Map(
			newL,
			newW,
			tileSize,
			newCrusaderTiles,
			oldMap.getTextures(),
			newHoriz,
			newVert,
			oldMap.getSkyConfigs(),
			newObjects,
			oldMap.getScripts());
		newMap.init();

		Zone zone = copyZoneShell(source);
		zone.setMap(newMap);
		zone.setTiles(newTiles);
		Point origin = source.getPlayerOrigin();
		if (origin != null)
		{
			zone.setPlayerOrigin(new Point(oldL - 1 - origin.y, origin.x));
		}
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	static EngineObject rotateObject(
		EngineObject src,
		int oldW,
		int oldL,
		int newW,
		int tileSize)
	{
		int sx = src.getGridX();
		int sy = src.getGridY();
		if (sx <= 0 && sy <= 0 && src.getTileIndex() >= 0)
		{
			sx = src.getTileIndex() % oldW;
			sy = src.getTileIndex() / oldW;
		}

		int nx = oldL - 1 - sy;
		int ny = sx;
		int ox = src.getXPos() - sx * tileSize;
		int oy = src.getYPos() - sy * tileSize;
		int newOx = (tileSize - oy) % tileSize;
		int newOy = ox;

		EngineObject copy = src.copyObject();
		copy.setGridX(nx);
		copy.setGridY(ny);
		copy.setTileIndex(ny * newW + nx);
		copy.setXPos(nx * tileSize + newOx);
		copy.setYPos(ny * tileSize + newOy);
		rotateTexturesCW(copy);
		return copy;
	}

	/*-------------------------------------------------------------------------*/
	static void rotateTexturesCW(EngineObject obj)
	{
		Texture north = obj.getNorthTexture();
		Texture east = obj.getEastTexture();
		Texture south = obj.getSouthTexture();
		Texture west = obj.getWestTexture();
		obj.setNorthTexture(west);
		obj.setEastTexture(north);
		obj.setSouthTexture(east);
		obj.setWestTexture(south);
	}

	/*-------------------------------------------------------------------------*/
	static int rotateFacingCW(int facing)
	{
		return switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> CrusaderEngine.Facing.EAST;
			case CrusaderEngine.Facing.EAST -> CrusaderEngine.Facing.SOUTH;
			case CrusaderEngine.Facing.SOUTH -> CrusaderEngine.Facing.WEST;
			case CrusaderEngine.Facing.WEST -> CrusaderEngine.Facing.NORTH;
			default -> facing;
		};
	}

	/*-------------------------------------------------------------------------*/
	private static WallSide wallSourceSide(WallSide destSide)
	{
		return switch (destSide)
		{
			case NORTH -> WallSide.WEST;
			case EAST -> WallSide.NORTH;
			case SOUTH -> WallSide.EAST;
			case WEST -> WallSide.SOUTH;
		};
	}

	/*-------------------------------------------------------------------------*/
	private static Wall wallAt(mclachlan.crusader.Map map, int x, int y, WallSide side)
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
	private static void setWall(
		Wall[] horiz,
		Wall[] vert,
		int width,
		int x,
		int y,
		WallSide side,
		Wall wall)
	{
		int tileIndex = y * width + x;
		switch (side)
		{
			case NORTH -> horiz[tileIndex] = wall;
			case SOUTH -> horiz[tileIndex + width] = wall;
			case WEST -> vert[x + y * (width + 1)] = wall;
			case EAST -> vert[x + y * (width + 1) + 1] = wall;
			default -> throw new IllegalStateException("side " + side);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Wall solidPlaceholder(mclachlan.crusader.Map map)
	{
		Wall[] horiz = map.getHorizontalWalls();
		for (Wall w : horiz)
		{
			if (w != null && w.isSolid())
			{
				return w.copyWall();
			}
		}
		return new Wall(
			new Texture[]{mclachlan.crusader.Map.NO_WALL},
			null,
			true,
			true,
			1,
			null,
			null,
			null);
	}
}

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

import java.util.ArrayList;
import java.util.List;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Pastes a {@link MapSelectionClipboard} onto a zone at the given anchor tile.
 */
public class MapSelectionPaster
{
	/*-------------------------------------------------------------------------*/
	public static List<Object> paste(
		MapSelectionClipboard clipboard, Zone zone, int destX, int destY)
	{
		List<Object> pasted = new ArrayList<>();
		mclachlan.crusader.Map map = zone.getMap();
		int width = zone.getWidth();
		int length = zone.getLength();
		int pixelDeltaX = (destX - clipboard.getOriginX()) * map.getBaseImageSize();
		int pixelDeltaY = (destY - clipboard.getOriginY()) * map.getBaseImageSize();

		for (MapSelectionClipboard.TileEntry entry : clipboard.getTiles())
		{
			int x = destX + entry.relX;
			int y = destY + entry.relY;
			if (!inBounds(x, y, width, length))
			{
				continue;
			}

			Tile destCrusaderTile = map.getTiles()[y * width + x];
			mclachlan.maze.map.Tile destMazeTile = zone.getTiles()[x][y];
			MapElementApplier.applyCrusaderTile(destCrusaderTile, entry.crusaderTile);
			MapElementApplier.applyMazeTile(destMazeTile, entry.mazeTile);
			pasted.add(destCrusaderTile);
		}

		for (MapSelectionClipboard.WallEntry entry : clipboard.getWalls())
		{
			int x = destX + entry.relX;
			int y = destY + entry.relY;
			if (!inBounds(x, y, width, length))
			{
				continue;
			}

			Wall destWall = getWallAt(map, x, y, entry.side);
			MapElementApplier.applyWall(destWall, entry.wall);
			pasted.add(destWall);
		}

		for (MapSelectionClipboard.ObjectEntry entry : clipboard.getObjects())
		{
			int x = destX + entry.relX;
			int y = destY + entry.relY;
			if (!inBounds(x, y, width, length))
			{
				continue;
			}

			EngineObject eo = MapElementCloner.cloneObject(entry.object);
			eo.setXPos(entry.xPos + pixelDeltaX);
			eo.setYPos(entry.yPos + pixelDeltaY);
			map.addObject(eo);
			map.initObjectFromXY(eo);
			pasted.add(eo);
		}

		return pasted;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean inBounds(int x, int y, int width, int length)
	{
		return x >= 0 && y >= 0 && x < width && y < length;
	}

	/*-------------------------------------------------------------------------*/
	private static Wall getWallAt(
		mclachlan.crusader.Map map,
		int x,
		int y,
		MapSelectionClipboard.WallSide side)
	{
		int tileIndex = y * map.getWidth() + x;
		int wallIndex;

		switch (side)
		{
			case NORTH:
				wallIndex = map.getNorthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			case SOUTH:
				wallIndex = map.getSouthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			case EAST:
				wallIndex = map.getEastWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			case WEST:
				wallIndex = map.getWestWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			default:
				throw new IllegalStateException("Unknown wall side "+side);
		}
	}
}

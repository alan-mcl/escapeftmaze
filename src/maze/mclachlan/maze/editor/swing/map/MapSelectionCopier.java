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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 * Copies the current map selection into a {@link MapSelectionClipboard}.
 */
public class MapSelectionCopier
{
	/*-------------------------------------------------------------------------*/
	public static MapSelectionClipboard copy(MapEditor editor, Zone zone)
	{
		List<Object> selection = editor.getSelection();

		if (selection == null || selection.isEmpty())
		{
			return null;
		}

		List<Tile> selectedTiles = new ArrayList<>();
		List<Wall> selectedWalls = new ArrayList<>();
		List<EngineObject> selectedObjects = new ArrayList<>();

		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				selectedTiles.add((Tile)obj);
			}
			else if (obj instanceof Wall)
			{
				selectedWalls.add((Wall)obj);
			}
			else if (obj instanceof EngineObject)
			{
				selectedObjects.add((EngineObject)obj);
			}
			else
			{
				throw new MazeException("Unexpected object in selection ["+obj+"]");
			}
		}

		Point origin = computeOrigin(selectedTiles, selectedWalls, selectedObjects, editor, zone);
		int originX = origin.x;
		int originY = origin.y;

		MapSelectionClipboard clipboard = new MapSelectionClipboard();
		clipboard.setOrigin(originX, originY);

		for (Tile tile : selectedTiles)
		{
			Point coords = getTileCoords(editor, tile);
			clipboard.getTiles().add(new MapSelectionClipboard.TileEntry(
				coords.x - originX,
				coords.y - originY,
				MapElementCloner.cloneCrusaderTile(tile),
				MapElementCloner.cloneMazeTile(editor.getMazeTile(tile))));
		}

		for (Wall wall : selectedWalls)
		{
			WallLocation location = resolveWallLocation(zone, wall);
			clipboard.getWalls().add(new MapSelectionClipboard.WallEntry(
				location.x - originX,
				location.y - originY,
				location.side,
				MapElementCloner.cloneWall(wall)));
		}

		for (EngineObject object : selectedObjects)
		{
			Point coords = getObjectCoords(zone, object);
			clipboard.getObjects().add(new MapSelectionClipboard.ObjectEntry(
				coords.x - originX,
				coords.y - originY,
				object.getXPos(),
				object.getYPos(),
				MapElementCloner.cloneObject(object)));
		}

		return clipboard;
	}

	/*-------------------------------------------------------------------------*/
	private static Point computeOrigin(
		List<Tile> tiles,
		List<Wall> walls,
		List<EngineObject> objects,
		MapEditor editor,
		Zone zone)
	{
		List<Point> points = new ArrayList<>();

		if (!tiles.isEmpty())
		{
			for (Tile tile : tiles)
			{
				points.add(getTileCoords(editor, tile));
			}
		}
		else if (!walls.isEmpty())
		{
			for (Wall wall : walls)
			{
				points.add(getWallCoords(zone, wall));
			}
		}
		else
		{
			for (EngineObject object : objects)
			{
				points.add(getObjectCoords(zone, object));
			}
		}

		int originX = points.get(0).x;
		int originY = points.get(0).y;
		for (Point p : points)
		{
			originX = Math.min(originX, p.x);
			originY = Math.min(originY, p.y);
		}

		return new Point(originX, originY);
	}

	/*-------------------------------------------------------------------------*/
	private static Point getTileCoords(MapEditor editor, Tile tile)
	{
		int index = editor.getCrusaderIndexOfTile(tile);
		int width = editor.getMap().getWidth();
		return new Point(index % width, index / width);
	}

	/*-------------------------------------------------------------------------*/
	private static Point getObjectCoords(Zone zone, EngineObject object)
	{
		int index = object.getTileIndex();
		int width = zone.getWidth();
		return new Point(index % width, index / width);
	}

	/*-------------------------------------------------------------------------*/
	private static Point getWallCoords(Zone zone, Wall wall)
	{
		WallLocation location = resolveWallLocation(zone, wall);
		return new Point(location.x, location.y);
	}

	/*-------------------------------------------------------------------------*/
	static WallLocation resolveWallLocation(Zone zone, Wall wall)
	{
		mclachlan.crusader.Map map = zone.getMap();
		int width = map.getWidth();

		for (int i = 0; i < map.getTiles().length; i++)
		{
			if (map.getHorizontalWalls()[map.getNorthWall(i)] == wall)
			{
				return new WallLocation(i % width, i / width,
					MapSelectionClipboard.WallSide.NORTH);
			}
			if (map.getHorizontalWalls()[map.getSouthWall(i)] == wall)
			{
				return new WallLocation(i % width, i / width,
					MapSelectionClipboard.WallSide.SOUTH);
			}
			if (map.getVerticalWalls()[map.getEastWall(i)] == wall)
			{
				return new WallLocation(i % width, i / width,
					MapSelectionClipboard.WallSide.EAST);
			}
			if (map.getVerticalWalls()[map.getWestWall(i)] == wall)
			{
				return new WallLocation(i % width, i / width,
					MapSelectionClipboard.WallSide.WEST);
			}
		}

		throw new MazeException("Cannot resolve wall location for "+wall);
	}

	/*-------------------------------------------------------------------------*/
	static class WallLocation
	{
		final int x;
		final int y;
		final MapSelectionClipboard.WallSide side;

		WallLocation(int x, int y, MapSelectionClipboard.WallSide side)
		{
			this.x = x;
			this.y = y;
			this.side = side;
		}
	}
}

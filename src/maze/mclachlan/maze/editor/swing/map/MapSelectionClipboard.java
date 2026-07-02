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

/**
 * In-memory clipboard for map-editor copy/paste.
 */
public class MapSelectionClipboard
{
	public enum WallSide
	{
		NORTH, SOUTH, EAST, WEST
	}

	public static class TileEntry
	{
		public final int relX;
		public final int relY;
		public final Tile crusaderTile;
		public final mclachlan.maze.map.Tile mazeTile;

		public TileEntry(
			int relX,
			int relY,
			Tile crusaderTile,
			mclachlan.maze.map.Tile mazeTile)
		{
			this.relX = relX;
			this.relY = relY;
			this.crusaderTile = crusaderTile;
			this.mazeTile = mazeTile;
		}
	}

	public static class WallEntry
	{
		public final int relX;
		public final int relY;
		public final WallSide side;
		public final Wall wall;

		public WallEntry(int relX, int relY, WallSide side, Wall wall)
		{
			this.relX = relX;
			this.relY = relY;
			this.side = side;
			this.wall = wall;
		}
	}

	public static class ObjectEntry
	{
		public final int relX;
		public final int relY;
		public final int xPos;
		public final int yPos;
		public final EngineObject object;

		public ObjectEntry(
			int relX,
			int relY,
			int xPos,
			int yPos,
			EngineObject object)
		{
			this.relX = relX;
			this.relY = relY;
			this.xPos = xPos;
			this.yPos = yPos;
			this.object = object;
		}
	}

	private int originX;
	private int originY;
	private final List<TileEntry> tiles = new ArrayList<>();
	private final List<WallEntry> walls = new ArrayList<>();
	private final List<ObjectEntry> objects = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	public int getOriginX()
	{
		return originX;
	}

	/*-------------------------------------------------------------------------*/
	public int getOriginY()
	{
		return originY;
	}

	/*-------------------------------------------------------------------------*/
	public void setOrigin(int originX, int originY)
	{
		this.originX = originX;
		this.originY = originY;
	}

	/*-------------------------------------------------------------------------*/
	public List<TileEntry> getTiles()
	{
		return tiles;
	}

	/*-------------------------------------------------------------------------*/
	public List<WallEntry> getWalls()
	{
		return walls;
	}

	/*-------------------------------------------------------------------------*/
	public List<ObjectEntry> getObjects()
	{
		return objects;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEmpty()
	{
		return tiles.isEmpty() && walls.isEmpty() && objects.isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		originX = 0;
		originY = 0;
		tiles.clear();
		walls.clear();
		objects.clear();
	}
}

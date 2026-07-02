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

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.serialisers.V2SerialiserFactory;

/**
 * Deep-clones map elements via V2 serialisation round-trips.
 */
public class MapElementCloner
{
	/*-------------------------------------------------------------------------*/
	public static mclachlan.maze.map.Tile cloneMazeTile(mclachlan.maze.map.Tile tile)
	{
		return V2SerialiserFactory.cloneMazeTile(tile, Database.getInstance());
	}

	/*-------------------------------------------------------------------------*/
	public static Tile cloneCrusaderTile(Tile tile)
	{
		return V2SerialiserFactory.cloneCrusaderTile(tile, Database.getInstance());
	}

	/*-------------------------------------------------------------------------*/
	public static Wall cloneWall(Wall wall)
	{
		return V2SerialiserFactory.cloneWall(wall, Database.getInstance());
	}

	/*-------------------------------------------------------------------------*/
	public static EngineObject cloneObject(EngineObject object)
	{
		return V2SerialiserFactory.cloneObject(object, Database.getInstance());
	}

	/*-------------------------------------------------------------------------*/
	public static mclachlan.crusader.MouseClickScript cloneMouseClickScript(
		mclachlan.crusader.MouseClickScript script)
	{
		return V2SerialiserFactory.cloneMouseClickScript(script, Database.getInstance());
	}
}

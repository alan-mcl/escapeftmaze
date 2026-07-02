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
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;

/**
 * Deep-clones map elements via domain object copy methods.
 */
public class MapElementCloner
{
	/*-------------------------------------------------------------------------*/
	public static mclachlan.maze.map.Tile cloneMazeTile(mclachlan.maze.map.Tile tile)
	{
		return tile.copyTile();
	}

	/*-------------------------------------------------------------------------*/
	public static Tile cloneCrusaderTile(Tile tile)
	{
		return tile.copyTile();
	}

	/*-------------------------------------------------------------------------*/
	public static Wall cloneWall(Wall wall)
	{
		return wall.copyWall();
	}

	/*-------------------------------------------------------------------------*/
	public static EngineObject cloneObject(EngineObject object)
	{
		return object.copyObject();
	}

	/*-------------------------------------------------------------------------*/
	public static MouseClickScript cloneMouseClickScript(MouseClickScript script)
	{
		return script == null ? null : script.copyScript();
	}
}

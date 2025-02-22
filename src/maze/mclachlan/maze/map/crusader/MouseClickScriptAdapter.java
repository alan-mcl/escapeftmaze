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

package mclachlan.maze.map.crusader;

import mclachlan.crusader.Map;
import mclachlan.crusader.MouseClickScript;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.game.Maze;
import java.awt.*;

/**
 *
 */
public class MouseClickScriptAdapter implements MouseClickScript
{
	private final TileScript script;
	
	/*-------------------------------------------------------------------------*/
	public MouseClickScriptAdapter(TileScript script)
	{
		this.script = script;
	}

	/*-------------------------------------------------------------------------*/
	public void initialise(Map map)
	{
		// nothing to do
	}

	/*-------------------------------------------------------------------------*/
	public void execute(Map map)
	{
		Maze maze = Maze.getInstance();
		Point tile = maze.getTile();
		int facing = maze.getFacing();

		if (script.shouldExecute(maze, tile, tile, facing, TileScript.PlayerAction.MOUSE_CLICK))
		{
			Maze.getInstance().appendEvents(script.execute(maze, tile, tile, facing));
		}
	}

	@Override
	public int getMaxDist()
	{
		return 1;
	}

	/*-------------------------------------------------------------------------*/
	public TileScript getScript()
	{
		return script;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof MouseClickScriptAdapter))
		{
			return false;
		}

		MouseClickScriptAdapter that = (MouseClickScriptAdapter)o;

		return getScript() != null ? getScript().equals(that.getScript()) : that.getScript() == null;
	}

	@Override
	public int hashCode()
	{
		return getScript() != null ? getScript().hashCode() : 0;
	}
}

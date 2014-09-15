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

package mclachlan.maze.arena;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.SignBoardEvent;

/**
 * Test arena script implementation.
 * Displays the magic sign board's text, if it has been added.
 */
public class MagicSignBoardText extends TileScript
{
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(MagicSignBoardTrigger.VAR) 
			&& tile != previousTile 
			&& (facing == CrusaderEngine.Facing.SOUTH || 
			facing == CrusaderEngine.Facing.NORTH))
		{
			List<MazeEvent> result = new ArrayList();
			result.add(
				new SignBoardEvent(
					"\n* MAGIC SIGN BOARD *" +
					"\n" +
					"\nThis sign board was added when the " +
					"\nplayer stepped into the alcove."));
			return result;
		}

		return null;
	}
}

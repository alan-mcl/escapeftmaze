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

package mclachlan.maze.map;

import java.awt.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;

/**
 *
 */
public class HiddenStuff extends TileScript
{
	private String mazeVariable;
	private MazeScript preScript, content;
	
	/*-------------------------------------------------------------------------*/
	public HiddenStuff(MazeScript content, MazeScript preScript, String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
		this.content = content;
		this.preScript = preScript;
	}
	
	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (preScript != null)
		{
			return preScript.getEvents();
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> handlePlayerAction(Maze maze, Point tile, int facing, int playerAction)
	{
		if (playerAction == PlayerAction.SEARCH
			&& (mazeVariable == null || MazeVariables.get(mazeVariable) == null))
		{
			if (mazeVariable != null && !mazeVariable.equals(""))
			{
				MazeVariables.set(mazeVariable, "1");
			}
			return content.getEvents();
		}
		else
		{
			return null;
		}
	}

	public MazeScript getContent()
	{
		return content;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public MazeScript getPreScript()
	{
		return preScript;
	}
}

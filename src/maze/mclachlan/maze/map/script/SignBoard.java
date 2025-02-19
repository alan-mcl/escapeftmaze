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

package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;

/**
 *
 */
public class SignBoard extends TileScript
{
	private String text;

	public SignBoard()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SignBoard(String text)
	{
		this.text = text;
	}
	
	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.add(new SignBoardEvent(text));
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		SignBoard signBoard = (SignBoard)o;

		return getText() != null ? getText().equals(signBoard.getText()) : signBoard.getText() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getText() != null ? getText().hashCode() : 0);
		return result;
	}
}

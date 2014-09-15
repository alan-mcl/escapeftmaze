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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;
import java.util.*;

/**
 *
 */
public class SignBoardEvent extends MazeEvent
{
	String text;

	/*-------------------------------------------------------------------------*/
	public SignBoardEvent(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().signBoard(this.text, this);
		
		// a bit of a hack to ensure that if we have other events following a sign
		// board they are properly displayed.  The right way to fix this would be
		// to implement the sign board stuff without involving a change in game
		// state.
		synchronized(this)
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}
		}
		
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getSignBoardText()
	{
		return text;
	}
}

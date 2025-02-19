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

package mclachlan.maze.game.event;

import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.util.MazeException;
import java.util.*;

/**
 *
 */
public class BlockingScreenEvent extends MazeEvent
{
	private String imageResource;
	private int mode;

	public BlockingScreenEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public BlockingScreenEvent(String imageResource, int mode)
	{
		this.imageResource = imageResource;
		this.mode = mode;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{

		Maze.getInstance().getUi().showBlockingScreen(
			imageResource, mode, Maze.getInstance().getEventMutex());
		
		synchronized(Maze.getInstance().getEventMutex())
		{
			try
			{
				Maze.getInstance().getEventMutex().wait();
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}
		}
		
		Maze.getInstance().getUi().clearBlockingScreen();
		
		return null;
	}
	
	/*-------------------------------------------------------------------------*/

	public String getImageResource()
	{
		return imageResource;
	}

	public int getMode()
	{
		return mode;
	}

	public void setImageResource(String imageResource)
	{
		this.imageResource = imageResource;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
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

		BlockingScreenEvent that = (BlockingScreenEvent)o;

		if (getMode() != that.getMode())
		{
			return false;
		}
		return getImageResource() != null ? getImageResource().equals(that.getImageResource()) : that.getImageResource() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getImageResource() != null ? getImageResource().hashCode() : 0;
		result = 31 * result + getMode();
		return result;
	}
}

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

package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.data.v2.V2Seralisable;

/**
 *
 */
public abstract class MazeEvent implements V2Seralisable
{
	/*-------------------------------------------------------------------------*/
	/**
	 * Apply all the effects of this event.  This callback is used to sync
	 * displayed effects with displayed events.  It should return an array
	 * of events that result from resolving this event.  These will be processed
	 * before any other events already in the queue.
	 * <p>
	 * This default implementation does nothing.
	 *
	 * @return
	 * 	An array of events.  May return <code>null</code> or an empty array.
	 */
	public List<MazeEvent> resolve()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return Indicate whether text should be cleared just prior
	 * to this event being displayed.
	 * This default implementation returns false.
	 */
	public boolean shouldClearText()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return any text to display in the event messages UI for the player, or
	 * null if none.
	 */
	public String getText()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	protected static List<MazeEvent> getList(MazeEvent... events)
	{
		return new ArrayList<>(Arrays.asList(events));
	}

	/*-------------------------------------------------------------------------*/
	public static class Delay
	{
		public static final int NONE = 0;
		public static final int WAIT_ON_CLICK = -1;
		public static final int WAIT_ON_READLINE = -2;
	}
}

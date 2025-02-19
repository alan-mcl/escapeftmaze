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

package mclachlan.maze.stat.combat.event;

import mclachlan.maze.game.MazeEvent;

/**
 * Note only works in combat
 */
public class DelayEvent extends MazeEvent
{
	private int delay;

	/*-------------------------------------------------------------------------*/
	public DelayEvent(int delay)
	{
		this.delay = delay;
	}

	/*-------------------------------------------------------------------------*/
	public DelayEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

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

		DelayEvent that = (DelayEvent)o;

		return getDelay() == that.getDelay();
	}

	@Override
	public int hashCode()
	{
		return getDelay();
	}
}

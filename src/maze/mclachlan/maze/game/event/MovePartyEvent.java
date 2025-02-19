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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import java.awt.*;

/**
 *
 */
public class MovePartyEvent extends MazeEvent
{
	private Point pos;
	private int facing;

	public MovePartyEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public MovePartyEvent(Point pos, int facing)
	{
		this.facing = facing;
		this.pos = pos;
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> resolve()
	{
		if (pos.x > -1 && pos.y > -1)
		{
			return Maze.getInstance().setPlayerPos(pos, facing);
		}
		else
		{
			return Maze.getInstance().setPlayerPos(Maze.getInstance().getPlayerPos(), facing);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getFacing()
	{
		return facing;
	}

	public Point getPos()
	{
		return pos;
	}

	public void setPos(Point pos)
	{
		this.pos = pos;
	}

	public void setFacing(int facing)
	{
		this.facing = facing;
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

		MovePartyEvent that = (MovePartyEvent)o;

		if (getFacing() != that.getFacing())
		{
			return false;
		}
		return getPos() != null ? getPos().equals(that.getPos()) : that.getPos() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getPos() != null ? getPos().hashCode() : 0;
		result = 31 * result + getFacing();
		return result;
	}
}

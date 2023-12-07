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
	private final Point pos;
	private final int facing;

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
			Maze.getInstance().setPlayerPos(pos, facing);
		}
		else
		{
			Maze.getInstance().setPlayerPos(Maze.getInstance().getPlayerPos(), facing);
		}
		return null;
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
}

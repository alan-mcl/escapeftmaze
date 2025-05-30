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

package mclachlan.maze.stat.npc;

import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import java.util.*;
import mclachlan.maze.stat.Dice;

/**
 *
 */
public class BackPartyUpEvent extends MazeEvent
{
	private Dice maxTiles;
	private int facing;

	/*-------------------------------------------------------------------------*/
	public BackPartyUpEvent(Dice maxTiles, int facing)
	{
		this.maxTiles = maxTiles;
		this.facing = facing;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		return Maze.getInstance().backPartyUp(maxTiles.roll("backPartyUpEvent"), facing);
	}

	public Dice getMaxTiles()
	{
		return maxTiles;
	}

	public void setMaxTiles(Dice maxTiles)
	{
		this.maxTiles = maxTiles;
	}

	public int getFacing()
	{
		return facing;
	}

	public void setFacing(int facing)
	{
		this.facing = facing;
	}
}
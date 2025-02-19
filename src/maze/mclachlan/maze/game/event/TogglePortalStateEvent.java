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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.util.MazeException;

public class TogglePortalStateEvent extends MazeEvent
{
	private Point tile;
	private int facing;

	public TogglePortalStateEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public TogglePortalStateEvent(Point tile, int facing)
	{
		this.tile = tile;
		this.facing = facing;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		Portal portal = Maze.getInstance().getCurrentZone().getPortal(tile, facing);

		if (portal == null)
		{
			throw new MazeException("no portal found to toggle: "+tile+"::"+facing);
		}

		List<MazeEvent> result = new ArrayList<>();

		if (Portal.State.WALL_LIKE.equals(portal.getState()) || Portal.State.LOCKED.equals(portal.getState()))
		{
			result.add(new SetLockState(portal, Portal.State.UNLOCKED, false));
		}
		else if (Portal.State.UNLOCKED.equals(portal.getState()))
		{
			result.add(new SetLockState(portal, Portal.State.LOCKED, true));
		}
		else
		{
			throw new MazeException("Invalid state: "+portal.getState());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	public Point getTile()
	{
		return tile;
	}

	public int getFacing()
	{
		return facing;
	}

	public void setTile(Point tile)
	{
		this.tile = tile;
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

		TogglePortalStateEvent that = (TogglePortalStateEvent)o;

		if (getFacing() != that.getFacing())
		{
			return false;
		}
		return getTile() != null ? getTile().equals(that.getTile()) : that.getTile() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getTile() != null ? getTile().hashCode() : 0;
		result = 31 * result + getFacing();
		return result;
	}
}

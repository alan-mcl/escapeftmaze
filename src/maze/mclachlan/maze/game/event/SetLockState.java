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

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.util.MazeException;

public class SetLockState extends MazeEvent
{
	private final LockOrTrap lockOrTrap;
	private final String state;

	/*-------------------------------------------------------------------------*/
	public SetLockState(
		LockOrTrap lockOrTrap,
		String state)
	{
		this.lockOrTrap = lockOrTrap;
		this.state = state;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		if (lockOrTrap.getLockState().equals(this.state))
		{
			return null;
		}

		List<MazeEvent> result = new ArrayList<>();

		lockOrTrap.setLockState(this.state);

		if (Portal.State.UNLOCKED.equals(this.state))
		{
			result.addAll(Database.getInstance().getMazeScript("_UNLOCK_").getEvents());
			result.add(new UiMessageEvent(StringUtil.getEventText("msg.unlocked")));
		}
		else if (Portal.State.LOCKED.equals(this.state) || Portal.State.WALL_LIKE.equals(this.state))
		{
			result.addAll(Database.getInstance().getMazeScript("_UNLOCK_").getEvents());
			result.add(new UiMessageEvent(StringUtil.getEventText("msg.locked")));
		}
		else
		{
			throw new MazeException("Invalid state: "+this.state);
		}

		return result;
	}
}

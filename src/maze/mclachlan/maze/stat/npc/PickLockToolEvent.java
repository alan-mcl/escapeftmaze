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

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.SetStateEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.util.MazeException;

public class PickLockToolEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private LockOrTrap lockOrTrap;
	private int tool;

	/*-------------------------------------------------------------------------*/
	public PickLockToolEvent(
		PlayerCharacter pc,
		LockOrTrap lockOrTrap,
		int tool)
	{
		this.pc = pc;
		this.lockOrTrap = lockOrTrap;
		this.tool = tool;
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
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		MazeScript script = Database.getInstance().getScript("_THIEF_TOOL_");
		result.addAll(script.getEvents());
		int pickLockResult = GameSys.getInstance().pickLock(pc, lockOrTrap, tool);

		switch (pickLockResult)
		{
			case Trap.DisarmResult.NOTHING:
				break;
			case Trap.DisarmResult.DISARMED:
				BitSet picked = lockOrTrap.getAlreadyLockPicked();
				picked.set(tool);

				if (picked.equals(lockOrTrap.getPickLockToolsRequired()))
				{
					// all required are picked
					Maze.getInstance().getUi().clearDialog();
					script = Database.getInstance().getScript("_UNLOCK_");
					result.addAll(script.getEvents());

					result.add(new UiMessageEvent(StringUtil.getEventText("msg.unlocked")));

					lockOrTrap.setLockState(Portal.State.UNLOCKED);

					result.add(new SetStateEvent(Maze.getInstance(), Maze.State.MOVEMENT));
				}
				break;

			case Trap.DisarmResult.SPRING_TRAP:

				result.add(new UiMessageEvent(StringUtil.getEventText("msg.oops")));
				Maze.getInstance().getUi().clearDialog();
				break;

			default:
				throw new MazeException("Invalid result: "+result);
		}

		return result;
	}
}

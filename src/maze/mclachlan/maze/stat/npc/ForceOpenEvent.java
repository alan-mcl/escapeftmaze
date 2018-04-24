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
import mclachlan.maze.game.event.SetStateEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.FatigueEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;

public class ForceOpenEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private LockOrTrap lockOrTrap;

	/*-------------------------------------------------------------------------*/
	public ForceOpenEvent(
		PlayerCharacter pc,
		LockOrTrap lockOrTrap)
	{
		this.pc = pc;
		this.lockOrTrap = lockOrTrap;
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

		int forcePortalResult = GameSys.getInstance().forcePortal(pc, lockOrTrap);

		CurMaxSub hp = pc.getHitPoints();

		switch (forcePortalResult)
		{
			case Portal.ForceResult.FAILED_NO_DAMAGE:
				result.addAll(Database.getInstance().getScript("_OUCH_").getEvents());
				result.add(new UiMessageEvent(StringUtil.getEventText("event.ouch")));
				break;

			case Portal.ForceResult.FAILED_DAMAGE:
				result.addAll(Database.getInstance().getScript("_OUCH_").getEvents());
				result.add(new UiMessageEvent(StringUtil.getEventText("event.ouch")));
				hp.incSub(lockOrTrap.getHitPointCostToForceLock());
				break;

			case Portal.ForceResult.SUCCESS:

				result.addAll(Database.getInstance().getScript("_FORCE_PORTAL_").getEvents());
				result.add(new UiMessageEvent(StringUtil.getEventText("msg.force.open.success")));

				result.add(
					new FatigueEvent(
						pc,
						pc,
						lockOrTrap.getHitPointCostToForceLock(),
						MagicSys.SpellEffectType.BLUDGEONING,
						MagicSys.SpellEffectSubType.NORMAL_DAMAGE));

				lockOrTrap.setLockState(Portal.State.UNLOCKED);
				result.add(new SetStateEvent(Maze.getInstance(), Maze.State.MOVEMENT));

				break;
			default:
				throw new MazeException("Invalid result: "+forcePortalResult);
		}

		return result;
	}
}

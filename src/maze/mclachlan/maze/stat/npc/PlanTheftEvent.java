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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.TheftCallback;
import mclachlan.maze.ui.diygui.TheftDialog;

/**
 *
 */
public class PlanTheftEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private Foe npc;

	/*-------------------------------------------------------------------------*/
	public PlanTheftEvent(
		Foe npc,
		PlayerCharacter pc)
	{
		this.pc = pc;
		this.npc = npc;
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
		if (npc.getAttitude() == NpcFaction.Attitude.FRIENDLY ||
			npc.getAttitude() == NpcFaction.Attitude.ALLIED)
		{
			// a friendly NPC means the party has a chance to pick an item to steal
			Maze.getInstance().getUi().showDialog(new TheftDialog(pc, npc,
				new TheftCallback()
				{
					@Override
					public void grabAndAttack(Item item, PlayerCharacter pc)
					{
						Maze.getInstance().appendEvents(new GrabAndAttackEvent(npc, pc, item));
					}

					@Override
					public void stealItem(Item item, PlayerCharacter pc)
					{
						Maze.getInstance().appendEvents(new TheftEvent(npc, pc, item));
					}
				}));
			return null;
		}
		else
		{
			// a neutral NPC means a random item is the target
			Item item = GameSys.getInstance().getRandomItemToSteal(npc);
			return getList(new TheftEvent(npc, pc, item));
		}
	}
}

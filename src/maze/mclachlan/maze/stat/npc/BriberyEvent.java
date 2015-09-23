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
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class BriberyEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private Foe npc;
	private int amount;

	/*-------------------------------------------------------------------------*/
	public BriberyEvent(
		Foe npc,
		PlayerCharacter pc,
		int amount)
	{
		this.pc = pc;
		this.npc = npc;
		this.amount = amount;
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
		// bribery happens
		Maze.getInstance().getParty().incGold(-amount);
		int total = GameSys.getInstance().bribeNpc(npc, pc, amount);

		if (total > 0)
		{
			return npc.getActionScript().successfulBribe(total);
		}
		else
		{
			return npc.getActionScript().failedBribe(total);
		}
	}
}

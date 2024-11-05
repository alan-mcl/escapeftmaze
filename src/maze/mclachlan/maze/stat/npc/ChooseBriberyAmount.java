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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.GetAmount;

/**
 *
 */
public class ChooseBriberyAmount extends MazeEvent
{
	private final PlayerCharacter pc;
	private final Foe npc;

	/*-------------------------------------------------------------------------*/
	public ChooseBriberyAmount(
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
		new GetAmount(
			(amount, user, userIndex) -> {
				Maze.getInstance().appendEvents(
					new BriberyEvent(npc, user, amount));
				return true;
			},
			pc,
			Maze.getInstance().getParty().getGold());

		return null;
	}
}

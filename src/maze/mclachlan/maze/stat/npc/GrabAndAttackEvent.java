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
import mclachlan.maze.stat.*;

/**
 *
 */
public class GrabAndAttackEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private Foe npc;
	private Item item;
	private int amount;

	/*-------------------------------------------------------------------------*/
	public GrabAndAttackEvent(
		Foe npc,
		PlayerCharacter pc,
		Item item)
	{
		this.pc = pc;
		this.npc = npc;
		this.item = item;
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
		// regardless of the result, the theft dialog is done.
		Maze.getInstance().getUi().clearDialog();

		if (item instanceof GoldPieces)
		{
			// generate the amount stolen now
			int amount = GameSys.getInstance().getAmountOfGoldStolen(npc, pc);
			item = new GoldPieces(amount);
		}

		return npc.getActionScript().grabAndAttack(pc, item);
	}
}

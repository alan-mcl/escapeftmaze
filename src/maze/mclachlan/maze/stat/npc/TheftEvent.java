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
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class TheftEvent extends MazeEvent
{
	private PlayerCharacter pc;
	private Foe npc;
	private Item item;
	private int amount;

	/*-------------------------------------------------------------------------*/
	public TheftEvent(
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
		// regardless of the result, this dialog is done.
		Maze.getInstance().getUi().clearDialog();

		// treat "nothing to steal" as an undetected failure
		if (item == null)
		{
			return npc.getActionScript().failedUndetectedTheft(pc, item);
		}

		if (item instanceof GoldPieces)
		{
			// generate the amount stolen now
			int amount = GameSys.getInstance().getAmountOfGoldStolen(npc, pc);
			item = new GoldPieces(amount);
		}

		int result = GameSys.getInstance().stealItem(npc, pc, item);

		if (result == Npc.TheftResult.SUCCESS)
		{
			return npc.getActionScript().successfulTheft(pc, item);
		}
		else if (result == Npc.TheftResult.FAILED_UNDETECTED)
		{
			return npc.getActionScript().failedUndetectedTheft(pc, item);
		}
		else if (result == Npc.TheftResult.FAILED_DETECTED)
		{
			return npc.getActionScript().failedDetectedTheft(pc, item);
		}
		else
		{
			throw new MazeException("invalid theft result: "+result);
		}
	}
}

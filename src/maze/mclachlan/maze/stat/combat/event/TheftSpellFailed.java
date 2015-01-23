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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.GoldPieces;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.Npc;

/**
 *
 */
public class TheftSpellFailed extends MazeEvent
{
	private PlayerCharacter pc;
	private Npc npc;
	private int strength;

	/*-------------------------------------------------------------------------*/
	public TheftSpellFailed(PlayerCharacter pc, Npc npc, int strength)
	{
		this.pc = pc;
		this.npc = npc;
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public Npc getNpc()
	{
		return npc;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Item item = GameSys.getInstance().getRandomItemToSteal(npc);
		if (item instanceof GoldPieces)
		{
			// generate the amount stolen now
			int amount = GameSys.getInstance().getAmountOfGoldStolen(npc, pc);
			item = new GoldPieces(amount);
		}
		return npc.getScript().failedDetectedTheft(pc, item);
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return "";
	}
}

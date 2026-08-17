/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;

/**
 * Headless UI that supplies player combat intentions and auto-takes loot.
 */
public class HarnessUi extends HeadlessUi
{
	private int facing;
	private int lootBaseCostTotal;

	public int getLootBaseCostTotal()
	{
		return lootBaseCostTotal;
	}

	public void resetLootBaseCostTotal()
	{
		lootBaseCostTotal = 0;
	}

	@Override
	public ActorActionIntention getCombatIntention(PlayerCharacter pc)
	{
		Maze maze = Maze.getInstance();
		Combat combat = maze.getCurrentCombat();
		if (combat == null)
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		return BasicPlayerCombatAi.getCombatIntention(pc, combat.getFoes(), combat);
	}

	@Override
	public void grantItems(List<Item> items)
	{
		if (items == null || items.isEmpty())
		{
			return;
		}

		PlayerParty party = Maze.getInstance().getParty();
		if (party == null)
		{
			return;
		}

		for (Item item : items)
		{
			lootBaseCostTotal += item.getBaseCost();
			boolean placed = false;
			for (PlayerCharacter pc : party.getPlayerCharacters())
			{
				if (pc.addInventoryItem(item))
				{
					placed = true;
					break;
				}
			}
			if (!placed)
			{
				Maze.getInstance().dropItemsOnCurrentTile(List.of(item));
			}
		}
	}

	@Override
	public int getFacing()
	{
		return facing;
	}

	public void setFacing(int facing)
	{
		this.facing = facing;
	}

	@Override
	public void setPlayerPos(java.awt.Point pos, int facing)
	{
		this.facing = facing;
	}
}

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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GiveItemToParty extends MazeEvent
{
	private Foe npc;
	private boolean inInventory;
	private PlayerCharacter pc;
	private Item item;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param npc
	 * 	The former owner of the item
	 * @param pc
	 * 	The new owner of the item
	 * @param item
	 * 	The item in question
	 * @param inInventory
	 * 	Set to true if this item should be the NPCs inventory (associated
	 * 	assertions will be run), or false if the NPC produces it from thin air
	 */
	public GiveItemToParty(Foe npc, PlayerCharacter pc, Item item, boolean inInventory)
	{
		this.pc = pc;
		this.item = item;
		this.npc = npc;
		this.inInventory = inInventory;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (inInventory)
		{
			npc.removeItem(item, true);
		}
		pc.addInventoryItem(item);
		return null;
	}
}

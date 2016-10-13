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
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class GiveItemToParty extends MazeEvent
{
	private UnifiedActor source, recipient;
	private boolean inInventory;
	private Item item;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param source
	 * 	The former owner of the item
	 * @param recipient
	 * 	The new owner of the item
	 * @param item
	 * 	The item in question
	 * @param inInventory
	 * 	Set to true if this item should be the NPCs inventory (associated
	 * 	assertions will be run), or false if the NPC produces it from thin air
	 */
	public GiveItemToParty(UnifiedActor source, UnifiedActor recipient, Item item, boolean inInventory)
	{
		this.recipient = recipient;
		this.item = item;
		this.source = source;
		this.inInventory = inInventory;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (inInventory)
		{
			source.removeItem(item, true);
		}
		recipient.addInventoryItem(item);
		return null;
	}
}

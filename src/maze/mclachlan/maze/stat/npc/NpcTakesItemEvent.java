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

import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Item;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.util.MazeException;
import java.util.*;

/**
 * Removes an item from a PC and gives it to the NPC.
 */
public class NpcTakesItemEvent extends MazeEvent
{
	private PlayerCharacter owner;
	private Item item;
	private Npc npc;

	/*-------------------------------------------------------------------------*/
	public NpcTakesItemEvent(PlayerCharacter owner, Item item, Npc npc)
	{
		this.owner = owner;
		this.item = item;
		this.npc = npc;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (!owner.getInventory().contains(item))
		{
			throw new MazeException("["+owner.getName()
				+"] inventory did not contain ["+item.getName()+"]");
		}

		owner.getInventory().remove(item);

		if (npc.getCurrentInventory() != null)
		{
			npc.getCurrentInventory().add(item);
		}

		return null;
	}
}

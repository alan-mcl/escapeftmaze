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
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;

/**
 *
 */
public class NpcInventoryTemplate
{
	private List<NpcInventoryTemplateRow> rows;

	/*-------------------------------------------------------------------------*/
	public NpcInventoryTemplate()
	{
		this(new ArrayList<NpcInventoryTemplateRow>());
	}

	/*-------------------------------------------------------------------------*/
	public NpcInventoryTemplate(List<NpcInventoryTemplateRow> rows)
	{
		this.rows = rows;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param itemName
	 * 	The name of the item
	 * @param chanceOfSpawning
	 * 	The % chance of this item spawning every 24 hrs
	 * @param partyLevelAppearing
	 * 	The party level at which this item begins spawning
	 * @param maxStocked
	 * 	The maximum nr of copies of this item that the NPC will stock
	 * @param chanceOfVanishing
	 * 	The % chance of this item vanishing every 24 hrs
	 * @param stackSize
	 * 	The number of items stacked, if applicable.
	 */
	public void add(
		String itemName, 
		int chanceOfSpawning, 
		int partyLevelAppearing,
		int maxStocked,
		int chanceOfVanishing,
		Dice stackSize)
	{
		this.rows.add(
			new NpcInventoryTemplateRowItem(
				itemName,
				chanceOfSpawning,
				partyLevelAppearing,
				maxStocked,
				chanceOfVanishing,
				stackSize));
	}

	/*-------------------------------------------------------------------------*/
	public void add(NpcInventoryTemplateRow row)
	{
		this.rows.add(row);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param currentInventory
	 * 	The NPCs current inventory
	 * @return
	 * 	The new inventory of the NPC
	 */
	public List<Item> update(List<Item> currentInventory)
	{
		// first remove items from the current inventory
		Iterator<Item> i = currentInventory.iterator();
		while(i.hasNext())
		{
			Item item = i.next();
			int chanceOfVanishing = getChanceOfVanishing(item);

			if (Dice.d100.roll("npc inventory vanishing") <= chanceOfVanishing)
			{
				i.remove();
			}
		}

		// now, spawn new items
		int clvl = Maze.getInstance().getParty().getPartyLevel();
		for (NpcInventoryTemplateRow row : rows)
		{
			List<Item> spawned = row.spawnNewItems(clvl, currentInventory);

			currentInventory.addAll(spawned);
		}

		// limit inventory size to keep NPC inventories from growing endlessly
		int maxSize = currentInventory.size()/2;
		if (maxSize > rows.size())
		{
			int nrToDelete = maxSize - rows.size();
			for (int j=0; j<nrToDelete; j++)
			{
				int n = new Dice(1, currentInventory.size(), -1).roll("removing inventory item");
				currentInventory.remove(n);
			}
		}

		return currentInventory;
	}


	/*-------------------------------------------------------------------------*/
	private int getChanceOfVanishing(Item item)
	{
		for (NpcInventoryTemplateRow row : rows)
		{
			if (row.contains(item))
			{
				// use the configured chance of vanishing
				return row.getChanceOfVanishing();
			}
		}

		// not an item in this template (perhaps sold to us by the PCs)
		// default to a 2% chance of vanishing
		return 2;
	}

	/*-------------------------------------------------------------------------*/
	public List<NpcInventoryTemplateRow> getRows()
	{
		return rows;
	}
}

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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class NpcInventoryTemplate
{
	List<NpcInventoryTemplateRow> rows;

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
			new NpcInventoryTemplateRow(
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

			if (Dice.d100.roll() <= chanceOfVanishing)
			{
				i.remove();
			}
		}

		// now, spawn new items
		int clvl = Maze.getInstance().getParty().getPartyLevel();
		for (NpcInventoryTemplateRow t : rows)
		{
			if (getNumInStock(t.itemName, currentInventory) < t.maxStocked)
			{
				if (clvl >= t.partyLevelAppearing && Dice.d100.roll() <= t.chanceOfSpawning)
				{
					ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(t.itemName);
					Item newItem = null;

					if (itemTemplate.getMaxItemsPerStack() > 0 && t.stackSize != null)
					{
						newItem = itemTemplate.create(t.stackSize.roll());
					}
					else
					{
						newItem = itemTemplate.create();
					}

					// set vendor items to always identified
					newItem.setIdentificationState(Item.IdentificationState.IDENTIFIED);
					currentInventory.add(newItem);
				}
			}
		}

		return currentInventory;
	}

	/*-------------------------------------------------------------------------*/
	int getNumInStock(String itemName, List<Item> inv)
	{
		int result = 0;

		for (Item i : inv)
		{
			if (i.getName().equals(itemName))
			{
				result++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	int getChanceOfVanishing(Item i)
	{
		for (NpcInventoryTemplateRow t : rows)
		{
			if (i.getName().equals(t.itemName))
			{
				// use the configured chance of vanishing
				return t.chanceOfVanishing;
			}
		}

		// not an item in this template (perhaps sold to us by the PCs)
		// default to a 10% chance of vanishing
		return 10;
	}

	/*-------------------------------------------------------------------------*/
	public List<NpcInventoryTemplateRow> getRows()
	{
		return rows;
	}
}

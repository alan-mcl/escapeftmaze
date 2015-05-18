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

package mclachlan.maze.map;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class SingleItemLootEntry implements ILootEntry
{
	private String itemName;

	/*-------------------------------------------------------------------------*/
	public SingleItemLootEntry(String itemName)
	{
		this.itemName = itemName;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return itemName;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		itemName = name;
	}

	/*-------------------------------------------------------------------------*/
	public Item generate()
	{
		return Database.getInstance().getItemTemplate(itemName).create();
	}

	/*-------------------------------------------------------------------------*/
	public List<LootEntryRow> getContents()
	{
		ArrayList<LootEntryRow> result = new ArrayList<LootEntryRow>();
		ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(itemName);
		Dice quantity = Dice.d1;

		if (itemTemplate.getMaxItemsPerStack() > 1)
		{
			switch (itemTemplate.getType())
			{
				case ItemTemplate.Type.AMMUNITION: quantity = new Dice(1,20,20);
					break;
				case ItemTemplate.Type.POTION: quantity = new Dice(2,3,-1);
								break;
				case ItemTemplate.Type.BOMB: quantity = new Dice(2,3,-1);
								break;
				case ItemTemplate.Type.MONEY: quantity = new Dice(1,20,5);
								break;
				case ItemTemplate.Type.SUPPLIES: quantity = new Dice(1,20,5);
								break;
				case ItemTemplate.Type.DRINK: quantity = new Dice(2,3,-1);
								break;
				case ItemTemplate.Type.FOOD: quantity = new Dice(2,3,-1);
								break;
				case ItemTemplate.Type.POWDER: quantity = new Dice(2,3,-1);
								break;
				case ItemTemplate.Type.THROWN_WEAPON: quantity = new Dice(1,15,5);
								break;
				default:
					quantity = Dice.d1;
			}
		}

		result.add(new LootEntryRow(itemName, quantity));
		return result;
	}
}

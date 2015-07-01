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
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 * Represents an row in a table of random loot.
 */
public class LootEntry implements ILootEntry
{
	private String name;
	private PercentageTable<LootEntryRow> contains;

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new composite loot entry
	 */
	public LootEntry(String name, PercentageTable<LootEntryRow> contains)
	{
		this.name = name;
		this.contains = contains;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<LootEntryRow> getContains()
	{
		return contains;
	}

	/*-------------------------------------------------------------------------*/
	public void setContains(PercentageTable<LootEntryRow> contains)
	{
		this.contains = contains;
	}

	/*-------------------------------------------------------------------------*/
	public Item generate()
	{
		LootEntryRow row = contains.getRandomItem();
		String itemName = row.getItemName();
		Item item;

		// item entry
		ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(itemName);

		if (row.getQuantity().getMaxPossible() > itemTemplate.getMaxItemsPerStack())
		{
			throw new MazeException("Invalid stack size: "+itemName+", "+row.getQuantity());
		}

		item = itemTemplate.create(row.getQuantity().roll());

		if (item.getType() == ItemTemplate.Type.MONEY || item.getType() == ItemTemplate.Type.SUPPLIES)
		{
			// don't generate amounts that will convert to zero
			while (item.applyConversionRate() == 0)
			{
				item.getStack().incCurrent(1);
			}
		}

		return item;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PercentageTable<LootEntryRow> getPercentageTable()
	{
		return contains;
	}

	/*-------------------------------------------------------------------------*/
	public List<LootEntryRow> getContents()
	{
		return contains.getItems();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	spawn a list of items, unsorted, from the given list of LootEntries
	 */
	public static List<Item> generate(List<ILootEntry> entries)
	{
		List<Item> result = new ArrayList<Item>();

		for (ILootEntry t : entries)
		{
			Item item = t.generate();
			result.add(item);
		}

		return result;
	}
}

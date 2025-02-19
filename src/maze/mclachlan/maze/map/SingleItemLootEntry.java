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
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class SingleItemLootEntry implements ILootEntry
{
	private String name;

	public SingleItemLootEntry()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SingleItemLootEntry(String itemName)
	{
		this.name = itemName;
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
	public Item generate()
	{
		return Database.getInstance().getItemTemplate(name).create();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PercentageTable<LootEntryRow> getPercentageTable()
	{
		return new PercentageTable<LootEntryRow>(getContents(), Arrays.asList(new Double[]{1.0}));
	}

	/*-------------------------------------------------------------------------*/
	public List<LootEntryRow> getContents()
	{
		ArrayList<LootEntryRow> result = new ArrayList<LootEntryRow>();
		ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(name);
		Dice quantity = Dice.d1;

		if (itemTemplate.getMaxItemsPerStack() > 1)
		{
			quantity = switch (itemTemplate.getType())
				{
					case ItemTemplate.Type.AMMUNITION -> new Dice(1, 20, 20);
					case ItemTemplate.Type.POTION -> new Dice(2, 3, -1);
					case ItemTemplate.Type.BOMB -> new Dice(2, 3, -1);
					case ItemTemplate.Type.MONEY -> new Dice(1, 20, 5);
					case ItemTemplate.Type.SUPPLIES -> new Dice(1, 20, 5);
					case ItemTemplate.Type.DRINK -> new Dice(2, 3, -1);
					case ItemTemplate.Type.FOOD -> new Dice(2, 3, -1);
					case ItemTemplate.Type.POWDER -> new Dice(2, 3, -1);
					case ItemTemplate.Type.THROWN_WEAPON -> new Dice(1, 15, 5);
					default -> Dice.d1;
				};
		}

		result.add(new LootEntryRow(name, quantity));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean contains(String itemName)
	{
		return this.name.equals(itemName);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		SingleItemLootEntry that = (SingleItemLootEntry)o;

		return name != null ? name.equals(that.name) : that.name == null;
	}

	@Override
	public int hashCode()
	{
		return name != null ? name.hashCode() : 0;
	}
}

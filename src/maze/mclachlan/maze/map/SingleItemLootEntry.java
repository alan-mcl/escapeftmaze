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

/**
 *
 */
public class SingleItemLootEntry implements ILootEntry
{
	String itemName;

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
		result.add(new LootEntryRow(itemName, Dice.d1));
		return result;
	}
}

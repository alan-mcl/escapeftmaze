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

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class LootTable extends DataObject
{
	private String name;
	private GroupOfPossibilities<ILootEntry> lootEntries;

	public LootTable()
	{
	}

	/*-------------------------------------------------------------------------*/
	public LootTable(String name, GroupOfPossibilities<ILootEntry> lootEntries)
	{
		this.lootEntries = lootEntries;
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities<ILootEntry> getLootEntries()
	{
		return lootEntries;
	}

	public String getName()
	{
		return name;
	}

	public void setLootEntries(GroupOfPossibilities<ILootEntry> lootEntries)
	{
		this.lootEntries = lootEntries;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getMaxDroppableGold()
	{
		int result = 0;

		for (ILootEntry entry : lootEntries.getPossibilities())
		{
			for (LootEntryRow row : entry.getContents())
			{
				ItemTemplate it = Database.getInstance().getItemTemplate(row.getItemName());
				if (it.getType() == ItemTemplate.Type.MONEY)
				{
					result += (int)(row.getQuantity().getAverage() * it.getConversionRate());
				}
			}
		}

		return result;
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

		LootTable lootTable = (LootTable)o;

		if (getName() != null ? !getName().equals(lootTable.getName()) : lootTable.getName() != null)
		{
			return false;
		}
		return getLootEntries() != null ? getLootEntries().equals(lootTable.getLootEntries()) : lootTable.getLootEntries() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getLootEntries() != null ? getLootEntries().hashCode() : 0);
		return result;
	}
}

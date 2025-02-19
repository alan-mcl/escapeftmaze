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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 * Creates an item in the targets possession
 */
public class CreateItemSpellResult extends SpellResult
{
	/**
	 * Loot table from which to calculate the item
	 */
	private String lootTable;

	/**
	 * True if the item should be equipped, false if a grant items event should
	 * be generated
	 */
	private boolean equipItems;

	public CreateItemSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public CreateItemSpellResult(String lootTable, boolean equipItems)
	{
		this.lootTable = lootTable;
		this.equipItems = equipItems;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		LootTable table = Database.getInstance().getLootTable(lootTable);
		List<ILootEntry> lootEntries = table.getLootEntries().getRandom();
		List<Item> items = new ArrayList<Item>();

		for (ILootEntry lootEntry : lootEntries)
		{
			Item item = lootEntry.generate();
			item.setIdentificationState(Item.IdentificationState.IDENTIFIED);
			item.setCursedState(Item.CursedState.DISCOVERED);

			items.add(item);
		}


		if (equipItems)
		{
			List<Item> dropped = new ArrayList<Item>();
			for (Item item : items)
			{
				if (!target.addItemSmartly(item))
				{
					dropped.add(item);
				}
			}
			Maze.getInstance().dropItemsOnCurrentTile(dropped);
		}
		else
		{
			result.add(new GrantItemsEvent(items));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getLootTable()
	{
		return lootTable;
	}

	/*-------------------------------------------------------------------------*/

	public boolean isEquipItems()
	{
		return equipItems;
	}

	public void setLootTable(String lootTable)
	{
		this.lootTable = lootTable;
	}

	public void setEquipItems(boolean equipItems)
	{
		this.equipItems = equipItems;
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
		if (!super.equals(o))
		{
			return false;
		}

		CreateItemSpellResult that = (CreateItemSpellResult)o;

		if (isEquipItems() != that.isEquipItems())
		{
			return false;
		}
		return getLootTable() != null ? getLootTable().equals(that.getLootTable()) : that.getLootTable() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getLootTable() != null ? getLootTable().hashCode() : 0);
		result = 31 * result + (isEquipItems() ? 1 : 0);
		return result;
	}
}

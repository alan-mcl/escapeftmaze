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
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class NpcInventoryTemplateRowLootEntry extends NpcInventoryTemplateRow
{
	private String lootEntry;
	private Dice itemsToSpawn;

	public NpcInventoryTemplateRowLootEntry()
	{
	}

	/*-------------------------------------------------------------------------*/
	public NpcInventoryTemplateRowLootEntry(
		Type type,
		int chanceOfSpawning,
		int partyLevelAppearing,
		int maxStocked,
		int chanceOfVanishing,
		String lootEntryName,
		Dice itemsToSpawn)
	{
		super(type, chanceOfSpawning, partyLevelAppearing, maxStocked, chanceOfVanishing);
		this.lootEntry = lootEntryName;
		this.itemsToSpawn = itemsToSpawn;
	}

	/*-------------------------------------------------------------------------*/
	public String getLootEntry()
	{
		return lootEntry;
	}

	public void setLootEntry(String lootEntry)
	{
		this.lootEntry = lootEntry;
	}

	public Dice getItemsToSpawn()
	{
		return itemsToSpawn;
	}

	public void setItemsToSpawn(Dice itemsToSpawn)
	{
		this.itemsToSpawn = itemsToSpawn;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean contains(Item item)
	{
		ILootEntry le = Database.getInstance().getLootEntry(lootEntry);
		return le.contains(item.getName());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<Item> spawnNewItems(int clvl, List<Item> currentInventory)
	{
		List<Item> result = new ArrayList<Item>();

		ILootEntry le = Database.getInstance().getLootEntry(lootEntry);

		int count = itemsToSpawn.roll("npc items to spawn");

		for (int i=0; i<count; i++)
		{
			Item item = le.generate();

			if (getNumInStock(item.getName(), currentInventory) < this.getMaxStocked())
			{
				if (clvl >= this.getPartyLevelAppearing() &&
					Dice.d100.roll("npc chance of spawning 2") <= this.getChanceOfSpawning())
				{
					ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(item.getName());
					Item newItem = null;

					if (itemTemplate.getMaxItemsPerStack() > 0)
					{
						newItem = itemTemplate.create(getDefaultStackSize(item.getName()).roll("npc inventory stack size 2"));
					}
					else
					{
						newItem = itemTemplate.create();
					}

					// set vendor items to always identified
					newItem.setIdentificationState(Item.IdentificationState.IDENTIFIED);
					result.add(newItem);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int compareTo(NpcInventoryTemplateRow r2)
	{
		return this.getLootEntry().compareTo(((NpcInventoryTemplateRowLootEntry)r2).getLootEntry());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();

		s.append("loot entry - "+getLootEntry()+" ");
		s.append(getType());
		s.append(" [");
		s.append("spawn="+getChanceOfSpawning()+"%, ");
		s.append("clvl="+getPartyLevelAppearing()+", ");
		s.append("max="+getMaxStocked()+", ");
		s.append("vanish="+getChanceOfVanishing()+"%, ");
		s.append("#toSpawn="+ V1Dice.toString(getItemsToSpawn()));
		s.append("]");

		if (s.length() > 100)
		{
			return s.substring(0, 97)+"...";
		}
		else
		{
			return s.toString();
		}
	}
}

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
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

public abstract class NpcInventoryTemplateRow
{
	private int chanceOfSpawning;
	private int partyLevelAppearing;
	private int maxStocked;
	private int chanceOfVanishing;

	public NpcInventoryTemplateRow()
	{
	}

	/*-------------------------------------------------------------------------*/
	protected NpcInventoryTemplateRow(int chanceOfSpawning,
		int partyLevelAppearing,
		int maxStocked, int chanceOfVanishing)
	{
		this.chanceOfSpawning = chanceOfSpawning;
		this.partyLevelAppearing = partyLevelAppearing;
		this.maxStocked = maxStocked;
		this.chanceOfVanishing = chanceOfVanishing;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if this row can spawn the given item
	 */
	public abstract boolean contains(Item item);

	/*-------------------------------------------------------------------------*/
	/**
	 *
	 * @param clvl
	 * 	The average party level
	 * @param currentInventory
	 * @return
	 * 	A list of newly spawned items
	 */
	public abstract List<Item> spawnNewItems(int clvl,
		List<Item> currentInventory);

	/*-------------------------------------------------------------------------*/
	public int getChanceOfSpawning()
	{
		return chanceOfSpawning;
	}

	public void setChanceOfSpawning(int chanceOfSpawning)
	{
		this.chanceOfSpawning = chanceOfSpawning;
	}

	public int getPartyLevelAppearing()
	{
		return partyLevelAppearing;
	}

	public void setPartyLevelAppearing(int partyLevelAppearing)
	{
		this.partyLevelAppearing = partyLevelAppearing;
	}

	public int getMaxStocked()
	{
		return maxStocked;
	}

	public void setMaxStocked(int maxStocked)
	{
		this.maxStocked = maxStocked;
	}

	public int getChanceOfVanishing()
	{
		return chanceOfVanishing;
	}

	public void setChanceOfVanishing(int chanceOfVanishing)
	{
		this.chanceOfVanishing = chanceOfVanishing;
	}

	/*-------------------------------------------------------------------------*/
	protected int getNumInStock(String itemName, List<Item> inv)
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
	public Dice getDefaultStackSize(String itemName)
	{
		ItemTemplate template = Database.getInstance().getItemTemplate(itemName);
		Dice result;
		if (template.getType() == ItemTemplate.Type.AMMUNITION)
		{
			result = new Dice(1,25,25);
		}
		else if (template.getType() == ItemTemplate.Type.THROWN_WEAPON)
		{
			result = new Dice(1,10,5);
		}
		else if (template.getType() == ItemTemplate.Type.BOMB ||
			template.getType() == ItemTemplate.Type.POTION ||
			template.getType() == ItemTemplate.Type.POWDER ||
			template.getType() == ItemTemplate.Type.FOOD)
		{
			result = new Dice(1,3,1);
		}
		else
		{
			result = new Dice(1,1,0);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public abstract int compareTo(NpcInventoryTemplateRow r2);

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

		NpcInventoryTemplateRow that = (NpcInventoryTemplateRow)o;

		if (getChanceOfSpawning() != that.getChanceOfSpawning())
		{
			return false;
		}
		if (getPartyLevelAppearing() != that.getPartyLevelAppearing())
		{
			return false;
		}
		if (getMaxStocked() != that.getMaxStocked())
		{
			return false;
		}
		return getChanceOfVanishing() == that.getChanceOfVanishing();
	}

	@Override
	public int hashCode()
	{
		int result = getChanceOfSpawning();
		result = 31 * result + getPartyLevelAppearing();
		result = 31 * result + getMaxStocked();
		result = 31 * result + getChanceOfVanishing();
		return result;
	}
}

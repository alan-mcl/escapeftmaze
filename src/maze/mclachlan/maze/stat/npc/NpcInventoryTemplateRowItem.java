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
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class NpcInventoryTemplateRowItem extends NpcInventoryTemplateRow
{
	private String itemName;
	private Dice stackSize;

	/*-------------------------------------------------------------------------*/
	public NpcInventoryTemplateRowItem(
		String itemName,
		int chanceOfSpawning,
		int partyLevelAppearing,
		int maxStocked,
		int chanceOfVanishing,
		Dice stackSize)
	{
		super(chanceOfSpawning, partyLevelAppearing, maxStocked, chanceOfVanishing);
		this.itemName = itemName;
		this.stackSize = stackSize;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean contains(Item item)
	{
		return item.getName().equals(this.getItemName());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<Item> spawnNewItems(int clvl, List<Item> currentInventory)
	{
		List<Item> result = new ArrayList<Item>();

		if (getNumInStock(this.getItemName(), currentInventory) < this.getMaxStocked())
		{
			if (clvl >= this.getPartyLevelAppearing() &&
				Dice.d100.roll() <= this.getChanceOfSpawning())
			{
				ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(this.getItemName());
				Item newItem = null;

				if (itemTemplate.getMaxItemsPerStack() > 0 && this.getStackSize() != null)
				{
					newItem = itemTemplate.create(this.getStackSize().roll());
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

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int compareTo(NpcInventoryTemplateRow r2)
	{
		return this.getItemName().compareTo(((NpcInventoryTemplateRowItem)r2).getItemName());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();

		s.append("item - "+getItemName());
		s.append(" [");
		s.append("spawn="+getChanceOfSpawning()+"%, ");
		s.append("clvl="+getPartyLevelAppearing()+", ");
		s.append("max="+getMaxStocked()+", ");
		s.append("vanish="+getChanceOfVanishing()+"%, ");
		s.append("stack="+ V1Dice.toString(getStackSize()));
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

	/*-------------------------------------------------------------------------*/
	public String getItemName()
	{
		return itemName;
	}

	public Dice getStackSize()
	{
		return stackSize;
	}

	/*-------------------------------------------------------------------------*/

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	public void setStackSize(Dice stackSize)
	{
		this.stackSize = stackSize;
	}
}

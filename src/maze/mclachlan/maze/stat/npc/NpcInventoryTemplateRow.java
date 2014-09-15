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

import mclachlan.maze.stat.Dice;

/**
 *
 */
public class NpcInventoryTemplateRow
{
	String itemName;
	int chanceOfSpawning;
	int partyLevelAppearing;
	int maxStocked;
	int chanceOfVanishing;
	Dice stackSize;

	/*-------------------------------------------------------------------------*/
	public NpcInventoryTemplateRow(
		String itemName,
		int chanceOfSpawning,
		int partyLevelAppearing,
		int maxStocked,
		int chanceOfVanishing,
		Dice stackSize)
	{
		this.chanceOfSpawning = chanceOfSpawning;
		this.itemName = itemName;
		this.partyLevelAppearing = partyLevelAppearing;
		this.maxStocked = maxStocked;
		this.chanceOfVanishing = chanceOfVanishing;
		this.stackSize = stackSize;
	}

	/*-------------------------------------------------------------------------*/
	public int getChanceOfSpawning()
	{
		return chanceOfSpawning;
	}

	public int getChanceOfVanishing()
	{
		return chanceOfVanishing;
	}

	public String getItemName()
	{
		return itemName;
	}

	public int getMaxStocked()
	{
		return maxStocked;
	}

	public int getPartyLevelAppearing()
	{
		return partyLevelAppearing;
	}

	public Dice getStackSize()
	{
		return stackSize;
	}

	/*-------------------------------------------------------------------------*/

	public void setChanceOfSpawning(int chanceOfSpawning)
	{
		this.chanceOfSpawning = chanceOfSpawning;
	}

	public void setChanceOfVanishing(int chanceOfVanishing)
	{
		this.chanceOfVanishing = chanceOfVanishing;
	}

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	public void setMaxStocked(int maxStocked)
	{
		this.maxStocked = maxStocked;
	}

	public void setPartyLevelAppearing(int partyLevelAppearing)
	{
		this.partyLevelAppearing = partyLevelAppearing;
	}

	public void setStackSize(Dice stackSize)
	{
		this.stackSize = stackSize;
	}
}

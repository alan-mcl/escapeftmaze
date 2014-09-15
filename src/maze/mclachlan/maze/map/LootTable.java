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

import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class LootTable
{
	String name;
	GroupOfPossibilities<ILootEntry> lootEntries;

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
}

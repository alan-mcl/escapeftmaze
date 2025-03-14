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

package mclachlan.maze.map.script;

import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.Item;
import java.util.*;

/**
 * Grants the player some loot.
 */
public class Loot extends TileScript
{
	private String lootTable;

	public Loot()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Loot(String lootTable)
	{
		this.lootTable = lootTable;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		GroupOfPossibilities<ILootEntry> loot =
			Database.getInstance().getLootTable(this.lootTable).getLootEntries();
		if (loot == null)
		{
			// no loot here
			return null;
		}
		java.util.List<ILootEntry> entries = loot.getRandom();
		java.util.List<Item> items = LootEntry.generate(entries);

		return getLootingEvents(items);
	}

	/*-------------------------------------------------------------------------*/
	public String getLootTable()
	{
		return lootTable;
	}

	public void setLootTable(String lootTable)
	{
		this.lootTable = lootTable;
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

		Loot loot = (Loot)o;

		return getLootTable() != null ? getLootTable().equals(loot.getLootTable()) : loot.getLootTable() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getLootTable() != null ? getLootTable().hashCode() : 0);
		return result;
	}
}

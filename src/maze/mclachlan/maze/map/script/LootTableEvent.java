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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.TileScript;
import java.util.*;

/**
 * Grants items from a given loot table
 */
public class LootTableEvent extends MazeEvent
{
	LootTable lootTable;

	/*-------------------------------------------------------------------------*/
	public LootTableEvent(LootTable lootTable)
	{
		this.lootTable = lootTable;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		java.util.List<ILootEntry> entries = lootTable.getLootEntries().getRandom();
		return TileScript.getLootingEvents(LootEntry.generate(entries));
	}

	/*-------------------------------------------------------------------------*/
	public LootTable getLootTable()
	{
		return lootTable;
	}
}

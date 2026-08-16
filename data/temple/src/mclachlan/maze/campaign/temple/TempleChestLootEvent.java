/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.campaign.temple;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.LootTableEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.PercentageTable;

/**
 * Chest loot for generated temple floors: roll the table like {@link LootTableEvent},
 * but guarantee at least one {@link ILootEntry} so procedural chests are never empty.
 */
public final class TempleChestLootEvent extends MazeEvent
{
	private String lootTableName;

	public TempleChestLootEvent()
	{
	}

	public TempleChestLootEvent(String lootTableName)
	{
		this.lootTableName = lootTableName;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		LootTable table = mclachlan.maze.data.Database.getInstance().getLootTable(lootTableName);
		if (table == null || table.getLootEntries() == null)
		{
			return null;
		}

		GroupOfPossibilities<ILootEntry> gop = table.getLootEntries();
		List<ILootEntry> entries = new ArrayList<>(gop.getRandom());
		if (entries.isEmpty())
		{
			entries.add(pickOne(gop));
		}
		return TileScript.getLootingEvents(LootEntry.generate(entries));
	}

	/*-------------------------------------------------------------------------*/
	static ILootEntry pickOne(GroupOfPossibilities<ILootEntry> gop)
	{
		List<ILootEntry> items = gop.getPossibilities();
		if (items.isEmpty())
		{
			return null;
		}
		List<Integer> weights = gop.getPercentages();
		PercentageTable<ILootEntry> table = new PercentageTable<>(false);
		for (int i = 0; i < items.size(); i++)
		{
			table.add(items.get(i), weights.get(i));
		}
		ILootEntry picked = table.getRandomItem();
		if (picked != null)
		{
			return picked;
		}
		return items.get(new Dice(1, items.size(), -1).roll("temple chest loot fallback") - 1);
	}

	public String getLootTableName()
	{
		return lootTableName;
	}

	public void setLootTableName(String lootTableName)
	{
		this.lootTableName = lootTableName;
	}
}

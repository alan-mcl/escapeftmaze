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

package mclachlan.maze.arena;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class HiddenLoot extends TileScript
{
	public static final String MAZE_VAR = "arena.hiddenloot";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handlePlayerAction(Maze maze, Point tile, int facing, int playerAction)
	{
		if (MazeVariables.getBoolean(MAZE_VAR))
		{
			return null;
		}

		GroupOfPossibilities<ILootEntry> lootTable =
			Database.getInstance().getLootTable("stick man loot").getLootEntries();
		java.util.List<ILootEntry> entries = lootTable.getRandom();
		List<MazeEvent> result = getLootingEvents(LootEntry.generate(entries));

		result.add(0, new FlavourTextEvent(
				"There's some stuff hidden here!",
				MazeEvent.Delay.WAIT_ON_CLICK,
				true));

		return result;
	}
}

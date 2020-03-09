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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootEntryRow;

/**
 *
 */
public class UpdateLoot
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		int count = 0;

		Map<String,LootEntry> m = db.getLootEntries();

		for (String s : m.keySet())
		{
			LootEntry item = m.get(s);

			for (LootEntryRow ler : item.getContents())
			{
//				if (ler.getItemName())
			}

		}
		
		saver.saveLootEntries(m);
		System.out.println("count = [" + count + "]");
	}
}

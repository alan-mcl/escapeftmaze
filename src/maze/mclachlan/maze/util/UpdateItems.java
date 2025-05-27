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
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class UpdateItems
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V2Loader loader = new V2Loader();
		V2Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		int count = 0;

		Map<String,ItemTemplate> items = db.getItemTemplates();

		for (String s : items.keySet())
		{
			ItemTemplate item = items.get(s);

			if (item.getUsableByRace().contains("Human"))
			{
				item.getUsableByRace().add("Pasht");
				count++;
			}

		}
		
		saver.saveItemTemplates(items);
		System.out.println("count = [" + count + "]");
	}
}

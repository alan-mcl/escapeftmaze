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
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class Report
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		Map<String,ItemTemplate> items = db.getItemTemplates();

		for (String s : items.keySet())
		{
			ItemTemplate item = items.get(s);

			String name = item.getName();
			String type = ItemTemplate.Type.describe(item.getType());
			int cost = item.getBaseCost();

			System.out.println(name+","+type+","+cost);
		}
	}
}

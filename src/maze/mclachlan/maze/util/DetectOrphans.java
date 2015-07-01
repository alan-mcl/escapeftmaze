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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.Loot;
import mclachlan.maze.map.script.LootTableEvent;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class DetectOrphans
{
	private static V1Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		doLootTables(db);
		doLootEntries(db);
	}

	/*-------------------------------------------------------------------------*/
	private static void doLootEntries(Database db)
	{
		System.out.println("LOOT ENTRIES");
		Map<String,LootEntry> lootEntries = db.getLootEntries();

		List<String> used = new ArrayList<String>();

		// get from loot tables
		Map<String, LootTable> lootTables = db.getLootTables();
		for (LootTable lt : lootTables.values())
		{
			for (ILootEntry le : lt.getLootEntries().getPossibilities())
			{
				used.add(le.getName());
			}
		}

		//find orphans
		int count=0;
		for (LootEntry le : lootEntries.values())
		{
			if (!used.contains(le.getName()))
			{
				System.out.println(le.getName());
				count++;
			}
		}
		System.out.println("~~~ "+count+" unused loot entries");
		System.out.println();
	}

	/*-------------------------------------------------------------------------*/
	private static void doLootTables(Database db)
	{
		System.out.println("LOOT TABLES");
		Map<String,LootTable> lootTables = db.getLootTables();

		List<String> used = new ArrayList<String>();

		// get from foes
		Map<String,FoeTemplate> foeTemplates = db.getFoeTemplates();
		for (FoeTemplate ft : foeTemplates.values())
		{
			used.add(ft.getLoot().getName());
		}

		// get from maze scripts
		Map<String,MazeScript> mazeScripts = db.getMazeScripts();
		for (MazeScript ms : mazeScripts.values())
		{
			for (MazeEvent me : ms.getEvents())
			{
				if (me instanceof LootTableEvent)
				{
					used.add(((LootTableEvent)me).getLootTable().getName());
				}
			}
		}

		// get from item templates
		Map<String,ItemTemplate> itemTemplates = db.getItemTemplates();
		for (ItemTemplate it : itemTemplates.values())
		{
			if (it.getDisassemblyLootTable() != null)
			{
				used.add(it.getDisassemblyLootTable());
			}
		}

		// get from zones
		List<String> zones = db.getZoneNames();

		for (String s : zones)
		{
			Zone z = db.getZone(s);

			Tile[][] tiles = z.getTiles();
			for (int i = 0, tilesLength = tiles.length; i < tilesLength; i++)
			{
				Tile[] x = tiles[i];
				for (int j = 0, xLength = x.length; j < xLength; j++)
				{
					Tile y = x[j];
					if (y.getScripts() != null && !y.getScripts().isEmpty())
					{
						for (TileScript ms : y.getScripts())
						{
							if (ms instanceof Chest)
							{
								TileScript chestContents = ((Chest)ms).getChestContents();
								if (chestContents instanceof Loot)
								{
									used.add(((Loot)chestContents).getLootTable());
								}
							}
							else if (ms instanceof Loot)
							{
								used.add(((Loot)ms).getLootTable());							}
						}
					}
				}
			}
		}

		Collections.sort(used);

		// find orpans
		int count=0;
		for (LootTable lt : lootTables.values())
		{
			if (!used.contains(lt.getName()))
			{
				System.out.println(lt.getName());
				count++;
			}
		}

		System.out.println("~~~ "+count+" unused loot tables");
		System.out.println();
	}


}
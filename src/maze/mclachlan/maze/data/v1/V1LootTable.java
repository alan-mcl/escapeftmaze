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

package mclachlan.maze.data.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.SingleItemLootEntry;
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class V1LootTable
{
	static final String SEP = "/";
	static final String ITEM_FLAG = "I~";

	static V1GroupOfPossibilties<ILootEntry> rows = new V1GroupOfPossibilties<ILootEntry>()
	{
		public ILootEntry typeFromString(String s)
		{
			if (s.startsWith(ITEM_FLAG))
			{
				return new SingleItemLootEntry(s.substring(ITEM_FLAG.length()));
			}
			else
			{
				return Database.getInstance().getLootEntry(s);
			}
		}

		public String typeToString(ILootEntry lootEntry)
		{
			if (lootEntry instanceof SingleItemLootEntry)
			{
				return ITEM_FLAG+lootEntry.getName();
			}
			else
			{
				return lootEntry.getName();
			}
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, LootTable> load(BufferedReader reader) throws Exception
	{
		Map <String, LootTable> result = new HashMap<String, LootTable>();

		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			LootTable g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, LootTable> map) throws Exception
	{
		for (String name : map.keySet())
		{
			LootTable g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(LootTable obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != LootTable.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("lootEntries=");
			b.append(rows.toString(obj.getLootEntries()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static LootTable fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom LootTable impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (LootTable)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			GroupOfPossibilities<ILootEntry> lootEntries = rows.fromString(p.getProperty("lootEntries"));
			return new LootTable(name, lootEntries);
		}
	}
}

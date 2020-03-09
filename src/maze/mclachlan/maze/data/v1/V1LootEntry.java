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
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootEntryRow;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1LootEntry
{
	static final String SEP = "/";

	static V1PercentageTable<LootEntryRow> rows = new V1PercentageTable<LootEntryRow>()
	{
		public LootEntryRow typeFromString(String s)
		{
			String[] strs = s.split(SEP);
			return new LootEntryRow(strs[0], V1Dice.fromString(strs[1]));
		}

		public String typeToString(LootEntryRow row)
		{
			StringBuilder s = new StringBuilder();
			s.append(row.getItemName());
			s.append(SEP);
			s.append(V1Dice.toString(row.getQuantity()));
			return s.toString();
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, LootEntry> load(BufferedReader reader)
	{
		try
		{
			Map <String, LootEntry> result = new HashMap<>();

			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				LootEntry g = fromProperties(p);
				result.put(g.getName(), g);
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, LootEntry> map) throws Exception
	{
		for (String name : map.keySet())
		{
			LootEntry g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(LootEntry obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != LootEntry.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("contains=");
			b.append(rows.toString(obj.getContains()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static LootEntry fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom LootEntry impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (LootEntry)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			PercentageTable<LootEntryRow> contains = rows.fromString(p.getProperty("contains"));
			return new LootEntry(name, contains);
		}
	}
}

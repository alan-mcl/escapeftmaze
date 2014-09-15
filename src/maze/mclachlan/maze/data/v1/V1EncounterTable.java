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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class V1EncounterTable
{
	static final String SEP = "/";

	static V1PercentageTable<FoeEntry> rows = new V1PercentageTable<FoeEntry>()
	{
		public FoeEntry typeFromString(String s)
		{
			return Database.getInstance().getFoeEntry(s);
		}

		public String typeToString(FoeEntry FoeEntry)
		{
			return FoeEntry.getName();
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, EncounterTable> load(BufferedReader reader) throws Exception
	{
		Map <String, EncounterTable> result = new HashMap<String, EncounterTable>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			EncounterTable g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, EncounterTable> map) throws Exception
	{
		for (String name : map.keySet())
		{
			EncounterTable g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(EncounterTable obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != EncounterTable.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("foeEntries=");
			b.append(rows.toString(obj.getEncounterTable()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static EncounterTable fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom EncounterTable impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (EncounterTable)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			PercentageTable<FoeEntry> foeEntries = rows.fromString(p.getProperty("foeEntries"));
			return new EncounterTable(name, foeEntries);
		}
	}
}

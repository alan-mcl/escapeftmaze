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

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import mclachlan.maze.stat.Item;

/**
 *
 */
public class V1ItemCache
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, Map<Point, List<Item>>> load(BufferedReader reader) throws Exception
	{
		Map<String, Map<Point, List<Item>>> result = new HashMap<String, Map<Point, List<Item>>>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			String zone = p.getProperty("zone");
			Map<Point, List<Item>> g = fromProperties(p);
			if (g != null && g.size() > 0)
			{
				result.put(zone, g);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, Map<Point, List<Item>>> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Map<Point, List<Item>> g = map.get(name);
			writer.write("zone="+name);
			writer.newLine();
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Map<Point, List<Item>> obj)
	{
		StringBuilder b = new StringBuilder();

		for (Point p : obj.keySet())
		{
			b.append(V1Point.toString(p, ",")+'=');
			b.append(V1PlayerCharacter.itemsList.toString(obj.get(p)));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Map<Point, List<Item>> fromProperties(Properties p) throws Exception
	{
		Map<Point, List<Item>> result = new HashMap<Point, List<Item>>();
		for (Object key : p.keySet())
		{
			if (!key.equals("zone"))
			{
				Point tile = V1Point.fromString((String)key, ",");
				List<Item> items = V1PlayerCharacter.itemsList.fromString(p.getProperty((String)key));
				if (items != null && !items.isEmpty())
				{
					result.put(tile, items);
				}
			}
		}
		return result;
	}
}

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
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Trap
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, Trap> load(BufferedReader reader)
	{
		try
		{
			Map <String, Trap> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				Trap g = fromProperties(p);
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
	public static void save(BufferedWriter writer, Map<String, Trap> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Trap g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Trap obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != Trap.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("difficulty=");
			b.append(V1Utils.toStringInts(obj.getDifficulty(), ","));
			b.append(V1Utils.NEWLINE);

			b.append("required=");
			b.append(V1BitSet.toString(obj.getRequired()));
			b.append(V1Utils.NEWLINE);

			b.append("payload=");
			b.append(V1TileScript.toString(obj.getPayload()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Trap fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom Trap impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (Trap)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			int[] difficulty = V1Utils.fromStringInts(p.getProperty("difficulty"), ",");
			BitSet required = V1BitSet.fromString(p.getProperty("required"));
			TileScript payload = V1TileScript.fromString(p.getProperty("payload"));

			return new Trap(name, difficulty, required, payload);
		}
	}
}

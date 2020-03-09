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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1MazeScript
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, MazeScript> load(BufferedReader reader)
	{
		try
		{
			Map <String, MazeScript> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				MazeScript g = fromProperties(p);
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
	public static void save(BufferedWriter writer, Map<String, MazeScript> map) throws Exception
	{
		for (String name : map.keySet())
		{
			MazeScript g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(MazeScript obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != MazeScript.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			List<MazeEvent> events = obj.getEvents();

			for (int i = 0; i < events.size(); i++)
			{
				b.append(i).append("=");
				b.append(V1MazeEvent.toString(events.get(i)));
				b.append(V1Utils.NEWLINE);
			}
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static MazeScript fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom MazeScript impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (MazeScript)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			ArrayList<MazeEvent> events = new ArrayList<MazeEvent>();
			int count = 0;
			while (true)
			{
				String s = p.getProperty(String.valueOf(count++));
				if (s == null)
				{
					break;
				}
				events.add(V1MazeEvent.fromString(s));
			}
			return new MazeScript(name, events);
		}
	}
}

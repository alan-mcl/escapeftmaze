/*
 * Copyright (c) 2012 Alan McLachlan
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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Personalities
{
	private static final String NAME = "___name___";
	private static final String DESC = "___desc___";
	private static final String COLOUR = "___colour___";

	/*-------------------------------------------------------------------------*/
	public static Map<String, Personality> load(BufferedReader reader)
	{
		try
		{
			Map <String, Personality> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				Personality g = fromProperties(p);
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
	public static void save(BufferedWriter writer, Map<String, Personality> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Personality g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Personality obj)
	{
		StringBuilder b = new StringBuilder();

		b.append(NAME+"=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append(DESC+"=");
		b.append(V1Utils.escapeNewlines(obj.getDescription()));
		b.append(V1Utils.NEWLINE);

		b.append(COLOUR+"=");
		b.append(V1Colour.toString(obj.getColour()));
		b.append(V1Utils.NEWLINE);

		for (String s : obj.getSpeech().keySet())
		{
			b.append(s).append("=").append(V1Utils.escapeNewlines(obj.getSpeech().get(s)));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Personality fromProperties(Properties p) throws Exception
	{
		String name = p.getProperty(NAME);
		String desc = V1Utils.replaceNewlines(p.getProperty(DESC));
		Color colour = V1Colour.fromString(p.getProperty(COLOUR));

		Map<String, String> map = new HashMap<String, String>();

		Set<String> reserved = new HashSet<String>();
		reserved.add(NAME);
		reserved.add(DESC);
		reserved.add(COLOUR);

		for (String s : p.stringPropertyNames())
		{
			if (!reserved.contains(s))
			{
				map.put(s, V1Utils.replaceNewlines(p.getProperty(s)));
			}
		}

		return new Personality(name, desc, map, colour);
	}
}

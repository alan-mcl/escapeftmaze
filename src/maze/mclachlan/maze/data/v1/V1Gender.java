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
import mclachlan.maze.stat.Gender;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Gender
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, Gender> load(
		BufferedReader reader)
	{
		try
		{
			Map <String, Gender> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				Gender g = fromProperties(p);
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
	public static void save(BufferedWriter writer, Map<String, Gender> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Gender g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Gender obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != Gender.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("startingModifiers=");
			b.append(V1StatModifier.toString(obj.getStartingModifiers()));
			b.append(V1Utils.NEWLINE);
			b.append("constantModifiers=");
			b.append(V1StatModifier.toString(obj.getConstantModifiers()));
			b.append(V1Utils.NEWLINE);
			b.append("bannerModifiers=");
			b.append(V1StatModifier.toString(obj.getBannerModifiers()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Gender fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom gender impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (Gender)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			StatModifier sm = V1StatModifier.fromString(p.getProperty("startingModifiers"));
			StatModifier cm = V1StatModifier.fromString(p.getProperty("constantModifiers"));
			StatModifier bm = V1StatModifier.fromString(p.getProperty("bannerModifiers"));
			return new Gender(name, sm, cm, bm);
		}
	}
}

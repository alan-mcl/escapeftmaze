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
import mclachlan.maze.stat.ExperienceTable;
import mclachlan.maze.stat.ExperienceTableArray;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1ExperienceTable
{
	static V1List<Integer> levels = new V1List<Integer>()
	{
		public String typeToString(Integer integer)
		{
			return String.valueOf(integer);
		}

		public Integer typeFromString(String s)
		{
			return Integer.parseInt(s);
		}
	};
	private static String separator = ",";

	/*-------------------------------------------------------------------------*/
	public static Map<String, ExperienceTable> load(BufferedReader reader) throws Exception
	{
		Map <String, ExperienceTable> result = new HashMap<String, ExperienceTable>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ExperienceTable g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, ExperienceTable> experienceTables) throws Exception
	{
		for (String name : experienceTables.keySet())
		{
			ExperienceTable g = experienceTables.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(ExperienceTable obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != ExperienceTableArray.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			if (!(obj instanceof ExperienceTableArray))
			{
				throw new MazeException("Unsupported xp table impl: "+obj);
			}

			ExperienceTableArray xt = (ExperienceTableArray)obj;

			b.append("levels=");
			b.append(V1Utils.toStringInts(xt.getLevels(), separator));
			b.append(V1Utils.NEWLINE);

			b.append("postGygaxIncrement=");
			b.append(xt.getPostGygaxIncrement());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static ExperienceTable fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom ExperienceTable impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (ExperienceTable)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			int[] levels = V1Utils.fromStringInts(p.getProperty("levels"), separator);
			int postGygaxInc = Integer.parseInt(p.getProperty("postGygaxIncrement"));

			return new ExperienceTableArray(name, levels, postGygaxInc);
		}
	}
}

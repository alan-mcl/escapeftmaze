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
import mclachlan.maze.stat.condition.ConditionEffect;

/**
 *
 */
public class V1ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, ConditionEffect> load(BufferedReader reader) throws Exception
	{
		Map <String, ConditionEffect> result = new HashMap<String, ConditionEffect>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ConditionEffect g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, ConditionEffect> map) throws Exception
	{
		for (String name : map.keySet())
		{
			ConditionEffect g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(ConditionEffect obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		// custom impl
		b.append("impl=");
		b.append(obj.getClass().getName());
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static ConditionEffect fromProperties(Properties p) throws Exception
	{
		// custom ConditionEffect impl
		Class clazz = Class.forName(p.getProperty("impl"));
		ConditionEffect result = (ConditionEffect)clazz.newInstance();
		result.setName(p.getProperty("name"));
		return result;
	}
}

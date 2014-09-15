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
import mclachlan.maze.stat.CraftRecipe;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class V1CraftRecipe
{
	public static final String SEP = ",";

	/*-------------------------------------------------------------------------*/
	public static Map<String, CraftRecipe> load(BufferedReader reader) throws Exception
	{
		Map <String, CraftRecipe> result = new HashMap<String, CraftRecipe>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			CraftRecipe g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, CraftRecipe> map) throws Exception
	{
		for (String name : map.keySet())
		{
			CraftRecipe g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(CraftRecipe obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append("requirements=");
		b.append(V1StatModifier.toString(obj.getRequirements()));
		b.append(V1Utils.NEWLINE);

		b.append("item1=");
		b.append(obj.getItem1());
		b.append(V1Utils.NEWLINE);

		b.append("item2=");
		b.append(obj.getItem2());
		b.append(V1Utils.NEWLINE);

		b.append("resultingItem=");
		b.append(obj.getResultingItem());
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static CraftRecipe fromProperties(Properties p) throws Exception
	{
		String name = p.getProperty("name");
		StatModifier requirements = V1StatModifier.fromString(p.getProperty("requirements"));
		String item1 = p.getProperty("item1");
		String item2 = p.getProperty("item2");
		String resultingItem = p.getProperty("resultingItem");

		return new CraftRecipe(name, requirements, item1, item2, resultingItem);
	}
}

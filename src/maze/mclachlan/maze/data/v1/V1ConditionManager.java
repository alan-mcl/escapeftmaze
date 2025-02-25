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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;

/**
 *
 */
public class V1ConditionManager
{
	/*-------------------------------------------------------------------------*/
	public static Map<ConditionBearer,List<Condition>> load(
		BufferedReader reader,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		Map<ConditionBearer,List<Condition>> result = new HashMap<ConditionBearer,List<Condition>>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ConditionBearer cb = V1ConditionBearer.fromString(
				p.getProperty("conditionBearer"),
				playerCharacterCache);
			if (cb != null)
			{
				List<Condition> conditions = fromProperties(p, cb);
				result.put(cb, conditions);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<ConditionBearer,List<Condition>> map) throws Exception
	{
		for (ConditionBearer cb : map.keySet())
		{
			String cbs = V1ConditionBearer.toString(cb);
			
			if (cbs != null && cbs.length() > 0)
			{
				List<Condition> g = map.get(cb);
				writer.write("conditionBearer="+ cbs);
				writer.newLine();
				writer.write(toProperties(g));
				writer.write("@");
				writer.newLine();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(List<Condition> obj)
	{
		StringBuilder b = new StringBuilder();

		int count=0;
		for (Condition c : obj)
		{
			b.append(""+(count++)+'=');
			b.append(V1Condition.toString(c));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static List<Condition> fromProperties(Properties p, ConditionBearer cb) throws Exception
	{
		List<Condition> result = new ArrayList<Condition>();
		for (Object key : p.keySet())
		{
			if (!key.equals("conditionBearer"))
			{
				Condition c = V1Condition.fromString(p.getProperty((String)key));
				c.setTarget(cb);
				cb.addCondition(c);
				result.add(c);
			}
		}
		return result;
	}
}

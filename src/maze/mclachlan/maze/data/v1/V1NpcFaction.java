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
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcFactionTemplate;

/**
 *
 */
public class V1NpcFaction
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, NpcFaction> load(BufferedReader reader) throws Exception
	{
		Map <String, NpcFaction> result = new HashMap<String, NpcFaction>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			NpcFaction g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, NpcFaction> map) throws Exception
	{
		for (String name : map.keySet())
		{
			NpcFaction g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(NpcFaction obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append("template=");
		b.append(obj.getTemplate().getName());
		b.append(V1Utils.NEWLINE);

		b.append("attitude=");
		b.append(obj.getAttitude());
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static NpcFaction fromProperties(Properties p) throws Exception
	{
//		String name = p.getProperty("name");
		NpcFactionTemplate template = Database.getInstance().getNpcFactionTemplates().get(p.getProperty("template"));
		NpcFaction.Attitude attitude = NpcFaction.Attitude.valueOf(p.getProperty("attitude"));

		return new NpcFaction(
			template,
			attitude);
	}
}

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
import java.util.*;
import java.util.List;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcTemplate;

/**
 *
 */
public class V1Npc
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, Npc> load(BufferedReader reader) throws Exception
	{
		Map <String, Npc> result = new HashMap<String, Npc>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			Npc g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, Npc> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Npc g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Npc obj)
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

		b.append("currentInventory=");
		b.append(V1PlayerCharacter.itemsList.toString(obj.getCurrentInventory()));
		b.append(V1Utils.NEWLINE);

		b.append("theftCounter=");
		b.append(obj.getTheftCounter());
		b.append(V1Utils.NEWLINE);

		b.append("zone=");
		b.append(obj.getZone());
		b.append(V1Utils.NEWLINE);

		b.append("tile=");
		b.append(V1Point.toString(obj.getTile()));
		b.append(V1Utils.NEWLINE);

		b.append("found=");
		b.append(obj.isFound());
		b.append(V1Utils.NEWLINE);

		b.append("dead=");
		b.append(obj.isDead());
		b.append(V1Utils.NEWLINE);
		
		b.append("guildMaster=");
		b.append(obj.isGuildMaster());
		b.append(V1Utils.NEWLINE);
		
		b.append("guild=");
		b.append(V1Utils.stringList.toString(obj.getGuild()));
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Npc fromProperties(Properties p) throws Exception
	{
		NpcTemplate template = Database.getInstance().getNpcTemplates().get(p.getProperty("template"));
		int attitude = Integer.parseInt(p.getProperty("attitude"));
		List<Item> currentInventory = V1PlayerCharacter.itemsList.fromString(p.getProperty("currentInventory"));
		if (currentInventory == null)
		{
			currentInventory = new ArrayList<Item>();
		}
		int theftCounter = Integer.parseInt(p.getProperty("theftCounter"));
		String zone = p.getProperty("zone");
		Point tile = V1Point.fromString(p.getProperty("tile"));
		boolean found = Boolean.parseBoolean(p.getProperty("found"));
		boolean dead = Boolean.parseBoolean(p.getProperty("dead"));
		boolean guildMaster = Boolean.parseBoolean(p.getProperty("guildMaster"));
		List<String> guild = V1Utils.stringList.fromString(p.getProperty("guild"));
		if (guild == null)
		{
			guild = new ArrayList<String>();
		}

		return new Npc(
			template,
			attitude,
			currentInventory,
			theftCounter,
			tile,
			zone,
			found,
			dead, 
			guildMaster,
			guild);
	}
}

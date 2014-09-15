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

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.stat.npc.NpcInventoryTemplate;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcTemplate;

/**
 *
 */
public class V1NpcTemplate
{

	/*-------------------------------------------------------------------------*/
	public static Map<String, NpcTemplate> load(V1Loader v1Loader,
		BufferedReader reader) throws Exception
	{
		Map <String, NpcTemplate> result = new HashMap<String, NpcTemplate>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			NpcTemplate nt = fromProperties(p);
			result.put(nt.getName(), nt);

			// load npc dialogue
			V1StringManager sm = (V1StringManager)(v1Loader.getStringManager());
			Properties speechP = sm.loadProperties("npcs/" + nt.getName());
			NpcSpeech speech = V1NpcSpeech.fromProperties(speechP);
			nt.setDialogue(speech);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(String path, BufferedWriter writer, Map<String, NpcTemplate> map) throws Exception
	{
		for (String name : map.keySet())
		{
			NpcTemplate nt = map.get(name);
			writer.write(toProperties(nt));
			writer.write("@");
			writer.newLine();

			// write npc dialogue
			V1StringManager.storeProperties(
				path,
				"npcs/"+name,
				V1NpcSpeech.toProperties(nt.getDialogue()));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(NpcTemplate obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != NpcTemplate.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("foeName=");
			b.append(obj.getFoeName());
			b.append(V1Utils.NEWLINE);

			b.append("faction=");
			b.append(obj.getFaction());
			b.append(V1Utils.NEWLINE);

			b.append("attitude=");
			b.append(obj.getAttitude());
			b.append(V1Utils.NEWLINE);

			b.append("script=");
			b.append(obj.getScript().getClass().getName());
			b.append(V1Utils.NEWLINE);

			b.append("alliesOnCall=");
			b.append(obj.getAlliesOnCall());
			b.append(V1Utils.NEWLINE);

			b.append("buysAt=");
			b.append(obj.getBuysAt());
			b.append(V1Utils.NEWLINE);

			b.append("sellsAt=");
			b.append(obj.getSellsAt());
			b.append(V1Utils.NEWLINE);

			b.append("maxPurchasePrice=");
			b.append(obj.getMaxPurchasePrice());
			b.append(V1Utils.NEWLINE);

			b.append("willBuyItemTypes=");
			b.append(V1BitSet.toString(obj.getWillBuyItemTypes()));
			b.append(V1Utils.NEWLINE);

			b.append("inventoryTemplate=");
			b.append(V1NpcInventoryTemplate.toString(obj.getInventoryTemplate()));
			b.append(V1Utils.NEWLINE);

			b.append("resistThreats=");
			b.append(obj.getResistThreats());
			b.append(V1Utils.NEWLINE);

			b.append("resistBribes=");
			b.append(obj.getResistBribes());
			b.append(V1Utils.NEWLINE);

			b.append("resistSteal=");
			b.append(obj.getResistSteal());
			b.append(V1Utils.NEWLINE);

			b.append("theftCounter=");
			b.append(obj.getTheftCounter());
			b.append(V1Utils.NEWLINE);

			b.append("zone=");
			b.append(obj.getZone());
			b.append(V1Utils.NEWLINE);

			b.append("tile=");
			b.append(obj.getTile().x+","+obj.getTile().y);
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
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static NpcTemplate fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom NpcTemplate impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (NpcTemplate)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String foeName = p.getProperty("foeName");
			String faction = p.getProperty("faction");
			int attitude = Integer.parseInt(p.getProperty("attitude"));
			Class clazz = Class.forName(p.getProperty("script"));
			NpcScript script = (NpcScript)clazz.newInstance();
			String alliesOnCall = p.getProperty("alliesOnCall");
			int buysAt = Integer.parseInt(p.getProperty("buysAt"));
			int sellsAt = Integer.parseInt(p.getProperty("sellsAt"));
			int maxPurchasePrice = Integer.parseInt(p.getProperty("maxPurchasePrice"));
			BitSet willBuyItemTypes = V1BitSet.fromString(p.getProperty("willBuyItemTypes"));
			NpcInventoryTemplate inventoryTemplate = V1NpcInventoryTemplate.fromString(p.getProperty("inventoryTemplate"));
			int resistThreats = Integer.parseInt(p.getProperty("resistThreats"));
			int resistBribes = Integer.parseInt(p.getProperty("resistBribes"));
			int resistSteal = Integer.parseInt(p.getProperty("resistSteal"));
			int theftCounter = Integer.parseInt(p.getProperty("theftCounter"));
			String zone = p.getProperty("zone");
			String[] strs = p.getProperty("tile").split(",");
			Point tile = new Point(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
			boolean found = Boolean.valueOf(p.getProperty("found"));
			boolean dead = Boolean.valueOf(p.getProperty("dead"));
			boolean guildMaster = Boolean.valueOf(p.getProperty("guildMaster"));

			return new NpcTemplate(
				name,
				foeName,
				faction,
				attitude,
				script,
				alliesOnCall,
				buysAt,
				sellsAt,
				maxPurchasePrice,
				willBuyItemTypes,
				inventoryTemplate,
				resistThreats,
				resistBribes,
				resistSteal,
				theftCounter,
				new NpcSpeech(), // loaded later in the process
				zone,
				tile,
				found,
				dead,
				guildMaster);
		}
	}
}

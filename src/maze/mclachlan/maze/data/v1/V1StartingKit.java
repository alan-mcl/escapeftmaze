/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.maze.stat.StartingKit;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class V1StartingKit
{
	/*-------------------------------------------------------------------------*/
	private static void append(StringBuilder result, String s)
	{
		result.append(s == null ? "" : s);
	}

	/*-------------------------------------------------------------------------*/
	public static Map<String, StartingKit> load(
		BufferedReader reader) throws Exception
	{
		Map<String, StartingKit> result = new HashMap<String, StartingKit>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			StartingKit g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer,
		Map<String, StartingKit> map) throws Exception
	{
		for (String name : map.keySet())
		{
			StartingKit g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(StartingKit obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != StartingKit.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("displayName=");
			append(b, obj.getDisplayName());
			b.append(V1Utils.NEWLINE);

			b.append("primaryWeapon=");
			append(b, obj.getPrimaryWeapon());
			b.append(V1Utils.NEWLINE);

			b.append("secondaryWeapon=");
			append(b, obj.getSecondaryWeapon());
			b.append(V1Utils.NEWLINE);

			b.append("helm=");
			append(b, obj.getHelm());
			b.append(V1Utils.NEWLINE);

			b.append("torsoArmour=");
			append(b, obj.getTorsoArmour());
			b.append(V1Utils.NEWLINE);

			b.append("legArmour=");
			append(b, obj.getLegArmour());
			b.append(V1Utils.NEWLINE);

			b.append("gloves=");
			append(b, obj.getGloves());
			b.append(V1Utils.NEWLINE);

			b.append("boots=");
			append(b, obj.getBoots());
			b.append(V1Utils.NEWLINE);

			b.append("miscItem1=");
			append(b, obj.getMiscItem1());
			b.append(V1Utils.NEWLINE);

			b.append("miscItem2=");
			append(b, obj.getMiscItem2());
			b.append(V1Utils.NEWLINE);

			b.append("bannerItem=");
			append(b, obj.getBannerItem());
			b.append(V1Utils.NEWLINE);

			b.append("description=");
			append(b, obj.getDescription() == null ? "" : V1Utils.escapeNewlineaAndCommas(obj.getDescription()));
			b.append(V1Utils.NEWLINE);

			b.append("combatModifier=");
			append(b, V1StatModifier.toString(obj.getCombatModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("stealthModifier=");
			append(b, V1StatModifier.toString(obj.getStealthModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("magicModifier=");
			append(b, V1StatModifier.toString(obj.getMagicModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("usableByCharacterClass=");
			Set<String> classes = obj.getUsableByCharacterClass();
			b.append(classes == null ? "" : V1Utils.stringList.toString(new ArrayList<String>(classes)));
			b.append(V1Utils.NEWLINE);

			int max = 20;
			for (int i = 0; i < max; i++)
			{
				b.append("packItem").append(i).append("=");

				if (obj.getPackItems() != null && obj.getPackItems().size() > i)
				{
					String str = obj.getPackItems().get(i);
					if (str != null)
					{
						b.append(str);
					}
				}

				b.append(V1Utils.NEWLINE);
			}
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static StartingKit fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom CharacterClass impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (StartingKit)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String displayName = p.getProperty("displayName");
			String description = V1Utils.replaceNewlineaAndCommas(p.getProperty("description"));
			StatModifier combatModifier = V1StatModifier.fromString(p.getProperty("combatModifier"));
			StatModifier stealthModifier = V1StatModifier.fromString(p.getProperty("stealthModifier"));
			StatModifier magictModifier = V1StatModifier.fromString(p.getProperty("magicModifier"));

			String primaryWeapon = getProp(p.getProperty("primaryWeapon"));
			String secondaryWeapon = getProp(p.getProperty("secondaryWeapon"));
			String helm = getProp(p.getProperty("helm"));
			String torsoArmour = getProp(p.getProperty("torsoArmour"));
			String legArmour = getProp(p.getProperty("legArmour"));
			String boots = getProp(p.getProperty("boots"));
			String gloves = getProp(p.getProperty("gloves"));
			String miscItem1 = getProp(p.getProperty("miscItem1"));
			String miscItem2 = getProp(p.getProperty("miscItem2"));
			String bannerItem = getProp(p.getProperty("bannerItem"));
			String usableByCharacterClass = p.getProperty("usableByCharacterClass");
			List<String> classes;
			if (usableByCharacterClass == null || "".equals(usableByCharacterClass))
			{
				classes = new ArrayList<String>();
			}
			else
			{
				classes = V1Utils.stringList.fromString(usableByCharacterClass);
			}

			ArrayList<String> packItems = new ArrayList<String>();
			int max = 20;
			for (int i = 0; i < max; i++)
			{
				String packItem = getProp(p.getProperty("packItem" + i));

				if (packItem != null)
				{
					packItems.add(packItem);
				}
			}

			return new StartingKit(name, displayName, primaryWeapon, secondaryWeapon, helm,
				torsoArmour, legArmour, gloves, boots, miscItem1, miscItem2, bannerItem,
				packItems, description, combatModifier, stealthModifier, magictModifier, new HashSet<String>(classes));
		}
	}

	private static String getProp(String s)
	{
		return "".equals(s) ? null : s;
	}
}

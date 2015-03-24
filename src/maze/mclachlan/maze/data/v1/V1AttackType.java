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
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class V1AttackType
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, AttackType> load(BufferedReader reader) throws Exception
	{
		Map <String, AttackType> result = new HashMap<String, AttackType>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			AttackType g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, AttackType> map) throws Exception
	{
		for (String name : map.keySet())
		{
			AttackType g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(AttackType obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != AttackType.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("verb=");
			b.append(obj.getVerb());
			b.append(V1Utils.NEWLINE);
			b.append("modifiers=");
			b.append(V1StatModifier.toString(obj.getModifiers()));
			b.append(V1Utils.NEWLINE);
			b.append("damageType=");
			b.append(obj.getDamageType().name());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static AttackType fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom AttackType impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (AttackType)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String verb = p.getProperty("verb");
			MagicSys.SpellEffectType damageType = MagicSys.SpellEffectType.valueOf(p.getProperty("damageType"));
			StatModifier modifiers = V1StatModifier.fromString(p.getProperty("modifiers"));
			return new AttackType(name, verb, damageType, modifiers);
		}
	}
}

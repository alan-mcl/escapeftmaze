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
import mclachlan.maze.stat.magic.*;

/**
 *
 */
public class V1SpellEffect
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, SpellEffect> load(BufferedReader reader) throws Exception
	{
		Map <String, SpellEffect> result = new HashMap<String, SpellEffect>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			SpellEffect g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, SpellEffect> map) throws Exception
	{
		for (String name : map.keySet())
		{
			SpellEffect g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(SpellEffect obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != SpellEffect.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("displayName=");
			b.append(obj.getDisplayName()==null?"":obj.getDisplayName());
			b.append(V1Utils.NEWLINE);

			b.append("type=");
			b.append(obj.getType().name());
			b.append(V1Utils.NEWLINE);

			b.append("subtype=");
			b.append(obj.getSubType().name());
			b.append(V1Utils.NEWLINE);

			b.append("saveAdjustment=");
			b.append(V1Value.toString(obj.getSaveAdjustment()));
			b.append(V1Utils.NEWLINE);

			b.append("unsavedResult=");
			b.append(V1SpellResult.toString(obj.getUnsavedResult()));
			b.append(V1Utils.NEWLINE);

			b.append("savedResult=");
			b.append(V1SpellResult.toString(obj.getSavedResult()));
			b.append(V1Utils.NEWLINE);

			b.append("targetType=");
			b.append(obj.getTargetType());
			b.append(V1Utils.NEWLINE);

			b.append("application=");
			b.append(obj.getApplication());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static SpellEffect fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom SpellEffect impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (SpellEffect)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String displayName = p.getProperty("displayName");
			if ("".equals(displayName))
			{
				displayName = null;
			}
			MagicSys.SpellEffectType type = MagicSys.SpellEffectType.valueOf(p.getProperty("type"));
			ValueList saveAdjustment = V1Value.fromString(p.getProperty("saveAdjustment"));
			SpellResult unsavedResult = V1SpellResult.fromString(p.getProperty("unsavedResult"));
			SpellResult savedResult = V1SpellResult.fromString(p.getProperty("savedResult"));
			int targetType = Integer.parseInt(p.getProperty("targetType"));
			MagicSys.SpellEffectSubType effectSubType =
				MagicSys.SpellEffectSubType.valueOf(p.getProperty("subtype"));
			SpellEffect.Application application =
				SpellEffect.Application.valueOf(
					p.getProperty("application"));

			return new SpellEffect(name, displayName, type, effectSubType, application, saveAdjustment, unsavedResult, savedResult, targetType);
		}
	}
}

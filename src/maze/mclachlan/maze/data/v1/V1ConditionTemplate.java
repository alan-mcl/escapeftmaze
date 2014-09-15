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
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.data.Database;

/**
 *
 */
public class V1ConditionTemplate
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, ConditionTemplate> load(BufferedReader reader) throws Exception
	{
		Map <String, ConditionTemplate> result = new HashMap<String, ConditionTemplate>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ConditionTemplate g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, ConditionTemplate> map) throws Exception
	{
		for (String name : map.keySet())
		{
			ConditionTemplate g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(ConditionTemplate obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getImpl() != null)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getImpl().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("displayName=");
			b.append(obj.getDisplayName()==null?"":obj.getDisplayName());
			b.append(V1Utils.NEWLINE);

			b.append("icon=");
			b.append(obj.getIcon());
			b.append(V1Utils.NEWLINE);

			b.append("adjective=");
			b.append(obj.getAdjective());
			b.append(V1Utils.NEWLINE);

			b.append("conditionEffect=");
			ConditionEffect conditionEffect = obj.getConditionEffect();
			if (conditionEffect != null)
			{
				b.append(conditionEffect.getName());
			}
			b.append(V1Utils.NEWLINE);

			b.append("duration=");
			b.append(V1Value.toString(obj.getDuration()));
			b.append(V1Utils.NEWLINE);

			b.append("strength=");
			b.append(V1Value.toString(obj.getStrength()));
			b.append(V1Utils.NEWLINE);

			b.append("hitPointDamage=");
			b.append(V1Value.toString(obj.getHitPointDamage()));
			b.append(V1Utils.NEWLINE);
			
			b.append("staminaDamage=");
			b.append(V1Value.toString(obj.getStaminaDamage()));
			b.append(V1Utils.NEWLINE);
			
			b.append("actionPointDamage=");
			b.append(V1Value.toString(obj.getActionPointDamage()));
			b.append(V1Utils.NEWLINE);
			
			b.append("magicPointDamage=");
			b.append(V1Value.toString(obj.getMagicPointDamage()));
			b.append(V1Utils.NEWLINE);

			b.append("statModifier=");
			b.append(V1StatModifier.toString(obj.getStatModifier()));
			b.append(V1Utils.NEWLINE);

			b.append("bannerModifier=");
			b.append(V1StatModifier.toString(obj.getBannerModifier()));
			b.append(V1Utils.NEWLINE);

			b.append("scaleModifierWithStrength=");
			b.append(obj.scaleModifierWithStrength());
			b.append(V1Utils.NEWLINE);

			b.append("strengthWanes=");
			b.append(obj.strengthWanes());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static ConditionTemplate fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom ConditionTemplate impl
			String name = p.getProperty("name");
			Class clazz = Class.forName(p.getProperty("impl"));
			return new ConditionTemplate(name, clazz);
		}
		else
		{
			String name = p.getProperty("name");
			String displayName = p.getProperty("displayName");
			String icon = p.getProperty("icon");
			String adjective = p.getProperty("adjective");
			ConditionEffect conditionEffect = null;
			String ceStr = p.getProperty("conditionEffect");
			if (ceStr.equals("none"))
			{
				conditionEffect = ConditionEffect.NONE;
			}
			else
			{
				conditionEffect = Database.getInstance().getConditionEffect(ceStr);
			}
			Value duration = V1Value.fromString(p.getProperty("duration"));
			Value strength = V1Value.fromString(p.getProperty("strength"));
			Value hp = V1Value.fromString(p.getProperty("hitPointDamage"));
			Value stam = V1Value.fromString(p.getProperty("staminaDamage"));
			Value ap = V1Value.fromString(p.getProperty("actionPointDamage"));
			Value mp = V1Value.fromString(p.getProperty("magicPointDamage"));
			StatModifier statModifier = V1StatModifier.fromString(p.getProperty("statModifier"));
			StatModifier bannerModifier = V1StatModifier.fromString(p.getProperty("bannerModifier"));
			boolean scaleModifierWithStrength = Boolean.valueOf(p.getProperty("scaleModifierWithStrength"));
			boolean strengthWanes = Boolean.valueOf(p.getProperty("strengthWanes"));

			return new ConditionTemplate(
				name,
				displayName,
				duration,
				strength,
				conditionEffect,
				statModifier,
				bannerModifier,
				hp,
				stam,
				ap,
				mp,
				icon,
				adjective,
				scaleModifierWithStrength,
				strengthWanes);
		}
	}
}

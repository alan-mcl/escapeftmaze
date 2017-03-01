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
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.RepeatedSpellEffect;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class V1ConditionTemplate
{
	/*-------------------------------------------------------------------------*/
	static V1List<RepeatedSpellEffect> repeatedSpellEffects = new V1List<RepeatedSpellEffect>(",")
	{
		public String typeToString(RepeatedSpellEffect rse)
		{
			return V1RepeatedSpellEffect.toString(rse);
		}

		public RepeatedSpellEffect typeFromString(String s)
		{
			return V1RepeatedSpellEffect.fromString(s);
		}
	};

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
			b.append(obj.isScaleModifierWithStrength());
			b.append(V1Utils.NEWLINE);

			b.append("strengthWanes=");
			b.append(obj.isStrengthWanes());
			b.append(V1Utils.NEWLINE);

			b.append("exitCondition=");
			b.append(obj.getExitCondition());
			b.append(V1Utils.NEWLINE);

			b.append("exitConditionChance=");
			b.append(obj.getExitConditionChance());
			b.append(V1Utils.NEWLINE);

			b.append("exitSpellEffect=");
			b.append(obj.getExitSpellEffect()==null?"":obj.getExitSpellEffect());
			b.append(V1Utils.NEWLINE);

			b.append("repeatedSpellEffects=");
			b.append(repeatedSpellEffects.toString(obj.getRepeatedSpellEffects()));
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
			ValueList duration = V1Value.fromString(p.getProperty("duration"));
			ValueList strength = V1Value.fromString(p.getProperty("strength"));
			ValueList hp = V1Value.fromString(p.getProperty("hitPointDamage"));
			ValueList stam = V1Value.fromString(p.getProperty("staminaDamage"));
			ValueList ap = V1Value.fromString(p.getProperty("actionPointDamage"));
			ValueList mp = V1Value.fromString(p.getProperty("magicPointDamage"));
			StatModifier statModifier = V1StatModifier.fromString(p.getProperty("statModifier"));
			StatModifier bannerModifier = V1StatModifier.fromString(p.getProperty("bannerModifier"));
			boolean scaleModifierWithStrength = Boolean.valueOf(p.getProperty("scaleModifierWithStrength"));
			boolean strengthWanes = Boolean.valueOf(p.getProperty("strengthWanes"));
			List<RepeatedSpellEffect> rse = repeatedSpellEffects.fromString(p.getProperty("repeatedSpellEffects"));
			ConditionTemplate.ExitCondition exitCondition =
				ConditionTemplate.ExitCondition.valueOf(p.getProperty("exitCondition"));
			int exitConditionChance = Integer.valueOf(p.getProperty("exitConditionChance"));
			String exitSpellEffect = p.getProperty("exitSpellEffect");
			if ("".equals(exitSpellEffect))
			{
				exitSpellEffect = null;
			}

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
				strengthWanes,
				exitCondition,
				exitConditionChance,
				exitSpellEffect,
				rse);
		}
	}
}

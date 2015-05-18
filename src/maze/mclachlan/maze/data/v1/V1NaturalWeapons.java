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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class V1NaturalWeapons
{
	static V1GroupOfPossibilties<SpellEffect> spellEffects = new V1GroupOfPossibilties<SpellEffect>()
	{
		public SpellEffect typeFromString(String s)
		{
			return Database.getInstance().getSpellEffect(s);
		}

		public String typeToString(SpellEffect spellEffect)
		{
			return spellEffect.getName();
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, NaturalWeapon> load(BufferedReader reader) throws Exception
	{
		Map <String, NaturalWeapon> result = new HashMap<String, NaturalWeapon>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			NaturalWeapon g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, NaturalWeapon> map) throws Exception
	{
		for (String name : map.keySet())
		{
			NaturalWeapon g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(NaturalWeapon obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != NaturalWeapon.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("ranged=");
			b.append(obj.isRanged());
			b.append(V1Utils.NEWLINE);

			b.append("description=");
			b.append(obj.getDescription());
			b.append(V1Utils.NEWLINE);

			b.append("modifiers=");
			b.append(V1StatModifier.toString(obj.getModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("minRange=");
			b.append(obj.getMinRange());
			b.append(V1Utils.NEWLINE);

			b.append("maxRange=");
			b.append(obj.getMaxRange());
			b.append(V1Utils.NEWLINE);

			b.append("attacks=");
			b.append(V1Utils.toStringInts(obj.getAttacks(), ","));
			b.append(V1Utils.NEWLINE);

			b.append("slaysFoeType=");
			b.append(obj.getSlaysFoeType()==null?"":obj.getSlaysFoeType());
			b.append(V1Utils.NEWLINE);

			b.append("damage=");
			b.append(obj.getDamage()==null?"":V1Dice.toString(obj.getDamage()));
			b.append(V1Utils.NEWLINE);

			b.append("damageType=");
			b.append(obj.getDefaultDamageType().name());
			b.append(V1Utils.NEWLINE);

			GroupOfPossibilities<SpellEffect> se = obj.getSpellEffects();
			b.append("spellEffects=");
			b.append(se==null?"":spellEffects.toString(se));
			b.append(V1Utils.NEWLINE);

			b.append("spellEffectLevel=");
			b.append(obj.getSpellEffectLevel());
			b.append(V1Utils.NEWLINE);

			b.append("attackScript=");
			b.append(obj.getAttackScript()==null?"":obj.getAttackScript().getName());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static NaturalWeapon fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom FoeAttack impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (NaturalWeapon)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String description = p.getProperty("description");
			boolean isRanged = Boolean.valueOf(p.getProperty("ranged"));
			StatModifier modifiers = V1StatModifier.fromString(p.getProperty("modifiers"));
			int minRange = Integer.parseInt(p.getProperty("minRange"));
			int maxRange = Integer.parseInt(p.getProperty("maxRange"));
			int[] attacks = V1Utils.fromStringInts(p.getProperty("attacks"), ",");
			String slaysFoeType = p.getProperty("slaysFoeType");
			Dice damage = V1Dice.fromString(p.getProperty("damage"));
			MagicSys.SpellEffectType damageType = MagicSys.SpellEffectType.valueOf(p.getProperty("damageType"));
			GroupOfPossibilities<SpellEffect> effects = spellEffects.fromString(p.getProperty("spellEffects"));
			int spellEffectLevel = Integer.parseInt(p.getProperty("spellEffectLevel"));
			String ass = p.getProperty("attackScript");
			MazeScript attackScript = ass.equals("")?null:Database.getInstance().getScript(ass);

			return new NaturalWeapon(
				name,
				description,
				isRanged,
				damage,
				damageType,
				modifiers,
				minRange,
				maxRange,
				effects,
				spellEffectLevel,
				attacks,
				slaysFoeType,
				attackScript);
		}
	}
}

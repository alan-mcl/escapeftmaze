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
import java.util.List;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class V1Spell
{
	static V1GroupOfPossibilties<SpellEffect> spellEffects = new V1GroupOfPossibilties<SpellEffect>()
	{
		public String typeToString(SpellEffect spellEffect)
		{
			return spellEffect.getName();
		}

		public SpellEffect typeFromString(String s)
		{
			return Database.getInstance().getSpellEffect(s);
		}
	};

	static V1List<ManaRequirement> manaRequirements = new V1List<ManaRequirement>()
	{
		public String typeToString(ManaRequirement manaRequirement)
		{
			return manaRequirement.getColour()+":"+manaRequirement.getAmount();
		}

		public ManaRequirement typeFromString(String s)
		{
			String[] strs = s.split(":");
			int colour = Integer.parseInt(strs[0]);
			int amount = Integer.parseInt(strs[1]);
			return new ManaRequirement(colour, amount);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, Spell> load(BufferedReader reader) throws Exception
	{
		Map <String, Spell> result = new HashMap<String, Spell>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			Spell g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, Spell> map) throws Exception
	{
		for (String name : map.keySet())
		{
			Spell g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Spell obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != Spell.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("displayName=");
			b.append(obj.getDisplayName());
			b.append(V1Utils.NEWLINE);
			
			b.append("hitPointCost=");
			b.append(V1Value.toString(obj.getHitPointCost()));
			b.append(V1Utils.NEWLINE);

			b.append("actionPointCost=");
			b.append(V1Value.toString(obj.getActionPointCost()));
			b.append(V1Utils.NEWLINE);

			b.append("magicPointCost=");
			b.append(V1Value.toString(obj.getMagicPointCost()));
			b.append(V1Utils.NEWLINE);

			b.append("targetType=");
			b.append(obj.getTargetType());
			b.append(V1Utils.NEWLINE);

			b.append("school=");
			b.append(obj.getSchool());
			b.append(V1Utils.NEWLINE);

			b.append("book=");
			b.append(obj.getBook().getName());
			b.append(V1Utils.NEWLINE);

			b.append("spellEffects=");
			b.append(spellEffects.toString(obj.getEffects()));
			b.append(V1Utils.NEWLINE);

			b.append("description=");
			b.append(V1Utils.escapeNewlines(obj.getDescription()));
			b.append(V1Utils.NEWLINE);

			b.append("level=");
			b.append(obj.getLevel());
			b.append(V1Utils.NEWLINE);

			b.append("requirementsToLearn=");
			b.append(V1StatModifier.toString(obj.getRequirementsToLearn()));
			b.append(V1Utils.NEWLINE);

			b.append("manaRequirements=");
			b.append(manaRequirements.toString(obj.getRequirementsToCast()));
			b.append(V1Utils.NEWLINE);

			b.append("castByPlayerScript=");
			MazeScript s = obj.getCastByPlayerScript();
			b.append(s==null?"":s.getName());
			b.append(V1Utils.NEWLINE);

			s = obj.getCastByFoeScript();
			b.append("castByFoeScript=");
			b.append(s==null?"":s.getName());
			b.append(V1Utils.NEWLINE);

			b.append("usabilityType=");
			b.append(obj.getUsabilityType());
			b.append(V1Utils.NEWLINE);

			b.append("primaryModifier=");
			b.append(obj.getPrimaryModifier());
			b.append(V1Utils.NEWLINE);

			b.append("secondaryModifier=");
			b.append(obj.getSecondaryModifier());
			b.append(V1Utils.NEWLINE);

			b.append("wildMagicValue=");
			b.append(V1Value.toString(obj.getWildMagicValue()));
			b.append(V1Utils.NEWLINE);

			b.append("wildMagicTable=");
			b.append(V1Utils.toStringStrings(obj.getWildMagicTable(), ","));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Spell fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom Spell impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (Spell)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String displayName = p.getProperty("displayName");
			int targetType = Integer.parseInt(p.getProperty("targetType"));
			String school = p.getProperty("school");
			MagicSys.SpellBook book = MagicSys.SpellBook.valueOf(p.getProperty("book"));
			GroupOfPossibilities<SpellEffect> effects = spellEffects.fromString(p.getProperty("spellEffects"));
			String description = p.getProperty("description");
			int level = Integer.parseInt(p.getProperty("level"));
			StatModifier requirementsToLearn = V1StatModifier.fromString(p.getProperty("requirementsToLearn"));
			List<ManaRequirement> requirementsToCast = manaRequirements.fromString(p.getProperty("manaRequirements"));
			String s = p.getProperty("castByPlayerScript");
			MazeScript castByPlayerScript = s.equals("")?null:Database.getInstance().getScript(s);
			s = p.getProperty("castByFoeScript");
			MazeScript castByFoeScript = s.equals("")?null:Database.getInstance().getScript(s);
			int usabilityType = Integer.parseInt(p.getProperty("usabilityType"));
			String primaryModifier = p.getProperty("primaryModifier");
			String secondaryModifier = p.getProperty("secondaryModifier");
			ValueList wildMagicValue = V1Value.fromString(p.getProperty("wildMagicValue"));
			String[] wildMagicTable = V1Utils.fromStringStrings(p.getProperty("wildMagicTable"), ",");

			ValueList hitPointCost = V1Value.fromString(p.getProperty("hitPointCost"));
			ValueList actionPointCost = V1Value.fromString(p.getProperty("actionPointCost"));
			ValueList magicPointCost = V1Value.fromString(p.getProperty("magicPointCost"));

			return new Spell(
				name,
				displayName,
				hitPointCost,
				actionPointCost,
				magicPointCost,
				description,
				level,
				targetType,
				usabilityType,
				school,
				book,
				effects,
				requirementsToCast,
				requirementsToLearn,
				castByPlayerScript,
				castByFoeScript,
				Stats.Modifier.valueOf(primaryModifier),
				Stats.Modifier.valueOf(secondaryModifier),
				wildMagicValue,
				wildMagicTable);
		}
	}
}

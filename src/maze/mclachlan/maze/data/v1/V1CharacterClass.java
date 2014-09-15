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
import mclachlan.maze.stat.*;

/**
 *
 */
public class V1CharacterClass
{
	public static final String SEP = "/";

	static V1List<LevelAbility> levelAbilityList = new V1List<LevelAbility>("/")
	{
		@Override
		public String typeToString(LevelAbility levelAbility)
		{
			return V1LevelAbility.toString(levelAbility);
		}

		@Override
		public LevelAbility typeFromString(String s)
		{
			return V1LevelAbility.fromString(s);
		}
	};

	static V1List<List<LevelAbility>> levelAbilityProgression = new V1List<List<LevelAbility>>(",")
	{
		@Override
		public String typeToString(List<LevelAbility> levelAbility)
		{
			return levelAbilityList.toString(levelAbility);
		}

		@Override
		public List<LevelAbility> typeFromString(String s)
		{
			return levelAbilityList.fromString(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, CharacterClass> load(BufferedReader reader) throws Exception
	{
		Map <String, CharacterClass> result = new HashMap<String, CharacterClass>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			CharacterClass g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, CharacterClass> cc) throws Exception
	{
		for (String name : cc.keySet())
		{
			CharacterClass g = cc.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(CharacterClass obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != CharacterClass.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("description=");
			b.append(V1Utils.escapeNewlines(obj.getDescription()));
			b.append(V1Utils.NEWLINE);

			b.append("focus=");
			b.append(obj.getFocus());
			b.append(V1Utils.NEWLINE);

			b.append("startingHitPoints=");
			b.append(obj.getStartingHitPoints());
			b.append(V1Utils.NEWLINE);

			b.append("startingActionPoints=");
			b.append(obj.getStartingActionPoints());
			b.append(V1Utils.NEWLINE);

			b.append("startingMagicPoints=");
			b.append(obj.getStartingMagicPoints());
			b.append(V1Utils.NEWLINE);

			b.append("startingModifiers=");
			b.append(V1StatModifier.toString(obj.getStartingModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("unlockModifiers=");
			b.append(V1StatModifier.toString(obj.getUnlockModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("startingActiveModifiers=");
			b.append(V1StatModifier.toString(obj.getStartingActiveModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("allowedGenders=");
			Set<String> allowedGenders = obj.getAllowedGenders();
			ArrayList<String> list = allowedGenders==null?new ArrayList<String>():new ArrayList<String>(allowedGenders);
			b.append(V1Utils.stringList.toString(list));
			b.append(V1Utils.NEWLINE);

			b.append("allowedRaces=");
			Set<String> allowedRaces = obj.getAllowedRaces();
			list = allowedRaces==null?new ArrayList<String>():new ArrayList<String>(allowedRaces);
			b.append(V1Utils.stringList.toString(list));
			b.append(V1Utils.NEWLINE);

			b.append("experienceTable=");
			b.append(obj.getExperienceTable().getName());
			b.append(V1Utils.NEWLINE);

			b.append("levelUpHitPoints=");
			b.append(V1Dice.toString(obj.getLevelUpHitPoints()));
			b.append(V1Utils.NEWLINE);

			b.append("levelUpActionPoints=");
			b.append(V1Dice.toString(obj.getLevelUpActionPoints()));
			b.append(V1Utils.NEWLINE);

			b.append("levelUpMagicPoints=");
			b.append(V1Dice.toString(obj.getLevelUpMagicPoints()));
			b.append(V1Utils.NEWLINE);

			b.append("levelUpAssignableModifiers=");
			b.append(obj.getLevelUpAssignableModifiers());
			b.append(V1Utils.NEWLINE);

			b.append("levelUpModifiers=");
			b.append(V1StatModifier.toString(obj.getLevelUpModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("progression=");
			b.append(levelAbilityProgression.toString(obj.getProgression().getLevelAbilities()));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static CharacterClass fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom CharacterClass impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (CharacterClass)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String description = V1Utils.replaceNewlines(p.getProperty("description"));
			CharacterClass.Focus focus = CharacterClass.Focus.valueOf(p.getProperty("focus"));
			int startingHitPoints = Integer.parseInt(p.getProperty("startingHitPoints"));
			int startingActionPoints = Integer.parseInt(p.getProperty("startingActionPoints"));
			int startingMagicPoints = Integer.parseInt(p.getProperty("startingMagicPoints"));
			StatModifier startingModifiers = V1StatModifier.fromString(p.getProperty("startingModifiers"));
			StatModifier unlockModifiers = V1StatModifier.fromString(p.getProperty("unlockModifiers"));
			StatModifier startingActiveModifiers = V1StatModifier.fromString(p.getProperty("startingActiveModifiers"));
			String asb = p.getProperty("availableSpellBooks");
			List<String> genders = V1Utils.stringList.fromString(p.getProperty("allowedGenders"));
			Set<String> allowedGenders = genders==null?null:new HashSet<String>(genders);
			List<String> races = V1Utils.stringList.fromString(p.getProperty("allowedRaces"));
			Set<String> allowedRaces = races==null?null:new HashSet<String>(races);
			ExperienceTable experienceTable = Database.getInstance().getExperienceTable(p.getProperty("experienceTable"));
			Dice levelUpHitpoints = V1Dice.fromString(p.getProperty("levelUpHitPoints"));
			Dice levelUpActionPoints = V1Dice.fromString(p.getProperty("levelUpActionPoints"));
			Dice levelUpMagicpoints = V1Dice.fromString(p.getProperty("levelUpMagicPoints"));
			int levelUpAssignableModifiers = Integer.parseInt(p.getProperty("levelUpAssignableModifiers"));
			StatModifier levelUpModifiers = V1StatModifier.fromString(p.getProperty("levelUpModifiers"));

			String progS = p.getProperty("progression");
			List<List<LevelAbility>> list = levelAbilityProgression.fromString(progS);
			LevelAbilityProgression progression = new LevelAbilityProgression(list);

			return new CharacterClass(
				name,
				focus,
				description,
				startingHitPoints,
				startingActionPoints,
				startingMagicPoints,
				startingActiveModifiers,
				startingModifiers,
				unlockModifiers,
				allowedGenders,
				allowedRaces,
				experienceTable,
				levelUpHitpoints,
				levelUpActionPoints,
				levelUpMagicpoints,
				levelUpAssignableModifiers,
				levelUpModifiers,
				progression);
		}
	}
}

/*
 * Copyright (c) 2014 Alan McLachlan
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

import java.util.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1LevelAbility
{
	public static final String SEP = ";";
	public static Map<Class, Integer> types;

	public static final int CUSTOM = 0;
	public static final int STAT_MODIFIER = 1;
	public static final int BANNER_MODIFIER = 2;
	public static final int SPECIAL_ABILITY = 3;
	public static final int SPELL_PICKS = 4;

	/*-------------------------------------------------------------------------*/
	static
	{
		types = new HashMap<Class, Integer>();

		types.put(StatModifierLevelAbility.class, STAT_MODIFIER);
		types.put(BannerModifierLevelAbility.class, BANNER_MODIFIER);
		types.put(SpecialAbilityLevelAbility.class, SPECIAL_ABILITY);
		types.put(AddSpellPicks.class, SPELL_PICKS);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(LevelAbility la)
	{
		if (la == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(la.getClass()))
		{
			type = types.get(la.getClass());
		}
		else
		{
			type = CUSTOM;
		}

		s.append(type);
		s.append(SEP);
		s.append(la.getKey());
		s.append(SEP);
		s.append(la.getDisplayName());
		s.append(SEP);
		s.append(la.getDescription());
		s.append(SEP);

		switch (type)
		{
			case CUSTOM:
				s.append(la.getClass().getName());
				break;
			case STAT_MODIFIER:
				StatModifierLevelAbility smla = (StatModifierLevelAbility)la;
				s.append(V1StatModifier.toString(smla.getModifier(), "@"));
				break;
			case BANNER_MODIFIER:
				BannerModifierLevelAbility bmla = (BannerModifierLevelAbility)la;
				s.append(V1StatModifier.toString(bmla.getModifier(), "@"));
				break;
			case SPECIAL_ABILITY:
				SpecialAbilityLevelAbility sala = (SpecialAbilityLevelAbility)la;
				s.append(V1SpellLikeAbility.toString(sala.getAbility()));
				break;
			case SPELL_PICKS:
				AddSpellPicks asp = (AddSpellPicks)la;
				s.append(asp.getSpellPicks());
				break;
			default: throw new MazeException("invalid: "+type);
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static LevelAbility fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP);
		int index = 0;
		int type = Integer.parseInt(strs[index++]);
		String key = strs[index++];
		String displayName = strs[index++];
		String description = strs[index++];

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[1]);
					return (LevelAbility)clazz.newInstance();
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			case STAT_MODIFIER:
				return new StatModifierLevelAbility(
					key,
					displayName,
					description,
					V1StatModifier.fromString(strs[index++], "@"));
			case BANNER_MODIFIER:
				return new BannerModifierLevelAbility(
					key,
					displayName,
					description,
					V1StatModifier.fromString(strs[index++], "@"));
			case SPECIAL_ABILITY:
				return new SpecialAbilityLevelAbility(
					key,
					displayName,
					description,
					V1SpellLikeAbility.fromString(strs[index++]));
			case SPELL_PICKS:
				return new AddSpellPicks(
					key,
					displayName,
					description,
					Integer.parseInt(strs[index++]));
			default: throw new MazeException("invalid: "+type);
		}
	}
}

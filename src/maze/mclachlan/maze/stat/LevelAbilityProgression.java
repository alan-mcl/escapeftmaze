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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.stat.magic.MagicSys;

/**
 * A class to store the progression of abilities by level.
 */
public class LevelAbilityProgression
{
	public static final int MAX_LEVELS = 20;

	/**
	 * The index of the list is the level, the contents are the list of
	 * the abilities gained at that level.
	 */
	private List<List<LevelAbility>> progression = new ArrayList<List<LevelAbility>>();

	/*-------------------------------------------------------------------------*/
	public LevelAbilityProgression()
	{
		this(new ArrayList<List<LevelAbility>>(MAX_LEVELS));
	}

	/*-------------------------------------------------------------------------*/
	public LevelAbilityProgression(
		List<List<LevelAbility>> progression)
	{
		this.progression = progression;

		if (this.progression.size() < MAX_LEVELS)
		{
			for (int i=this.progression.size(); i<MAX_LEVELS; i++)
			{
				progression.add(null);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void add(LevelAbility la, int level)
	{
		if (level > MAX_LEVELS)
		{
			return;
		}

		List<LevelAbility> list = progression.get(level - 1);

		if (list == null)
		{
			list = new ArrayList<LevelAbility>();
			progression.set(level-1, list);
		}

		list.add(la);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param level
	 * 	the class level, from 1..{@link #MAX_LEVELS}
	 * @return
	 * 	abilities granted at the specific level. Returns an empty list if there
	 * 	are none, never returns null.
	 */
	public List<LevelAbility> getForLevel(int level)
	{
		if (level > MAX_LEVELS)
		{
			return new ArrayList<LevelAbility>();
		}
		List<LevelAbility> result = progression.get(level - 1);
		return result==null?new ArrayList<LevelAbility>():result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	abilities granted by the given level and all levels below it
	 */
	public List<LevelAbility> getForLevelCumulative(int level)
	{
		List<LevelAbility> result = new ArrayList<LevelAbility>();

		Map<String, LevelAbility> keyed = new HashMap<String, LevelAbility>();

		for (int i=1; i<=level; i++)
		{
			List<LevelAbility> forLevel = getForLevel(i);

			for (LevelAbility la : forLevel)
			{
				if (la.getKey() != null && la.getKey().length() > 0)
				{
					if (keyed.containsKey(la.getKey()))
					{
						// this ability replaces the lower level ability
						result.remove(keyed.get(la.getKey()));
					}

					keyed.put(la.getKey(), la);
				}
				result.add(la);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setForLevel(int level, List<LevelAbility> abilities)
	{
		progression.set(level - 1, abilities);
	}

	/*-------------------------------------------------------------------------*/
	public List<List<LevelAbility>> getLevelAbilities()
	{
		return progression;
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacterClass(CharacterClass characterClass)
	{
		for (LevelAbility la : getForLevel(MAX_LEVELS))
		{
			la.setCharacterClass(characterClass.getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasMagicAbility(int atLevel)
	{
		List<LevelAbility> abilities = getForLevelCumulative(atLevel);

		for (LevelAbility la : abilities)
		{
			StatModifier modifier = la.getModifier();

			for (MagicSys.SpellBook sb : MagicSys.getInstance().getSpellBooks())
			{
				if (modifier.getModifier(sb.getCastingAbilityModifier()) > 0)
				{
					return true;
				}
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasSpellPicks(int atLevel)
	{
		List<LevelAbility> abilities = getForLevelCumulative(atLevel);

		for (LevelAbility la : abilities)
		{
			if (la instanceof AddSpellPicks)
			{
				return true;
			}
		}

		return false;
	}
}

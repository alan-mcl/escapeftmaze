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

package mclachlan.maze.data;

import mclachlan.maze.data.v1.V1Value;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class StringUtil
{
	// namespaces
	private static final String GAMESYS = "gamesys";
	private static final String TIPS = "tips";
	private static final String UI = "ui";
	private static final String CAMPAIGN = "campaign";
	private static final String EVENT = "event";

	/*-------------------------------------------------------------------------*/
	public static String getGamesysString(String key)
	{
		return getGamesysString(key, false);
	}

	/*-------------------------------------------------------------------------*/
	public static String getGamesysString(String key, boolean allowNull, Object... args)
	{
		return getString(GAMESYS, key, allowNull, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String getModifierDescription(String modifier)
	{
		return getGamesysString("modifier_desc-" + modifier);
	}

	/*-------------------------------------------------------------------------*/
	public static String getModifierName(String modifier)
	{
		return getGamesysString("modifier_name-" + modifier);
	}

	/*-------------------------------------------------------------------------*/
	public static String getTipOfTheDayText(int index)
	{
		return Database.getInstance().getString(TIPS, "tip_"+index, true);
	}

	/*-------------------------------------------------------------------------*/
	public static String getUiLabel(String key)
	{
		return getUiLabel(key, new Object[]{});
	}

	/*-------------------------------------------------------------------------*/
	public static String getUiLabel(String key, Object... args)
	{
		return getString(UI, key, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String getEventText(String key, Object... args)
	{
		return getString(EVENT, key, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String getString(String namespace, String key, Object[] args)
	{
		return getString(namespace, key, false, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String getString(String namespace, String key, boolean allowNull, Object[] args)
	{
		String string = Database.getInstance().getString(namespace, key, allowNull);
		return String.format(string, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String getCampaignText(String key, Object... args)
	{
		return getString(CAMPAIGN, key, args);
	}

	/*-------------------------------------------------------------------------*/
	public static String descModifier(String modifier, int value)
	{
		Stats.ModifierMetric metric = Stats.ModifierMetric.getMetric(modifier);
		switch (metric)
		{
			case PLAIN:
				return descPlainModifier(value);
			case BOOLEAN:
				if (value >= 0)
				{
					return "";
				}
				else
				{
					return "cancelled";
				}
			case PERCENTAGE:
				return descPlainModifier(value)+"%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+"+value;
		}
		else
		{
			return ""+value;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String descValue(Value v)
	{
		// todo: prettier printing
		return V1Value.toString(v);
	}

	/*-------------------------------------------------------------------------*/
	public static String descSpellTargetType(int targetType)
	{
		return MagicSys.SpellTargetType.describe(targetType);
	}

	/*-------------------------------------------------------------------------*/
	public static Object descSpellUsabilityType(int usabilityType)
	{
		return MagicSys.SpellUsabilityType.describe(usabilityType);
	}

	/*-------------------------------------------------------------------------*/
	public static String truncateWithEllipses(String s, int length)
	{
		if (s.length() < length)
		{
			return s;
		}
		else
		{
			return s.substring(0, length-3) + "...";
		}
	}
}

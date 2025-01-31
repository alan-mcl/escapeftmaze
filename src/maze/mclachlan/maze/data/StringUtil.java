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

import java.util.*;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.Stats;
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
	public static String getModifierDescription(Stats.Modifier modifier)
	{
		return getGamesysString("modifier_desc-" + modifier.getResourceBundleKey());
	}

	/*-------------------------------------------------------------------------*/
	public static String getModifierName(Stats.Modifier modifier)
	{
		return getGamesysString("modifier_name-" + modifier.getResourceBundleKey());
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
	public static String descModifier(Stats.Modifier modifier, int value)
	{
		Stats.ModifierMetric metric = modifier.getMetric();
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
	public static String descValue(ValueList v)
	{
		StringBuilder result = new StringBuilder();

		for (Value value : v.getValues())
		{
			String vd = descValue(value);

			if (value.shouldNegate())
			{
				result.append(" - ").append(vd);
			}
			else if (result.length() > 0)
			{
				result.append(" + ").append(vd);
			}
			else
			{
				result.append(vd);
			}

		}

		return result.toString();
	}

	/*-------------------------------------------------------------------------*/
	private static String descValue(Value v)
	{
		String result;

		if (v instanceof MagicPresentValue)
		{
			String mc = MagicSys.MagicColour.describe(((MagicPresentValue)v).getColour());
			result = StringUtil.getUiLabel("vd.magic.present", mc);
		}
		else if (v instanceof ModifierValue)
		{
			result = StringUtil.getModifierName(((ModifierValue)v).getModifier());
		}
		else if (v instanceof DiceValue)
		{
			result = V1Dice.toString(((DiceValue)v).getDice());
		}
		else if (v.getClass() == Value.class)
		{
			result = String.valueOf(v.getValue());
		}
		else
		{
			// custom value class
			result = v.toString();
		}

		switch (v.getScaling())
		{
			case NONE ->
			{
			}
			case SCALE_WITH_CASTING_LEVEL ->
			{
				result = StringUtil.getUiLabel("vd.scale.casting.level", result);
			}
			case SCALE_WITH_CHARACTER_LEVEL ->
			{
				result = StringUtil.getUiLabel("vd.scale.character.level", result);
			}
			case SCALE_WITH_CLASS_LEVEL ->
			{
				result = StringUtil.getUiLabel("vd.scale.class.level", result, v.getReference());
			}
			case SCALE_WITH_MODIFIER ->
			{
				result = StringUtil.getUiLabel("vd.scale.modifier", result, v.getReference());
			}
			case SCALE_WITH_PARTY_SIZE ->
			{
				result = StringUtil.getUiLabel("vd.scale.party", result);
			}
		}


		return result;
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

	/*-------------------------------------------------------------------------*/
	public static String getCommaString(Collection strings)
	{
		StringBuilder sb = new StringBuilder();
		boolean commaEd = false;

		for (Object obj : strings)
		{
			if (commaEd)
			{
				sb.append(", ");
			}
			commaEd = true;
			sb.append(obj.toString());
		}

		return sb.toString();
	}

}

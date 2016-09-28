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

import java.math.BigInteger;
import java.util.*;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1StatModifier
{
	private static final int MAX_MODIFIER = Byte.MAX_VALUE;
	private static final int MIN_MODIFIER = Byte.MIN_VALUE;
	private static final String DEFAULT_SEPARATOR = ",";

	/**
	 * Sorted in order of ID. A performance optimisation for the serialization.
	 */
	private static List<Stats.Modifier> sortedModifiers;

	/*-------------------------------------------------------------------------*/
	static
	{
		sortedModifiers = new ArrayList<Stats.Modifier>();
		sortedModifiers.addAll(Arrays.asList(Stats.Modifier.values()));

		// special case, we never want to persist or retrieve the NONE modifier
		sortedModifiers.remove(Stats.Modifier.NONE);

		Collections.sort(sortedModifiers, new Comparator<Stats.Modifier>()
		{
			@Override
			public int compare(Stats.Modifier o1, Stats.Modifier o2)
			{
				return o1.getId() - o2.getId();
			}
		});
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(StatModifier sm)
	{
		return toString(sm, DEFAULT_SEPARATOR);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(StatModifier sm, String separator)
	{
		if (sm == null)
		{
			return "";
		}

		BigInteger bitmap = BigInteger.valueOf(0);
		StringBuilder buffer = new StringBuilder();

		// Sorted modifiers guarantee ascending ID order so we can loop once here
		for (Stats.Modifier modifier : sortedModifiers)
		{
			int modifierValue = sm.getModifier(modifier);
			if (modifierValue != 0)
			{
				if (modifierValue > MAX_MODIFIER || modifierValue < MIN_MODIFIER)
				{
					throw new MazeException("modifier [" + modifier + "] out of bounds: " + modifierValue);
				}

				// inefficient
				bitmap = bitmap.setBit(modifier.getId());
				String hex = Integer.toHexString(modifierValue);
				if (hex.length() == 1)
				{
					buffer.append('0');
				}
				else if (hex.length() > 2)
				{
					// a negative number: just take the last two hex characters.
					// this does of course limit us to -128..127
					hex = hex.substring(hex.length()-2, hex.length());
				}
				buffer.append(hex);
			}
		}

		buffer.insert(0, toString(bitmap)+separator);

		return buffer.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static StatModifier fromString(String s)
	{
		return fromString(s, DEFAULT_SEPARATOR);
	}

	/*-------------------------------------------------------------------------*/
	public static StatModifier fromString(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		StatModifier result = new StatModifier();
		String[] strs = s.split(separator);
		BigInteger bi = new BigInteger(strs[0], 16);

		int counter = 0;

		// sorted modifiers in ID order mean we can loop once here
		for (Stats.Modifier modifier : sortedModifiers)
		{
			if (bi.testBit(modifier.getId()))
			{
				String modifierHex = strs[1].substring(counter, counter + 2);
				result.setModifier(modifier, (byte)(Integer.parseInt(modifierHex, 16)));
				counter += 2;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	static String toString(BigInteger bi)
	{
		return bi.toString(16);
	}
}

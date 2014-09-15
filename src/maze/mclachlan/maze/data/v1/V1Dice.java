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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Dice
{
	static Pattern diceRegex;

	static
	{
		diceRegex = Pattern.compile("(\\d+)d(\\d+)([+|-]\\d+)?");
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(Dice d)
	{
		if (d == null)
		{
			return "";
		}
		return d.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Dice fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		try
		{
			Matcher matcher = diceRegex.matcher(s);
			matcher.find();

			int nrDice = Integer.parseInt(matcher.group(1));
			int dieSize = Integer.parseInt(matcher.group(2));
			String modifierStr = matcher.group(3);
			int modifier;
			if (modifierStr != null)
			{
				if (modifierStr.startsWith("+"))
				{
					modifierStr = modifierStr.substring(1);
				}
				modifier = Integer.parseInt(modifierStr);
			}
			else
			{
				modifier = 0;
			}
			return new Dice(nrDice, dieSize, modifier);
		}
		catch (Exception x)
		{
			throw new MazeException("Invalid dice string ["+s+"]", x);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
		test(1, 4, 1);
		test(2, 4, -1);
		test(1, 4, 0);
		test(11, 4, 6);
		test(11, 44, 66);
		test(11, 44, -99);
	}

	private static void test(int numberOfDice, int diceMax, int modifier)
	{
		Dice d = new Dice(numberOfDice,diceMax,modifier);
		String s = toString(d);

		System.out.println("s = [" + s + "]");

		Dice test = fromString(s);
		System.out.println("test = [" + test + "]");
	}
}

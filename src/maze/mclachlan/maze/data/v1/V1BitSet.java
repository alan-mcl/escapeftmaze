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

import java.util.BitSet;

/**
 *
 */
public class V1BitSet
{
	/*-------------------------------------------------------------------------*/
	public static String toString(BitSet b)
	{
		if (b == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int max = b.length();
		for (int i=0; i<max; i++)
		{
			s.append(b.get(i)?'1':'0');
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static BitSet fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		int max = s.length();
		BitSet result = new BitSet(max);
		for (int i=0; i<max; i++)
		{
			result.set(i, s.charAt(i)=='1');
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
		BitSet test = new BitSet();
		test.set(2);
		test.set(4);
		test.set(7);
		String s = toString(test);
		System.out.println("s = [" + s + "]");

		test = fromString(s);
		System.out.println("test = [" + test + "]");

		s = toString(test);
		System.out.println("s = [" + s + "]");
	}
}

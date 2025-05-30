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

import java.awt.*;

/**
 *
 */
public class V1Point
{
	public static final String SEP = ":";

	/*-------------------------------------------------------------------------*/
	public static String toString(Point t)
	{
		return toString(t, SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(Point t, String sep)
	{
		if (t == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		s.append(t.x);
		s.append(sep);
		s.append(t.y);

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Point fromString(String s)
	{
		return fromString(s, SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static Point fromString(String s, String sep)
	{
		if (s == null || s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(sep);
		return new Point(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
	}
}

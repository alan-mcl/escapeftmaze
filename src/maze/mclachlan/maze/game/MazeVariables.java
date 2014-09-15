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

package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MazeVariables
{
	private static Map<String, String> vars = new HashMap<String, String>();
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the given maze variable. Maze variable names may not contain
	 * whitespace or be null.
	 * 
	 * @param variable
	 * 	The maze variable to set
	 * @param value
	 * 	The value to set it to
	 * @return
	 * 	The previous value of the maze variable, null if none
	 * @throws MazeException
	 * 	If the maze variable name is invalid
	 */ 
	public static String set(String variable, String value)
	{
		if (variable == null || variable.indexOf(" ") >= 0)
		{
			throw new MazeException("Invalid maze variable name: ["+variable+"]");
		}
		String existing = vars.get(variable);
		vars.put(variable, value);
		return existing;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the value of the given maze variable.
	 */ 
	public static String get(String variable)
	{
		return vars.get(variable);
	}
	
	/*-------------------------------------------------------------------------*/
	public static boolean getBoolean(String variable)
	{
		return Boolean.valueOf(get(variable));
	}

	/*-------------------------------------------------------------------------*/
	public static long getLong(String variable)
	{
		return Long.valueOf(get(variable));
	}

	/*-------------------------------------------------------------------------*/
	public static int getInt(String variable)
	{
		return Integer.valueOf(get(variable));
	}

	/*-------------------------------------------------------------------------*/
	public static Map<String, String> getVars()
	{
		return vars;
	}

	/*-------------------------------------------------------------------------*/
	public static void clear(String variable)
	{
		vars.remove(variable);
	}
	
	/*-------------------------------------------------------------------------*/
	public static void clearAll()
	{
		vars.clear();
	}
}

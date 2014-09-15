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
import java.util.Properties;
import mclachlan.maze.game.MazeVariables;

/**
 *
 */
public class V1MazeVariables
{
	/*-------------------------------------------------------------------------*/
	public static void load(BufferedReader reader) throws Exception
	{
		MazeVariables.getVars().clear();
		Properties p = V1Utils.getProperties(reader);
		for (Object name : p.keySet())
		{
			MazeVariables.set((String)name, (String)p.get(name));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer) throws Exception
	{
		for (String name : MazeVariables.getVars().keySet())
		{
			String value = MazeVariables.get(name);
			writer.write(name+'='+value);
			writer.newLine();
		}
		writer.write("@");
	}
}

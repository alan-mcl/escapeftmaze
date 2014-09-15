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

import mclachlan.crusader.MouseClickScript;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;

/**
 *
 */
public class V1MouseClickScript
{
	/*-------------------------------------------------------------------------*/
	public static String toString(MouseClickScript script)
	{
		if (script == null)
		{
			return "";
		}
		
		MouseClickScriptAdapter adapter = (MouseClickScriptAdapter)script;

		return V1TileScript.toString(adapter.getScript());
	}

	/*-------------------------------------------------------------------------*/
	public static MouseClickScript fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		return new MouseClickScriptAdapter(V1TileScript.fromString(s));
	}
}

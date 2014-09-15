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

import java.util.*;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.ZoneScript;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1ZoneScript
{
	static final String SEP = ",";
	static final String PT_ROW_SEP = ";";
	static final String PT_COL_SEP = ":";
	static Map<Class, Integer> types;

	private static final int CUSTOM = 0;
	private static final int DEFAULT = 1;

	static V1PercentageTable<String> ambientScriptsTable =
		new V1PercentageTable<String>()
	{
		public String typeFromString(String s)
		{
			return s;
		}

		public String typeToString(String s)
		{
			return s;
		}
	};

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(DefaultZoneScript.class, DEFAULT);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(ZoneScript zs)
	{
		if (zs == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(zs.getClass()))
		{
			type = types.get(zs.getClass());
		}
		else
		{
			type = CUSTOM;
		}

		s.append(type);

		if (type == CUSTOM)
		{
			s.append(SEP);
			s.append(zs.getClass().getName());
		}
		else if (type == DEFAULT)
		{
			DefaultZoneScript dzs = (DefaultZoneScript)zs;

			s.append(SEP);
			s.append(dzs.getTurnsBetweenChange());
			s.append(SEP);
			s.append(dzs.getLightLevelDiff());
			s.append(SEP);
			s.append(
				ambientScriptsTable.toString(
					dzs.getAmbientScripts(),
					PT_ROW_SEP,
					PT_COL_SEP));
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static ZoneScript fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		String[] strs = s.split(SEP,-1);
		int type = Integer.parseInt(strs[0]);

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[1]);
					return (ZoneScript)clazz.newInstance();
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
			case DEFAULT:
				int turnsBetweenChanges = Integer.parseInt(strs[1]);
				int lightLevelDiff = Integer.parseInt(strs[2]);
				PercentageTable<String> scripts =
					ambientScriptsTable.fromString(strs[3], PT_ROW_SEP, PT_COL_SEP);
				return new DefaultZoneScript(turnsBetweenChanges, lightLevelDiff, scripts);
			default: throw new MazeException("Invalid type: "+type);
		}
	}
}

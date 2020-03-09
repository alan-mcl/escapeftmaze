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
import java.util.*;
import mclachlan.maze.stat.FoeType;
import mclachlan.maze.stat.Race;

/**
 *
 */
public class V1FoeTypes
{
	public static Map<String, FoeType> load(BufferedReader reader)
	{
		Map<String, Race> load = V1Race.load(reader);

		Map<String, FoeType> result = new HashMap<String, FoeType>();
		for (Race r : load.values())
		{
			result.put(r.getName(), new FoeType(r));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, FoeType> foeTypes) throws Exception
	{
		Map<String, Race> map = new HashMap<String, Race>();
		for (FoeType ft : foeTypes.values())
		{
			map.put(ft.getName(), new Race(ft));
		}
		V1Race.save(writer, map);
	}
}

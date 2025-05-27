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
import java.io.File;
import java.io.FileReader;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Loader
{
	public Zone getZone(String name)
	{
		File file = new File("data/default/db/" +V1Utils.ZONES+name+".txt");
		Zone result = null;

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			result = V1Zone.load(reader);
			reader.close();
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}

		return result;
	}
}

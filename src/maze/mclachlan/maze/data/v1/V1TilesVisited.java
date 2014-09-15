/*
 * Copyright (c) 2012 Alan McLachlan
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

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.game.PlayerTilesVisited;

/**
 *
 */
public class V1TilesVisited
{
	/*-------------------------------------------------------------------------*/
	public static PlayerTilesVisited load(BufferedReader reader) throws Exception
	{
		PlayerTilesVisited result = new PlayerTilesVisited();
		
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			String zone = p.getProperty("zone");
			String[] visited = p.getProperty("visited").split(",");

			for (String s : visited)
			{
				result.visitTile(zone, V1Point.fromString(s));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, PlayerTilesVisited visited) throws Exception
	{
		for (String zoneName : visited.getZoneNames())
		{
			writer.write("zone=" + zoneName);
			writer.newLine();

			StringBuilder sb = new StringBuilder("visited=");
			for (Point p : visited.getTilesVisited(zoneName))
			{
				sb.append(V1Point.toString(p)).append(",");
			}
			writer.write(sb.toString());
			writer.newLine();

			writer.write("@");
			writer.newLine();
		}
	}
}

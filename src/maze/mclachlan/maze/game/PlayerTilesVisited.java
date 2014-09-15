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

package mclachlan.maze.game;

import java.awt.Point;
import java.util.*;

/**
 *
 */
public class PlayerTilesVisited
{
	/**
	 * Mao of tiles visited.<p>
	 * KEY: Zone name <br>
	 * VALUE: List of Points representing tiles visited in that zone.
	 */
	private Map<String, List<Point>> tilesVisited;

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited()
	{
		tilesVisited = new HashMap<String, List<Point>>();
	}

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited(Map<String, List<Point>> tilesVisited)
	{
		this.tilesVisited = tilesVisited;
	}

	/*-------------------------------------------------------------------------*/
	public void visitTile(String zoneName, Point tile)
	{
		List<Point> visited = tilesVisited.get(zoneName);

		if (visited == null)
		{
			visited = new ArrayList<Point>();
			tilesVisited.put(zoneName, visited);
		}

		if (!visited.contains(tile))
		{
			visited.add(tile);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Point> getTilesVisited(String zoneName)
	{
		if (tilesVisited.containsKey(zoneName))
		{
			return new ArrayList<Point>(tilesVisited.get(zoneName));
		}
		else
		{
			return new ArrayList<Point>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeTileVisited(String zoneName, Point tile)
	{
		if (tilesVisited.containsKey(zoneName))
		{
			tilesVisited.get(zoneName).remove(tile);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		return new ArrayList<String>(tilesVisited.keySet());
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasVisited(String zoneName, Point p)
	{
		if (tilesVisited.containsKey(zoneName))
		{
			return tilesVisited.get(zoneName).contains(p);
		}
		else
		{
			return false;
		}
	}
}

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
	 * Map of tiles visited.<p>
	 * KEY: {@link mclachlan.maze.map.Zone#getTilesVisitedKey()} (defaults to
	 * zone name)<br>
	 * VALUE: List of Points representing tiles visited in that zone.
	 */
	private Map<String, List<Point>> tilesVisited;

	private List<Point> recentTiles;

	public static final int MAX_RECENT_TILES = 10;
	public static final String RECENT_TILES_KEY = "__RECENT_TILES_KEY__";

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited()
	{
		tilesVisited = new HashMap<>();
		recentTiles = new ArrayList<>();

		tilesVisited.put(RECENT_TILES_KEY, recentTiles);
	}

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited(Map<String, List<Point>> tilesVisited)
	{
		this.tilesVisited = tilesVisited;
		recentTiles = tilesVisited.computeIfAbsent(RECENT_TILES_KEY, k -> new ArrayList<>());
	}

	/*-------------------------------------------------------------------------*/
	public void visitTile(String tilesVisitedKey, Point tile)
	{
		List<Point> visited = tilesVisited.computeIfAbsent(tilesVisitedKey, k -> new ArrayList<>());

		if (!visited.contains(tile))
		{
			visited.add(tile);
		}

		recentTiles.add(tile);

		if (recentTiles.size() > MAX_RECENT_TILES)
		{
			recentTiles.remove(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Point> getTilesVisited(String tilesVisitedKey)
	{
		if (tilesVisited.containsKey(tilesVisitedKey))
		{
			return new ArrayList<>(tilesVisited.get(tilesVisitedKey));
		}
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public void removeTileVisited(String tilesVisitedKey, Point tile)
	{
		if (tilesVisited.containsKey(tilesVisitedKey))
		{
			tilesVisited.get(tilesVisitedKey).remove(tile);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		return new ArrayList<>(tilesVisited.keySet());
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasVisited(String tilesVisitedKey, Point p)
	{
		if (tilesVisited.containsKey(tilesVisitedKey))
		{
			return tilesVisited.get(tilesVisitedKey).contains(p);
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/

	public Map<String, List<Point>> getTilesVisited()
	{
		return tilesVisited;
	}

	public void setTilesVisited(
		Map<String, List<Point>> tilesVisited)
	{
		this.tilesVisited = tilesVisited;
	}

	public List<Point> getRecentTiles()
	{
		return tilesVisited.get(RECENT_TILES_KEY);
	}

	public void setRecentTiles(List<Point> recentTiles)
	{
		this.recentTiles = recentTiles;
		tilesVisited.put(RECENT_TILES_KEY, recentTiles);
	}

	public void resetRecentTiles()
	{
		setRecentTiles(new ArrayList<>());
	}

	/*-------------------------------------------------------------------------*/

/*	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof PlayerTilesVisited))
		{
			return false;
		}

		PlayerTilesVisited that = (PlayerTilesVisited)o;

		return getTilesVisited() != null ? getTilesVisited().equals(that.getTilesVisited()) : that.getTilesVisited() == null;
	}

	@Override
	public int hashCode()
	{
		return getTilesVisited() != null ? getTilesVisited().hashCode() : 0;
	}*/
}

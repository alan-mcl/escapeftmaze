/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.campaign.temple;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Loot;

/**
 * Post-processes a generated temple floor: depth-keyed loot. Stair portals are
 * applied by {@link TempleStairwellDresser}. Mutation keys are per-depth.
 */
public final class TempleFloorDressing
{
	public static final String ASCEND_SCRIPT = TempleStairLinks.HUB_ASCEND_SCRIPT;
	public static final String ASCEND_PREV_SCRIPT = TempleStairLinks.ASCEND_PREV_SCRIPT;
	public static final String DESCEND_SCRIPT = TempleStairLinks.DESCEND_NEXT_SCRIPT;

	private TempleFloorDressing()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel)
	{
		dress(zone, dungeonLevel, Set.of());
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel, Set<Point> avoidTiles)
	{
		Point origin = zone.getPlayerOrigin();
		List<Point> encounterTiles = findEncounterTiles(zone);

		Set<Point> avoid = new HashSet<>(avoidTiles);
		Point upPortal = findStairsUpPortalFrom(zone);
		Point downPortal = findStairsDownPortalFrom(zone);
		if (upPortal != null)
		{
			avoid.add(upPortal);
		}
		if (downPortal != null)
		{
			avoid.add(downPortal);
		}

		int lootCount = TempleDepthScaler.lootPlacements(dungeonLevel);
		List<Point> lootTiles = pickLootTiles(origin, encounterTiles, avoid, lootCount);
		String lootTable = TempleDepthScaler.lootTableName(dungeonLevel);
		int i = 0;
		for (Point p : lootTiles)
		{
			placeLoot(zone, p, lootTable, TempleSeeds.lootVar(dungeonLevel, i++));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String encounterTableName(int dungeonLevel)
	{
		return TempleDepthScaler.encounterTableName(dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	public static String lootTableName(int dungeonLevel)
	{
		return TempleDepthScaler.lootTableName(dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	public static List<Point> findEncounterTiles(Zone zone)
	{
		List<Point> result = new ArrayList<>();
		Tile[][] tiles = zone.getTiles();
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				Tile tile = tiles[x][y];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof mclachlan.maze.map.script.Encounter)
					{
						result.add(new Point(x, y));
						break;
					}
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsUpPortalFrom(Zone zone)
	{
		Point up = findPortalFrom(zone, ASCEND_SCRIPT);
		if (up != null)
		{
			return up;
		}
		return findPortalFrom(zone, ASCEND_PREV_SCRIPT);
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsDownPortalFrom(Zone zone)
	{
		return findPortalFrom(zone, DESCEND_SCRIPT);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsUpPortalFrom} */
	public static Point findStairsUpTile(Zone zone)
	{
		return findStairsUpPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsDownPortalFrom} */
	public static Point findStairsDownTile(Zone zone)
	{
		return findStairsDownPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsUpPortalFrom} */
	public static Point findStairsTile(Zone zone)
	{
		return findStairsUpPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	private static Point findPortalFrom(Zone zone, String mazeScript)
	{
		if (zone.getPortals() == null)
		{
			return null;
		}
		for (Portal portal : zone.getPortals())
		{
			if (mazeScript.equals(portal.getMazeScript()))
			{
				return portal.getFrom();
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> pickLootTiles(
		Point origin,
		List<Point> encounterTiles,
		Set<Point> avoidTiles,
		int count)
	{
		List<Point> candidates = new ArrayList<>();
		for (Point p : encounterTiles)
		{
			if (!p.equals(origin) && !avoidTiles.contains(p))
			{
				candidates.add(p);
			}
		}
		candidates.sort((a, b) -> Integer.compare(dist(origin, b), dist(origin, a)));
		if (candidates.size() > count)
		{
			return new ArrayList<>(candidates.subList(0, count));
		}
		return candidates;
	}

	/*-------------------------------------------------------------------------*/
	private static int dist(Point a, Point b)
	{
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeLoot(Zone zone, Point tile, String lootTable, String onceVar)
	{
		Loot loot = new Loot(lootTable);
		loot.setExecuteOnceMazeVariable(onceVar);
		zone.getTile(tile).getScripts().add(loot);
	}
}

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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.map.script.ExecuteMazeScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.Loot;

/**
 * Post-processes a Noise4j-generated temple floor: stairs back to the hub and
 * depth-keyed loot on room-door tiles. Temple-only; does not modify the engine
 * generator.
 */
public final class TempleFloorDressing
{
	public static final String ASCEND_SCRIPT = "temple.ascend.1";
	public static final String LOOT_TABLE_PREFIX = "temple.depth.";
	public static final String LOOT_TABLE_SUFFIX = ".loot";
	public static final int LOOT_PLACEMENTS = 2;

	private TempleFloorDressing()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel)
	{
		Point origin = zone.getPlayerOrigin();
		List<Point> encounterTiles = findEncounterTiles(zone);

		Point stairs = pickStairsTile(zone, origin, encounterTiles);
		placeStairsUp(zone, stairs);

		List<Point> lootTiles = pickLootTiles(origin, encounterTiles, stairs, LOOT_PLACEMENTS);
		String lootTable = lootTableName(dungeonLevel);
		int i = 0;
		for (Point p : lootTiles)
		{
			placeLoot(zone, p, lootTable, zone.getName() + ".loot." + (i++));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String encounterTableName(int dungeonLevel)
	{
		return "temple.depth." + Math.max(1, dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	public static String lootTableName(int dungeonLevel)
	{
		return LOOT_TABLE_PREFIX + Math.max(1, dungeonLevel) + LOOT_TABLE_SUFFIX;
	}

	/*-------------------------------------------------------------------------*/
	public static List<Point> findEncounterTiles(Zone zone)
	{
		List<Point> result = new ArrayList<>();
		Tile[][] tiles = zone.getTiles();
		for (int y = 0; y < tiles.length; y++)
		{
			for (int x = 0; x < tiles[y].length; x++)
			{
				Tile tile = tiles[y][x];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof Encounter)
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
	public static Point findStairsTile(Zone zone)
	{
		Tile[][] tiles = zone.getTiles();
		for (int y = 0; y < tiles.length; y++)
		{
			for (int x = 0; x < tiles[y].length; x++)
			{
				Tile tile = tiles[y][x];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (isAscendScript(script))
					{
						return new Point(x, y);
					}
				}
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isAscendScript(TileScript script)
	{
		if (!(script instanceof ExecuteMazeScript))
		{
			return false;
		}
		MazeScript ms = ((ExecuteMazeScript)script).getScript();
		if (ms == null || ms.getEvents() == null)
		{
			return false;
		}
		for (MazeEvent event : ms.getEvents())
		{
			if (event instanceof ZoneChangeEvent)
			{
				ZoneChangeEvent zce = (ZoneChangeEvent)event;
				if ("Temple Hub".equals(zce.getZone()))
				{
					return true;
				}
			}
		}
		return ASCEND_SCRIPT.equals(ms.getName());
	}

	/*-------------------------------------------------------------------------*/
	private static Point pickStairsTile(Zone zone, Point origin, List<Point> encounterTiles)
	{
		// Prefer an open adjacent floor tile in the spawn room (reachable, no fight).
		Point openAdjacent = firstOpenAdjacent(zone, origin);
		if (openAdjacent != null && !openAdjacent.equals(origin))
		{
			return openAdjacent;
		}

		// Otherwise the closest room-door encounter tile (walkable from spawn).
		if (!encounterTiles.isEmpty())
		{
			return closest(origin, encounterTiles);
		}

		return new Point(origin);
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> pickLootTiles(
		Point origin,
		List<Point> encounterTiles,
		Point stairs,
		int count)
	{
		List<Point> candidates = new ArrayList<>();
		for (Point p : encounterTiles)
		{
			if (!p.equals(origin) && !p.equals(stairs))
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
	private static Point firstOpenAdjacent(Zone zone, Point origin)
	{
		Map map = zone.getMap();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		int width = map.getWidth();

		int[][] deltas = {
			{0, -1, CrusaderEngine.Facing.NORTH},
			{0, 1, CrusaderEngine.Facing.SOUTH},
			{-1, 0, CrusaderEngine.Facing.WEST},
			{1, 0, CrusaderEngine.Facing.EAST}};

		for (int[] d : deltas)
		{
			int x = origin.x + d[0];
			int y = origin.y + d[1];
			if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getLength())
			{
				continue;
			}
			Wall wall = wallBetween(origin, d[2], width, horiz, vert);
			if (wall != null && wall.isSolid())
			{
				continue;
			}
			Tile tile = zone.getTile(new Point(x, y));
			if (tile != null)
			{
				return new Point(x, y);
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static Wall wallBetween(Point cur, int facing, int width, Wall[] horiz, Wall[] vert)
	{
		return switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> horiz[cur.x + cur.y * width];
			case CrusaderEngine.Facing.SOUTH -> horiz[cur.x + (cur.y + 1) * width];
			case CrusaderEngine.Facing.WEST -> vert[cur.x + cur.y * (width + 1)];
			case CrusaderEngine.Facing.EAST -> vert[cur.x + cur.y * (width + 1) + 1];
			default -> null;
		};
	}

	/*-------------------------------------------------------------------------*/
	private static Point farthest(Point origin, List<Point> points)
	{
		Point best = points.get(0);
		int bestDist = dist(origin, best);
		for (Point p : points)
		{
			int d = dist(origin, p);
			if (d > bestDist)
			{
				bestDist = d;
				best = p;
			}
		}
		return best;
	}

	/*-------------------------------------------------------------------------*/
	private static Point closest(Point origin, List<Point> points)
	{
		Point best = points.get(0);
		int bestDist = dist(origin, best);
		for (Point p : points)
		{
			int d = dist(origin, p);
			if (d < bestDist)
			{
				bestDist = d;
				best = p;
			}
		}
		return best;
	}

	/*-------------------------------------------------------------------------*/
	private static int dist(Point a, Point b)
	{
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeStairsUp(Zone zone, Point tile)
	{
		MazeScript script = Database.getInstance().getMazeScripts().get(ASCEND_SCRIPT);
		if (script == null)
		{
			script = buildAscendScript();
		}
		ExecuteMazeScript exec = new ExecuteMazeScript(script);
		zone.getTile(tile).getScripts().add(0, exec);
	}

	/*-------------------------------------------------------------------------*/
	private static MazeScript buildAscendScript()
	{
		List<MazeEvent> events = new ArrayList<>();
		events.add(new FlavourTextEvent(
			"Stone stairs lead back up toward the temple entrance.",
			-1,
			true,
			FlavourTextEvent.Alignment.CENTER));
		events.add(new ZoneChangeEvent(
			"Temple Hub",
			new Point(8, 8),
			CrusaderEngine.Facing.SOUTH));
		return new MazeScript(ASCEND_SCRIPT, events);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeLoot(Zone zone, Point tile, String lootTable, String onceVar)
	{
		Loot loot = new Loot(lootTable);
		loot.setExecuteOnceMazeVariable(onceVar);
		zone.getTile(tile).getScripts().add(loot);
	}
}

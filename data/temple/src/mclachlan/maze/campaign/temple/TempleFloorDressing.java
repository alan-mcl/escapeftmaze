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
 * Post-processes a Noise4j-generated temple floor: stairs (up/down), depth-keyed
 * loot. Mutation keys are per-depth so regen restores cleared/looted state.
 */
public final class TempleFloorDressing
{
	public static final String ASCEND_SCRIPT = "temple.ascend";
	public static final String DESCEND_SCRIPT = "temple.descend";

	private TempleFloorDressing()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel)
	{
		Point origin = zone.getPlayerOrigin();
		List<Point> encounterTiles = findEncounterTiles(zone);

		Point stairsUp = pickStairsUpTile(zone, origin, encounterTiles);
		placeScript(zone, stairsUp, ASCEND_SCRIPT, buildAscendFallback());

		Point stairsDown = pickStairsDownTile(origin, encounterTiles, stairsUp);
		if (stairsDown != null)
		{
			placeScript(zone, stairsDown, DESCEND_SCRIPT, buildDescendFallback());
		}

		int lootCount = TempleDepthScaler.lootPlacements(dungeonLevel);
		List<Point> lootTiles = pickLootTiles(
			origin, encounterTiles, stairsUp, stairsDown, lootCount);
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
	public static Point findStairsUpTile(Zone zone)
	{
		return findScriptTile(zone, true, false);
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsDownTile(Zone zone)
	{
		return findScriptTile(zone, false, true);
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsTile(Zone zone)
	{
		return findStairsUpTile(zone);
	}

	/*-------------------------------------------------------------------------*/
	private static Point findScriptTile(Zone zone, boolean ascend, boolean descend)
	{
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
					if (ascend && isAscendScript(script))
					{
						return new Point(x, y);
					}
					if (descend && isDescendScript(script))
					{
						return new Point(x, y);
					}
				}
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean scriptMatches(
		TileScript script,
		String primaryName,
		String legacyName,
		Class<? extends MazeEvent> depthEventType,
		String hubZoneName)
	{
		if (!(script instanceof ExecuteMazeScript))
		{
			return false;
		}
		MazeScript ms = ((ExecuteMazeScript)script).getScript();
		if (ms == null)
		{
			return false;
		}
		if (primaryName.equals(ms.getName()) || legacyName.equals(ms.getName()))
		{
			return true;
		}
		if (ms.getEvents() == null)
		{
			return false;
		}
		for (MazeEvent event : ms.getEvents())
		{
			if (depthEventType.isInstance(event))
			{
				return true;
			}
			if (hubZoneName != null && event instanceof ZoneChangeEvent)
			{
				if (hubZoneName.equals(((ZoneChangeEvent)event).getZone()))
				{
					return true;
				}
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isAscendScript(TileScript script)
	{
		return scriptMatches(
			script, ASCEND_SCRIPT, "temple.ascend.1", TempleAscendEvent.class, "Temple Hub");
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isDescendScript(TileScript script)
	{
		return scriptMatches(
			script, DESCEND_SCRIPT, "temple.descend.1", TempleDescendEvent.class, null);
	}

	/*-------------------------------------------------------------------------*/
	private static Point pickStairsUpTile(Zone zone, Point origin, List<Point> encounterTiles)
	{
		Point openAdjacent = firstOpenAdjacent(zone, origin);
		if (openAdjacent != null && !openAdjacent.equals(origin))
		{
			return openAdjacent;
		}

		if (!encounterTiles.isEmpty())
		{
			return closest(origin, encounterTiles);
		}

		return new Point(origin);
	}

	/*-------------------------------------------------------------------------*/
	private static Point pickStairsDownTile(Point origin, List<Point> encounterTiles, Point stairsUp)
	{
		List<Point> candidates = new ArrayList<>();
		for (Point p : encounterTiles)
		{
			if (!p.equals(origin) && !p.equals(stairsUp))
			{
				candidates.add(p);
			}
		}
		if (candidates.isEmpty())
		{
			return null;
		}
		return farthest(origin, candidates);
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> pickLootTiles(
		Point origin,
		List<Point> encounterTiles,
		Point stairsUp,
		Point stairsDown,
		int count)
	{
		List<Point> candidates = new ArrayList<>();
		for (Point p : encounterTiles)
		{
			if (!p.equals(origin) && !p.equals(stairsUp) && !p.equals(stairsDown))
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
	private static void placeScript(Zone zone, Point tile, String scriptName, MazeScript fallback)
	{
		MazeScript script = Database.getInstance().getMazeScripts().get(scriptName);
		if (script == null)
		{
			script = fallback;
		}
		zone.getTile(tile).getScripts().add(0, new ExecuteMazeScript(script));
	}

	/*-------------------------------------------------------------------------*/
	private static MazeScript buildAscendFallback()
	{
		List<MazeEvent> events = new ArrayList<>();
		events.add(new FlavourTextEvent(
			"Stone stairs lead upward.",
			-1,
			true,
			FlavourTextEvent.Alignment.CENTER));
		events.add(new TempleAscendEvent());
		return new MazeScript(ASCEND_SCRIPT, events);
	}

	/*-------------------------------------------------------------------------*/
	private static MazeScript buildDescendFallback()
	{
		List<MazeEvent> events = new ArrayList<>();
		events.add(new FlavourTextEvent(
			"Stone stairs spiral deeper.",
			-1,
			true,
			FlavourTextEvent.Alignment.CENTER));
		events.add(new TempleDescendEvent());
		return new MazeScript(DESCEND_SCRIPT, events);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeLoot(Zone zone, Point tile, String lootTable, String onceVar)
	{
		Loot loot = new Loot(lootTable);
		loot.setExecuteOnceMazeVariable(onceVar);
		zone.getTile(tile).getScripts().add(loot);
	}
}

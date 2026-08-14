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
import java.awt.Rectangle;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;

/**
 * Stamps authored fragment zones onto a generated temple floor and ensures
 * connectivity back to the player origin.
 */
public final class TempleFragmentAssembler
{
	public static final int FLOOR_WIDE_CAP = 2;
	private static final int MARGIN = 1;

	public record Placement(String zoneName, Rectangle bounds)
	{
	}

	public record Result(List<Placement> placements, Set<Point> stampedTiles)
	{
	}

	/** Test seam: result of the most recent {@link #assemble} call. */
	static Result lastResult;

	private TempleFragmentAssembler()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static Result assemble(Zone floor, int depth, int floorSeed)
	{
		List<TempleFragmentCatalog.Entry> picks =
			TempleFragmentCatalog.pickForFloor(depth, floorSeed, FLOOR_WIDE_CAP);
		if (picks.isEmpty())
		{
			lastResult = new Result(List.of(), Set.of());
			return lastResult;
		}

		List<Placement> placements = new ArrayList<>();
		Set<Point> stampedTiles = new HashSet<>();
		Set<Rectangle> occupied = new HashSet<>();
		Random rng = new Random(floorSeed ^ 0x5354414dL);
		Point origin = floor.getPlayerOrigin();

		for (TempleFragmentCatalog.Entry entry : picks)
		{
			Zone fragment = Database.getInstance().getZone(entry.zoneName());
			Point anchor = pickAnchor(floor, fragment, origin, occupied, rng);
			if (anchor == null)
			{
				continue;
			}

			TempleFragmentStamp.stamp(floor, fragment, anchor.x, anchor.y);
			Rectangle bounds = TempleFragmentStamp.bounds(anchor.x, anchor.y, fragment);
			occupied.add(bounds);
			placements.add(new Placement(entry.zoneName(), bounds));
			addTiles(stampedTiles, bounds);

			remapEncounterVars(floor, bounds, depth, entry.zoneName());
			ensureConnectivity(floor, origin, bounds);
		}

		lastResult = new Result(placements, stampedTiles);
		return lastResult;
	}

	/*-------------------------------------------------------------------------*/
	private static Point pickAnchor(
		Zone floor,
		Zone fragment,
		Point origin,
		Set<Rectangle> occupied,
		Random rng)
	{
		int fw = fragment.getWidth();
		int fl = fragment.getLength();
		int maxX = floor.getWidth() - fw - MARGIN;
		int maxY = floor.getLength() - fl - MARGIN;
		if (maxX < MARGIN || maxY < MARGIN)
		{
			return null;
		}

		List<Point> candidates = new ArrayList<>();
		List<Integer> scores = new ArrayList<>();
		for (int x = MARGIN; x <= maxX; x++)
		{
			for (int y = MARGIN; y <= maxY; y++)
			{
				Rectangle rect = new Rectangle(x, y, fw, fl);
				if (overlapsAny(rect, occupied))
				{
					continue;
				}
				candidates.add(new Point(x, y));
				scores.add(scorePlacement(floor, rect, origin));
			}
		}

		if (candidates.isEmpty())
		{
			return null;
		}

		int bestScore = Collections.max(scores);
		List<Point> best = new ArrayList<>();
		for (int i = 0; i < candidates.size(); i++)
		{
			if (scores.get(i) == bestScore)
			{
				best.add(candidates.get(i));
			}
		}
		return best.get(rng.nextInt(best.size()));
	}

	/*-------------------------------------------------------------------------*/
	private static int scorePlacement(Zone floor, Rectangle rect, Point origin)
	{
		int openOverlap = 0;
		for (int x = rect.x; x < rect.x + rect.width; x++)
		{
			for (int y = rect.y; y < rect.y + rect.height; y++)
			{
				if (isOpenCell(floor, x, y))
				{
					openOverlap++;
				}
			}
		}
		int cx = rect.x + rect.width / 2;
		int cy = rect.y + rect.height / 2;
		int dist = Math.abs(cx - origin.x) + Math.abs(cy - origin.y);
		return openOverlap * 10 + dist;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean overlapsAny(Rectangle rect, Set<Rectangle> occupied)
	{
		for (Rectangle other : occupied)
		{
			if (rect.intersects(other))
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static void addTiles(Set<Point> stampedTiles, Rectangle bounds)
	{
		for (int x = bounds.x; x < bounds.x + bounds.width; x++)
		{
			for (int y = bounds.y; y < bounds.y + bounds.height; y++)
			{
				stampedTiles.add(new Point(x, y));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void remapEncounterVars(
		Zone floor,
		Rectangle bounds,
		int depth,
		String zoneName)
	{
		int index = 0;
		for (int x = bounds.x; x < bounds.x + bounds.width; x++)
		{
			for (int y = bounds.y; y < bounds.y + bounds.height; y++)
			{
				Tile tile = floor.getTiles()[x][y];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof Encounter enc)
					{
						enc.setMazeVariable(TempleSeeds.fragmentVar(depth, zoneName, index++));
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void ensureConnectivity(Zone floor, Point origin, Rectangle bounds)
	{
		if (canReachInterior(floor, origin, bounds))
		{
			return;
		}

		Point interior = interiorCell(bounds);
		Set<Point> reachable = collectReachable(floor, origin);
		Point bestOutside = null;
		int bestDist = Integer.MAX_VALUE;
		for (Point p : reachable)
		{
			if (bounds.contains(p))
			{
				continue;
			}
			int d = Math.abs(p.x - interior.x) + Math.abs(p.y - interior.y);
			if (d < bestDist)
			{
				bestDist = d;
				bestOutside = p;
			}
		}

		if (bestOutside != null)
		{
			carvePath(floor, bestOutside, interior);
		}
		else
		{
			openWallBetween(floor, interior, new Point(interior.x, Math.min(interior.y + 1, floor.getLength() - 1)));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> collectReachable(Zone floor, Point start)
	{
		Map map = floor.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		boolean[][] seen = new boolean[length][width];
		Set<Point> result = new HashSet<>();
		ArrayDeque<Point> q = new ArrayDeque<>();
		q.add(start);
		seen[start.y][start.x] = true;
		result.add(start);

		while (!q.isEmpty())
		{
			Point cur = q.removeFirst();
			visitStep(cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, null, q);
		}

		for (int y = 0; y < length; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (seen[y][x])
				{
					result.add(new Point(x, y));
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean canReachInterior(Zone floor, Point origin, Rectangle bounds)
	{
		Map map = floor.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> q = new ArrayDeque<>();
		q.add(origin);
		seen[origin.y][origin.x] = true;

		while (!q.isEmpty())
		{
			Point cur = q.removeFirst();
			if (bounds.contains(cur))
			{
				return true;
			}

			visitStep(cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, null, q);
			visitStep(cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, null, q);
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static Point interiorCell(Rectangle bounds)
	{
		return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
	}

	/*-------------------------------------------------------------------------*/
	private static void carvePath(Zone floor, Point start, Point goal)
	{
		Point cur = new Point(start);
		while (!cur.equals(goal))
		{
			int dx = Integer.compare(goal.x, cur.x);
			int dy = Integer.compare(goal.y, cur.y);
			Point next;
			if (Math.abs(goal.x - cur.x) >= Math.abs(goal.y - cur.y))
			{
				next = new Point(cur.x + dx, cur.y);
			}
			else
			{
				next = new Point(cur.x, cur.y + dy);
			}
			openWallBetween(floor, cur, next);
			cur = next;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void openWallBetween(Zone floor, Point a, Point b)
	{
		Map map = floor.getMap();
		int width = map.getWidth();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		if (a.x == b.x)
		{
			int minY = Math.min(a.y, b.y);
			int maxY = Math.max(a.y, b.y);
			for (int y = minY; y < maxY; y++)
			{
				int idx = map.getSouthWall(y * width + a.x);
				makePassable(horiz[idx]);
			}
		}
		else if (a.y == b.y)
		{
			int minX = Math.min(a.x, b.x);
			int maxX = Math.max(a.x, b.x);
			for (int x = minX; x < maxX; x++)
			{
				int idx = map.getEastWall(a.y * width + x);
				makePassable(vert[idx]);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void makePassable(Wall wall)
	{
		if (wall == null)
		{
			return;
		}
		wall.setSolid(false);
		wall.setVisible(false);
	}

	/*-------------------------------------------------------------------------*/
	private static void visitStep(
		Point cur,
		int facing,
		int width,
		int length,
		Wall[] horiz,
		Wall[] vert,
		boolean[][] seen,
		java.util.Map<Point, Point> prev,
		ArrayDeque<Point> q)
	{
		int nx = cur.x;
		int ny = cur.y;
		Wall wall;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH ->
			{
				wall = horiz[cur.x + cur.y * width];
				ny = cur.y - 1;
			}
			case CrusaderEngine.Facing.SOUTH ->
			{
				wall = horiz[cur.x + (cur.y + 1) * width];
				ny = cur.y + 1;
			}
			case CrusaderEngine.Facing.WEST ->
			{
				wall = vert[cur.x + cur.y * (width + 1)];
				nx = cur.x - 1;
			}
			case CrusaderEngine.Facing.EAST ->
			{
				wall = vert[cur.x + cur.y * (width + 1) + 1];
				nx = cur.x + 1;
			}
			default -> throw new IllegalStateException("facing " + facing);
		}

		if (nx < 0 || ny < 0 || nx >= width || ny >= length)
		{
			return;
		}
		if (wall != null && wall.isSolid())
		{
			return;
		}
		if (!seen[ny][nx])
		{
			seen[ny][nx] = true;
			if (prev != null)
			{
				prev.put(new Point(nx, ny), cur);
			}
			q.add(new Point(nx, ny));
		}
	}

	/*-------------------------------------------------------------------------*/
	static boolean isOpenCell(Zone zone, int x, int y)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		if (x > 0)
		{
			Wall w = vert[x + y * (width + 1)];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (x < width - 1)
		{
			Wall w = vert[x + y * (width + 1) + 1];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (y > 0)
		{
			Wall w = horiz[x + y * width];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (y < length - 1)
		{
			Wall w = horiz[x + (y + 1) * width];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		return false;
	}
}

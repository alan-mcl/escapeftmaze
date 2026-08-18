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

package mclachlan.dungeongen.fragment;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Walkability checks for assembled fragment layouts.
 */
public final class FragmentConnectivity
{
	private FragmentConnectivity()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isOpenCell(Zone zone, int x, int y)
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

	/*-------------------------------------------------------------------------*/
	/**
	 * Floor tiles reachable within a fragment prefab (excludes empty AABB
	 * corners that {@link #isOpenCell} would count on a merged shell).
	 */
	public static Set<Point> walkableFloorCells(Zone fragment)
	{
		Map map = fragment.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> queue = new ArrayDeque<>();

		for (FragmentSockets.Socket socket : FragmentSockets.detect(fragment))
		{
			seedWalkable(fragment, width, length, seen, queue, socket.localX(), socket.localY());
			int[] outward = FragmentSockets.delta(socket.facing());
			seedWalkable(
				fragment,
				width,
				length,
				seen,
				queue,
				socket.localX() - outward[0],
				socket.localY() - outward[1]);
		}

		while (!queue.isEmpty())
		{
			Point cur = queue.removeFirst();
			walkEnqueue(
				fragment, width, length, horiz, vert, seen, queue,
				cur, CrusaderEngine.Facing.NORTH);
			walkEnqueue(
				fragment, width, length, horiz, vert, seen, queue,
				cur, CrusaderEngine.Facing.SOUTH);
			walkEnqueue(
				fragment, width, length, horiz, vert, seen, queue,
				cur, CrusaderEngine.Facing.WEST);
			walkEnqueue(
				fragment, width, length, horiz, vert, seen, queue,
				cur, CrusaderEngine.Facing.EAST);
		}

		Set<Point> cells = new HashSet<>();
		for (int y = 0; y < length; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (seen[y][x])
				{
					cells.add(new Point(x, y));
				}
			}
		}
		return cells;
	}

	/*-------------------------------------------------------------------------*/
	private static void seedWalkable(
		Zone fragment,
		int width,
		int length,
		boolean[][] seen,
		ArrayDeque<Point> queue,
		int x,
		int y)
	{
		if (x < 0 || y < 0 || x >= width || y >= length)
		{
			return;
		}
		if (!isOpenCell(fragment, x, y) || seen[y][x])
		{
			return;
		}
		seen[y][x] = true;
		queue.add(new Point(x, y));
	}

	/*-------------------------------------------------------------------------*/
	private static void walkEnqueue(
		Zone fragment,
		int width,
		int length,
		Wall[] horiz,
		Wall[] vert,
		boolean[][] seen,
		ArrayDeque<Point> queue,
		Point cur,
		int facing)
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
		if (!isOpenCell(fragment, nx, ny) || seen[ny][nx])
		{
			return;
		}
		seen[ny][nx] = true;
		queue.add(new Point(nx, ny));
	}
}

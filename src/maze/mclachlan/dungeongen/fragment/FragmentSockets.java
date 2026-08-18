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

import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Infers connection sockets from fragment perimeter walls (1-tile non-solid
 * openings on walkable cells). Rotated variants are supplied in-memory by
 * {@link FragmentRotate}.
 */
public final class FragmentSockets
{
	public record Socket(int localX, int localY, int facing)
	{
		public int oppositeFacing()
		{
			return switch (facing)
			{
				case CrusaderEngine.Facing.NORTH -> CrusaderEngine.Facing.SOUTH;
				case CrusaderEngine.Facing.SOUTH -> CrusaderEngine.Facing.NORTH;
				case CrusaderEngine.Facing.EAST -> CrusaderEngine.Facing.WEST;
				case CrusaderEngine.Facing.WEST -> CrusaderEngine.Facing.EAST;
				default -> throw new IllegalStateException("facing " + facing);
			};
		}
	}

	private FragmentSockets()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static List<Socket> detect(Zone fragment)
	{
		Map map = fragment.getMap();
		int w = fragment.getWidth();
		int h = fragment.getLength();
		int mapW = map.getWidth();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		List<Socket> result = new ArrayList<>();

		collapseEdge(
			result,
			scanNorth(horiz, mapW, w, h, fragment),
			CrusaderEngine.Facing.NORTH);
		collapseEdge(
			result,
			scanSouth(horiz, map, w, h, fragment),
			CrusaderEngine.Facing.SOUTH);
		collapseEdge(
			result,
			scanWest(vert, map, w, h, fragment),
			CrusaderEngine.Facing.WEST);
		collapseEdge(
			result,
			scanEast(vert, map, w, h, fragment),
			CrusaderEngine.Facing.EAST);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<int[]> scanNorth(
		Wall[] horiz,
		int mapW,
		int w,
		int h,
		Zone fragment)
	{
		List<int[]> coords = new ArrayList<>();
		for (int x = 0; x < w; x++)
		{
			if (isSocketCell(horiz[x], fragment, x, 0))
			{
				coords.add(new int[]{x, 0});
			}
		}
		return coords;
	}

	/*-------------------------------------------------------------------------*/
	private static List<int[]> scanSouth(
		Wall[] horiz,
		Map map,
		int w,
		int h,
		Zone fragment)
	{
		List<int[]> coords = new ArrayList<>();
		for (int x = 0; x < w; x++)
		{
			int idx = map.getSouthWall((h - 1) * map.getWidth() + x);
			if (isSocketCell(horiz[idx], fragment, x, h - 1))
			{
				coords.add(new int[]{x, h - 1});
			}
		}
		return coords;
	}

	/*-------------------------------------------------------------------------*/
	private static List<int[]> scanWest(
		Wall[] vert,
		Map map,
		int w,
		int h,
		Zone fragment)
	{
		List<int[]> coords = new ArrayList<>();
		for (int y = 0; y < h; y++)
		{
			int idx = map.getWestWall(y * map.getWidth());
			if (isSocketCell(vert[idx], fragment, 0, y))
			{
				coords.add(new int[]{0, y});
			}
		}
		return coords;
	}

	/*-------------------------------------------------------------------------*/
	private static List<int[]> scanEast(
		Wall[] vert,
		Map map,
		int w,
		int h,
		Zone fragment)
	{
		List<int[]> coords = new ArrayList<>();
		for (int y = 0; y < h; y++)
		{
			int idx = map.getEastWall(y * map.getWidth() + (w - 1));
			if (isSocketCell(vert[idx], fragment, w - 1, y))
			{
				coords.add(new int[]{w - 1, y});
			}
		}
		return coords;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isSocketCell(Wall wall, Zone fragment, int x, int y)
	{
		return isPassable(wall)
			&& FragmentConnectivity.isOpenCell(fragment, x, y);
	}

	/*-------------------------------------------------------------------------*/
	private static void collapseEdge(
		List<Socket> result,
		List<int[]> coords,
		int facing)
	{
		if (coords.isEmpty())
		{
			return;
		}

		boolean horizontal = facing == CrusaderEngine.Facing.NORTH
			|| facing == CrusaderEngine.Facing.SOUTH;
		coords.sort((a, b) -> horizontal
			? Integer.compare(a[0], b[0])
			: Integer.compare(a[1], b[1]));

		int runStart = 0;
		for (int i = 1; i <= coords.size(); i++)
		{
			boolean breaksRun = i == coords.size()
				|| (horizontal
					? coords.get(i)[0] != coords.get(i - 1)[0] + 1
					: coords.get(i)[1] != coords.get(i - 1)[1] + 1);
			if (!breaksRun)
			{
				continue;
			}

			int runEnd = i - 1;
			int mid = (runStart + runEnd) / 2;
			int[] pick = coords.get(mid);
			result.add(new Socket(pick[0], pick[1], facing));
			runStart = i;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static List<Socket> withFacing(List<Socket> sockets, int facing)
	{
		List<Socket> result = new ArrayList<>();
		for (Socket socket : sockets)
		{
			if (socket.facing() == facing)
			{
				result.add(socket);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	static int[] delta(int facing)
	{
		return switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> new int[]{0, -1};
			case CrusaderEngine.Facing.SOUTH -> new int[]{0, 1};
			case CrusaderEngine.Facing.EAST -> new int[]{1, 0};
			case CrusaderEngine.Facing.WEST -> new int[]{-1, 0};
			default -> throw new IllegalStateException("facing " + facing);
		};
	}

	/*-------------------------------------------------------------------------*/
	static boolean isPassable(Wall wall)
	{
		return wall != null && !wall.isSolid();
	}
}

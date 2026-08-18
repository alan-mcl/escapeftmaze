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
 * openings). Rotated variants are supplied in-memory by
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

		for (int x = 0; x < w; x++)
		{
			if (isPassable(horiz[x + 0 * mapW]))
			{
				result.add(new Socket(x, 0, CrusaderEngine.Facing.NORTH));
			}
		}

		for (int x = 0; x < w; x++)
		{
			int idx = map.getSouthWall((h - 1) * mapW + x);
			if (isPassable(horiz[idx]))
			{
				result.add(new Socket(x, h - 1, CrusaderEngine.Facing.SOUTH));
			}
		}

		for (int y = 0; y < h; y++)
		{
			int idx = map.getWestWall(y * mapW + 0);
			if (isPassable(vert[idx]))
			{
				result.add(new Socket(0, y, CrusaderEngine.Facing.WEST));
			}
		}

		for (int y = 0; y < h; y++)
		{
			int idx = map.getEastWall(y * mapW + (w - 1));
			if (isPassable(vert[idx]))
			{
				result.add(new Socket(w - 1, y, CrusaderEngine.Facing.EAST));
			}
		}

		return result;
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

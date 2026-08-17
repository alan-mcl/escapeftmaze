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
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.dungeongen.StairwellPlan;
import mclachlan.dungeongen.StairwellPlanner;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.map.Zone;

/**
 * Noise4j stair placement: blank room walls only (never corridor door junctions).
 * Up near entry in the starting room; down on a far blank wall elsewhere.
 */
public final class Noise4jStairwellPlanner implements StairwellPlanner
{
	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int WALL = Noise4jDungeonGen.WALL_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	private static final int[] DIRECTION_SCAN = {
		CrusaderEngine.Facing.NORTH,
		CrusaderEngine.Facing.EAST,
		CrusaderEngine.Facing.SOUTH,
		CrusaderEngine.Facing.WEST
	};

	private static final int[][] DIRECTION_DELTA = {
		{0, -1},
		{1, 0},
		{0, 1},
		{-1, 0}
	};

	@Override
	public StairwellPlan planStairwells(
		Zone zone,
		Grid grid,
		int depth,
		Point layoutOrigin,
		DungeonGenContext context)
	{
		StairPortalSpec up = context.getRestoredUp();
		StairPortalSpec down = context.getRestoredDown();

		Set<Point> startingRoom = floodFillStartingRoom(grid, layoutOrigin);
		List<Candidate> candidates = findBlankWallCandidates(grid);
		if (up == null)
		{
			up = pickUp(layoutOrigin, startingRoom, candidates);
		}
		if (down == null && !candidates.isEmpty() && TempleDepthScaler.hasDownStairs(depth))
		{
			down = pickDown(layoutOrigin, startingRoom, candidates, up);
		}

		Point spawn = layoutOrigin;
		Integer spawnFacing = null;
		switch (context.getEntryMode())
		{
			case FROM_HUB, FROM_ABOVE -> {
				if (up != null)
				{
					spawn = up.from();
					spawnFacing = up.spawnFacing();
				}
			}
			case FROM_BELOW -> {
				if (down != null)
				{
					spawn = down.from();
					spawnFacing = down.spawnFacing();
				}
			}
			default -> { }
		}

		return new StairwellPlan(up, down, spawn, spawnFacing);
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> floodFillStartingRoom(Grid grid, Point origin)
	{
		Set<Point> room = new HashSet<>();
		if (origin == null || cell(grid, origin.x, origin.y) != ROOM)
		{
			return room;
		}

		ArrayDeque<Point> queue = new ArrayDeque<>();
		queue.add(origin);
		room.add(origin);

		while (!queue.isEmpty())
		{
			Point p = queue.removeFirst();
			for (int[] delta : DIRECTION_DELTA)
			{
				int nx = p.x + delta[0];
				int ny = p.y + delta[1];
				Point next = new Point(nx, ny);
				if (room.contains(next) || cell(grid, nx, ny) != ROOM)
				{
					continue;
				}
				room.add(next);
				queue.addLast(next);
			}
		}
		return room;
	}

	/*-------------------------------------------------------------------------*/
	private static StairPortalSpec pickUp(
		Point origin,
		Set<Point> startingRoom,
		List<Candidate> candidates)
	{
		List<Candidate> pool = filterStartingRoom(startingRoom, candidates);
		if (pool.isEmpty())
		{
			pool = candidates;
		}
		if (pool.isEmpty())
		{
			return null;
		}

		Candidate best = pool.get(0);
		int bestDist = dist(origin, best.from);
		for (int i = 1; i < pool.size(); i++)
		{
			Candidate c = pool.get(i);
			int d = dist(origin, c.from);
			if (d < bestDist || (d == bestDist && c.facingRank() < best.facingRank()))
			{
				bestDist = d;
				best = c;
			}
		}
		return best.toSpec(StairPortalSpec.StairMask.STAIR_UP);
	}

	/*-------------------------------------------------------------------------*/
	private static StairPortalSpec pickDown(
		Point origin,
		Set<Point> startingRoom,
		List<Candidate> candidates,
		StairPortalSpec up)
	{
		Candidate best = null;
		int bestDist = -1;
		for (Candidate c : candidates)
		{
			if (startingRoom.contains(c.from))
			{
				continue;
			}
			if (up != null && c.from.equals(up.from()))
			{
				continue;
			}
			int d = dist(origin, c.from);
			if (d > bestDist || (d == bestDist && best != null && c.facingRank() < best.facingRank()))
			{
				bestDist = d;
				best = c;
			}
		}
		return best == null ? null : best.toSpec(StairPortalSpec.StairMask.STAIR_DOWN);
	}

	/*-------------------------------------------------------------------------*/
	private static List<Candidate> filterStartingRoom(Set<Point> startingRoom, List<Candidate> candidates)
	{
		if (startingRoom.isEmpty())
		{
			return List.of();
		}
		List<Candidate> result = new ArrayList<>();
		for (Candidate c : candidates)
		{
			if (startingRoom.contains(c.from))
			{
				result.add(c);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Candidate> findBlankWallCandidates(Grid grid)
	{
		int width = grid.getWidth();
		int height = grid.getHeight();
		List<Candidate> result = new ArrayList<>();

		for (int x = 1; x < width - 1; x++)
		{
			for (int y = 1; y < height - 1; y++)
			{
				if (cell(grid, x, y) != ROOM)
				{
					continue;
				}

				for (int i = 0; i < DIRECTION_SCAN.length; i++)
				{
					int dx = DIRECTION_DELTA[i][0];
					int dy = DIRECTION_DELTA[i][1];
					int neighbor = cell(grid, x + dx, y + dy);
					if (neighbor != WALL)
					{
						continue;
					}
					boolean horizontalWall = DIRECTION_SCAN[i] == CrusaderEngine.Facing.NORTH
						|| DIRECTION_SCAN[i] == CrusaderEngine.Facing.SOUTH;
					result.add(new Candidate(
						new Point(x, y),
						new Point(x + dx, y + dy),
						DIRECTION_SCAN[i],
						horizontalWall,
						i));
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static int cell(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	/*-------------------------------------------------------------------------*/
	private static int dist(Point a, Point b)
	{
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	/*-------------------------------------------------------------------------*/
	private record Candidate(
		Point from,
		Point to,
		int fromFacing,
		boolean horizontalWall,
		int facingRank)
	{
		StairPortalSpec toSpec(StairPortalSpec.StairMask mask)
		{
			return new StairPortalSpec(from, fromFacing, to, StairPortalSpec.oppositeFacing(fromFacing),
				horizontalWall, mask);
		}
	}
}

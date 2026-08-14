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
 * Noise4j stair placement: corridor–room wall portals; up near entry, down far.
 */
public final class Noise4jStairwellPlanner implements StairwellPlanner
{
	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

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

		List<Candidate> candidates = findCandidates(grid);
		if (up == null)
		{
			up = pickUp(layoutOrigin, candidates);
		}
		if (down == null && !candidates.isEmpty())
		{
			down = pickDown(layoutOrigin, candidates, up);
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
	private static StairPortalSpec pickUp(Point origin, List<Candidate> candidates)
	{
		if (candidates.isEmpty())
		{
			return null;
		}
		Candidate best = candidates.get(0);
		int bestDist = dist(origin, best.from);
		for (Candidate c : candidates)
		{
			int d = dist(origin, c.from);
			if (d < bestDist)
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
		List<Candidate> candidates,
		StairPortalSpec up)
	{
		Candidate best = null;
		int bestDist = -1;
		for (Candidate c : candidates)
		{
			if (up != null && c.from.equals(up.from()))
			{
				continue;
			}
			int d = dist(origin, c.from);
			if (d > bestDist)
			{
				bestDist = d;
			 best = c;
			}
		}
		return best == null ? null : best.toSpec(StairPortalSpec.StairMask.STAIR_DOWN);
	}

	/*-------------------------------------------------------------------------*/
	private static List<Candidate> findCandidates(Grid grid)
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

				tryAdd(grid, result, x, y, 0, -1, CrusaderEngine.Facing.NORTH, true);
				tryAdd(grid, result, x, y, 0, 1, CrusaderEngine.Facing.SOUTH, true);
				tryAdd(grid, result, x, y, -1, 0, CrusaderEngine.Facing.WEST, false);
				tryAdd(grid, result, x, y, 1, 0, CrusaderEngine.Facing.EAST, false);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static void tryAdd(
		Grid grid,
		List<Candidate> result,
		int x,
		int y,
		int dx,
		int dy,
		int fromFacing,
		boolean horizontalWall)
	{
		if (cell(grid, x + dx, y + dy) == CORRIDOR)
		{
			result.add(new Candidate(
				new Point(x, y),
				new Point(x + dx, y + dy),
				fromFacing,
				horizontalWall));
		}
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
	private record Candidate(Point from, Point to, int fromFacing, boolean horizontalWall)
	{
		StairPortalSpec toSpec(StairPortalSpec.StairMask mask)
		{
			return new StairPortalSpec(from, fromFacing, to, StairPortalSpec.oppositeFacing(fromFacing),
				horizontalWall, mask);
		}
	}
}

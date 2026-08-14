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
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4: fragment catalog from zone metadata; assembler helpers for future WFC.
 * Live floor layout uses {@link TempleLayoutPolicy} / Noise4j only — no stamp overlay.
 */
public class TempleFragmentPhase4Test extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	@Test
	void fragmentCatalogPeeksMetadataWithoutFullZoneLoad() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		java.util.Map<String, String> chapel =
			db.peekZoneMetadata("fragment.flavour.chapel");
		assertEquals("true", chapel.get("fragment"));
		assertEquals("flavour", chapel.get("fragment.role"));

		java.util.Map<String, java.util.Map<String, String>> byPrefix =
			db.peekZoneMetadataByPrefix("fragment.");
		assertTrue(byPrefix.size() >= 3);
	}

	@Test
	void depth1LiveGenerationIsReachableWithoutFragmentOverlay() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 1);

		Point origin = zone.getPlayerOrigin();
		Point stairs = TempleFloorDressing.findStairsTile(zone);
		assertNotNull(stairs);
		assertTrue(canReach(zone, origin, stairs));
	}

	@Test
	void isolatedAssemblerStampsFragmentWithReachability() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 1);
		int floorSeed = TempleSeeds.floorSeed(1);
		TempleFragmentAssembler.Result result =
			TempleFragmentAssembler.assemble(zone, 1, floorSeed);

		assertNotNull(result);
		assertFalse(result.placements().isEmpty(),
			"isolated assembler should stamp at least one fragment");

		Point origin = zone.getPlayerOrigin();
		for (TempleFragmentAssembler.Placement p : result.placements())
		{
			Point interior = new Point(
				p.bounds().x + p.bounds().width / 2,
				p.bounds().y + p.bounds().height / 2);
			assertTrue(canReach(zone, origin, interior),
				"spawn should reach fragment " + p.zoneName());
		}
	}

	@Test
	void depth1DoesNotPickQuestFragmentFromCatalog() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		List<TempleFragmentCatalog.Entry> depth1 =
			TempleFragmentCatalog.eligibleForDepth(1);
		assertTrue(depth1.stream().noneMatch(e -> "quest".equals(e.role())));
		assertTrue(depth1.stream().anyMatch(e -> "flavour".equals(e.role())));
	}

	@Test
	void depth2EligibleIncludesQuestFragment() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		List<TempleFragmentCatalog.Entry> depth2 =
			TempleFragmentCatalog.eligibleForDepth(2);
		assertTrue(depth2.stream().anyMatch(e -> "quest".equals(e.role())));
	}

	@Test
	void fragmentStampIsDeterministicForFixedSeed() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		TempleFragmentAssembler.Result a = assembleOnFreshDepth1(db);
		MazeVariables.clearAll();
		TempleCampaignHarness.bootMaze(db);
		TempleFragmentAssembler.Result b = assembleOnFreshDepth1(db);

		assertEquals(a.placements().size(), b.placements().size());
		for (int i = 0; i < a.placements().size(); i++)
		{
			assertEquals(a.placements().get(i).zoneName(), b.placements().get(i).zoneName());
			assertEquals(a.placements().get(i).bounds(), b.placements().get(i).bounds());
		}
	}

	/*-------------------------------------------------------------------------*/
	private static TempleFragmentAssembler.Result assembleOnFreshDepth1(Database db)
		throws Exception
	{
		Zone zone = generateDepth(db, 1);
		int floorSeed = TempleSeeds.floorSeed(1);
		return TempleFragmentAssembler.assemble(zone, 1, floorSeed);
	}

	private static Zone generateDepth(Database db, int depth) throws Exception
	{
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, Integer.toString(depth));

		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean canReach(Zone zone, Point start, Point goal)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> q = new ArrayDeque<>();
		q.add(start);
		seen[start.y][start.x] = true;

		java.util.Map<Point, List<Point>> portalLinks = new HashMap<>();
		if (zone.getPortals() != null)
		{
			for (Portal portal : zone.getPortals())
			{
				portalLinks.computeIfAbsent(portal.getFrom(), k -> new ArrayList<>()).add(portal.getTo());
				portalLinks.computeIfAbsent(portal.getTo(), k -> new ArrayList<>()).add(portal.getFrom());
			}
		}

		while (!q.isEmpty())
		{
			Point cur = q.removeFirst();
			if (cur.equals(goal))
			{
				return true;
			}

			tryStep(cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, q);

			List<Point> links = portalLinks.get(cur);
			if (links != null)
			{
				for (Point next : links)
				{
					if (next.x >= 0 && next.y >= 0 && next.x < width && next.y < length
						&& !seen[next.y][next.x])
					{
						seen[next.y][next.x] = true;
						q.add(next);
					}
				}
			}
		}
		return false;
	}

	private static void tryStep(
		Point cur, int facing, int width, int length,
		Wall[] horiz, Wall[] vert, boolean[][] seen, ArrayDeque<Point> q)
	{
		int nx = cur.x;
		int ny = cur.y;
		Wall wall;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> {
				wall = horiz[cur.x + cur.y * width];
				ny = cur.y - 1;
			}
			case CrusaderEngine.Facing.SOUTH -> {
				wall = horiz[cur.x + (cur.y + 1) * width];
				ny = cur.y + 1;
			}
			case CrusaderEngine.Facing.WEST -> {
				wall = vert[cur.x + cur.y * (width + 1)];
				nx = cur.x - 1;
			}
			case CrusaderEngine.Facing.EAST -> {
				wall = vert[cur.x + cur.y * (width + 1) + 1];
				nx = cur.x + 1;
			}
			default -> { return; }
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
			q.add(new Point(nx, ny));
		}
	}
}

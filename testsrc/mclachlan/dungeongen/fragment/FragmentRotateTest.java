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
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;
import mclachlan.dungeongen.ZoneShell;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * In-memory quarter-turn rotation for fragment assembly.
 */
public class FragmentRotateTest extends MazeTestSupport
{
	@AfterEach
	void clearCaches()
	{
		FragmentDungeonGen.clearTestCatalog();
	}

	@Test
	void bendRotatesSocketsAndWalkableCells() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone bend = FragmentPrefabFixtures.bendFixture(db);
		assertEquals(2, FragmentSockets.detect(bend).size());
		assertTrue(FragmentSockets.detect(bend).stream()
			.anyMatch(s -> s.localX() == 0 && s.localY() == 0 && s.facing() == CrusaderEngine.Facing.NORTH));
		assertTrue(FragmentSockets.detect(bend).stream()
			.anyMatch(s -> s.localX() == 1 && s.localY() == 1 && s.facing() == CrusaderEngine.Facing.EAST));

		Zone rotated = FragmentRotate.rotate(bend, 1, "bend#r90");
		assertEquals(2, rotated.getWidth());
		assertEquals(2, rotated.getLength());
		assertTrue(FragmentSockets.detect(rotated).stream()
			.anyMatch(s -> s.localX() == 1 && s.localY() == 0 && s.facing() == CrusaderEngine.Facing.EAST));

		Set<Point> walkable = walkableCells(bend);
		Set<Point> rotatedWalkable = walkableCells(rotated);
		Set<Point> expected = new HashSet<>();
		for (Point p : walkable)
		{
			expected.add(new Point(1 - p.y, p.x));
		}
		assertEquals(expected, rotatedWalkable);
	}

	@Test
	void straightCorridorSwapsDimensions() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone straight = FragmentPrefabFixtures.straightFixture(db);
		assertEquals(1, straight.getWidth());
		assertEquals(3, straight.getLength());

		Zone rotated = FragmentRotate.rotate(straight, 1, "straight#r90");
		assertEquals(3, rotated.getWidth());
		assertEquals(1, rotated.getLength());
		assertEquals(3, walkableCells(rotated).size());
	}

	@Test
	void bedTexturesCycleOnRotation() throws Exception
	{
		Texture north = new Texture("n", null, 0, null, 0, null);
		Texture east = new Texture("e", null, 0, null, 0, null);
		Texture south = new Texture("s", null, 0, null, 0, null);
		Texture west = new Texture("w", null, 0, null, 0, null);
		EngineObject obj = new EngineObject(
			"bed",
			0,
			0,
			north,
			south,
			east,
			west,
			0,
			false,
			null,
			EngineObject.Alignment.BOTTOM);

		FragmentRotate.rotateTexturesCW(obj);
		assertSame(west, obj.getNorthTexture());
		assertSame(north, obj.getEastTexture());
		assertSame(east, obj.getSouthTexture());
		assertSame(south, obj.getWestTexture());
	}

	@Test
	void catalogExpansionProducesFourFacingsForRotatableFragments() throws Exception
	{
		FragmentCatalog.Entry base = new FragmentCatalog.Entry(
			"test.bend",
			"flavour",
			"barracks",
			FragmentCatalog.Kind.CORRIDOR,
			false,
			1,
			99,
			5,
			2);

		List<FragmentCatalog.Entry> expanded = FragmentCatalog.expandRotations(List.of(base));
		assertEquals(4, expanded.size());
		assertEquals("test.bend", expanded.get(0).zoneName());
		assertEquals("test.bend#r90", expanded.get(1).zoneName());
		assertEquals(1, expanded.get(1).quarterTurns());
		assertEquals("test.bend", expanded.get(1).sourceZoneName());
	}

	@Test
	void assemblerUsesRotatedDormFacing() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		FragmentPrefabFixtures.TestKit kit = FragmentPrefabFixtures.barracksTestKit(db);
		FragmentDungeonGen.setTestCatalog(kit.catalog(), kit.zonesByName());

		Zone floor = db.getZone("temple.1");
		ZoneShell.ensureSize(floor, 15);
		FragmentDungeonGen.AssemblyResult result = FragmentDungeonGen.assembleForTest(
			floor,
			FragmentCatalog.expandRotations(kit.catalog()),
			new Random(7L),
			floor.getWidth(),
			floor.getLength(),
			FragmentDungeonGen.Options.of("barracks"));

		assertNotNull(result);
		long roomCount = result.placements().stream()
			.filter(p -> p.kind() == FragmentCatalog.Kind.ROOM)
			.count();
		assertTrue(roomCount >= 3, "rotation should supply alternate dorm facings");
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> walkableCells(Zone zone)
	{
		Set<Point> result = new HashSet<>();
		for (int y = 0; y < zone.getLength(); y++)
		{
			for (int x = 0; x < zone.getWidth(); x++)
			{
				if (FragmentConnectivity.isOpenCell(zone, x, y))
				{
					result.add(new Point(x, y));
				}
			}
		}
		return result;
	}
}

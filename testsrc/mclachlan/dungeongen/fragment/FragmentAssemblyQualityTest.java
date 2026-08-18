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

import java.awt.Rectangle;
import java.util.List;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.Noise4jStairwellPlanner;
import mclachlan.dungeongen.ZoneShell;
import mclachlan.maze.campaign.temple.TempleEnvironment;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live barracks kit assembly quality: sockets, fill, seed variance, welds.
 */
public class FragmentAssemblyQualityTest extends MazeTestSupport
{
	private static final FragmentDungeonGen.Options LARGE_FLOOR =
		new FragmentDungeonGen.Options("barracks", 3, 12, 32, 1);

	@AfterEach
	void clearCaches()
	{
		FragmentDungeonGen.clearTestCatalog();
	}

	@Test
	void crossDetectsOneSocketPerEdgeAtCentre() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		Zone cross = db.getZone("fragment.barracks.corr.cross");

		List<FragmentSockets.Socket> sockets = FragmentSockets.detect(cross);
		assertEquals(4, sockets.size());
		assertTrue(sockets.stream().anyMatch(
			s -> s.localX() == 1 && s.localY() == 0 && s.facing() == CrusaderEngine.Facing.NORTH));
		assertTrue(sockets.stream().anyMatch(
			s -> s.localX() == 1 && s.localY() == 2 && s.facing() == CrusaderEngine.Facing.SOUTH));
		assertTrue(sockets.stream().anyMatch(
			s -> s.localX() == 0 && s.localY() == 1 && s.facing() == CrusaderEngine.Facing.WEST));
		assertTrue(sockets.stream().anyMatch(
			s -> s.localX() == 2 && s.localY() == 1 && s.facing() == CrusaderEngine.Facing.EAST));
	}

	@Test
	void seedChangesStartPlacementOnLargeFloor() throws Exception
	{
		Rectangle first = startBoundsForSeed(101L);
		Rectangle second = startBoundsForSeed(202L);
		assertNotEquals(first, second, "different seeds should move the start room");
	}

	@Test
	void largeFloorFillsBeyondMinimumRooms() throws Exception
	{
		DungeonGenResult result = generateLiveFloor(42L, LARGE_FLOOR);
		assertTrue(result.rooms().size() > 3, "31×31 target 12 should exceed min 3 rooms");
		assertNotNull(FragmentDungeonGen.lastAssemblyResult);
		assertFalse(FragmentDungeonGen.lastAssemblyResult.welds().isEmpty());
	}

	@Test
	void portalCountMatchesSocketWelds() throws Exception
	{
		Zone zone = generateLiveZone(42L, LARGE_FLOOR);
		assertEquals(
			FragmentDungeonGen.lastAssemblyResult.welds().size(),
			zone.getPortals().length);
	}

	@Test
	void unusedPerimeterSocketsAreSealedAfterGenerate() throws Exception
	{
		Zone zone = generateLiveZone(42L, LARGE_FLOOR);
		assertFalse(hasPassableUnusedSocket(FragmentDungeonGen.lastAssemblyResult, zone));
	}

	/*-------------------------------------------------------------------------*/
	private static Rectangle startBoundsForSeed(long seed) throws Exception
	{
		generateLiveFloor(seed, LARGE_FLOOR);
		return FragmentDungeonGen.lastAssemblyResult.placements().get(0).bounds();
	}

	private static DungeonGenResult generateLiveFloor(long seed, FragmentDungeonGen.Options options)
		throws Exception
	{
		Zone zone = generateLiveZone(seed, options);
		return new FragmentDungeonGen(options).generate(
			zone,
			seed,
			1,
			new PreviewDecorator(zone),
			DungeonGenContext.builder()
				.stairwellPlanner(new Noise4jStairwellPlanner())
				.build());
	}

	private static Zone generateLiveZone(long seed, FragmentDungeonGen.Options options)
		throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		Zone zone = db.getZone("temple.1");
		ZoneShell.ensureSize(zone, 31);
		TempleEnvironment.forFloor(1).applyToCrusaderTiles(zone.getMap());
		new FragmentDungeonGen(options).generate(
			zone,
			seed,
			1,
			new PreviewDecorator(zone),
			DungeonGenContext.builder()
				.stairwellPlanner(new Noise4jStairwellPlanner())
				.build());
		return zone;
	}

	private static boolean hasPassableUnusedSocket(
		FragmentDungeonGen.AssemblyResult assembly,
		Zone zone)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		for (FragmentDungeonGen.Placement placement : assembly.placements())
		{
			Zone fragment = Database.getInstance().getZone(placement.entry().sourceZoneName());
			for (FragmentSockets.Socket socket : FragmentSockets.detect(fragment))
			{
				FragmentDungeonGen.SocketRef ref = new FragmentDungeonGen.SocketRef(
					placement,
					socket,
					placement.bounds().x + socket.localX(),
					placement.bounds().y + socket.localY());
				if (assembly.connectedSockets().contains(ref))
				{
					continue;
				}

				int x = ref.worldX();
				int y = ref.worldY();
				int tileIndex = y * width + x;
				Wall wall = switch (socket.facing())
				{
					case CrusaderEngine.Facing.NORTH -> horiz[map.getNorthWall(tileIndex)];
					case CrusaderEngine.Facing.SOUTH -> horiz[map.getSouthWall(tileIndex)];
					case CrusaderEngine.Facing.WEST -> vert[map.getWestWall(tileIndex)];
					case CrusaderEngine.Facing.EAST -> vert[map.getEastWall(tileIndex)];
					default -> throw new IllegalStateException("facing " + socket.facing());
				};
				if (FragmentSockets.isPassable(wall))
				{
					return true;
				}
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static final class PreviewDecorator implements MapGenZoneScript.DungeonDecorator
	{
		private final mclachlan.crusader.Texture wallTexture;

		PreviewDecorator(Zone shell)
		{
			mclachlan.crusader.Wall sample = sampleSolidWall(shell.getMap());
			wallTexture = sample == null ? Map.NO_WALL : sample.getTexture(0);
		}

		@Override
		public mclachlan.crusader.Wall getRoomWall(
			mclachlan.dungeongen.noise4j.map.Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public mclachlan.crusader.Wall getCorridorWall(
			mclachlan.dungeongen.noise4j.map.Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public java.util.List<Object> handlePortal(
			mclachlan.dungeongen.noise4j.map.Grid grid,
			java.awt.Point from,
			int fromFacing,
			java.awt.Point to,
			int toFacing)
		{
			return java.util.List.of(
				wall(null),
				new mclachlan.maze.map.Portal(
					null,
					mclachlan.maze.map.Portal.State.UNLOCKED,
					from,
					fromFacing,
					to,
					toFacing,
					true,
					true,
					true,
					true,
					1,
					1,
					new int[]{0, 0, 0, 0, 0, 0, 0, 0},
					new java.util.BitSet(),
					null,
					false,
					"generic door creak",
					null));
		}

		@Override
		public mclachlan.maze.map.script.Encounter getEncounter(
			Zone zone, int x, int y, int dungeonLevel, int roomIndex)
		{
			return null;
		}

		private mclachlan.crusader.Wall wall(mclachlan.crusader.Texture door)
		{
			mclachlan.crusader.Texture[] doorMask = door == null ? null : new mclachlan.crusader.Texture[]{door};
			return new mclachlan.crusader.Wall(
				new mclachlan.crusader.Texture[]{wallTexture},
				doorMask,
				true,
				true,
				1,
				null,
				null,
				null);
		}

		private static mclachlan.crusader.Wall sampleSolidWall(Map map)
		{
			for (mclachlan.crusader.Wall w : map.getHorizontalWalls())
			{
				if (w != null && w.isSolid())
				{
					return w;
				}
			}
			for (mclachlan.crusader.Wall w : map.getVerticalWalls())
			{
				if (w != null && w.isSolid())
				{
					return w;
				}
			}
			return null;
		}
	}
}

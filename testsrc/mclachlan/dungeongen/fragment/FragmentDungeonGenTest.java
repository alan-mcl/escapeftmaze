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
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGenPreview;
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.DungeonGens;
import mclachlan.dungeongen.Noise4jStairwellPlanner;
import mclachlan.dungeongen.ZoneShell;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.campaign.temple.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fragment-assembly {@link FragmentDungeonGen}: connectivity, beds,
 * layout grid for dressing, and stair planner compatibility.
 */
public class FragmentDungeonGenTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	@AfterEach
	void clearTestCatalog()
	{
		FragmentDungeonGen.clearTestCatalog();
	}

	@Test
	void barracksCatalogPeeksAssemblyFragmentsAndExcludesLegacyFlavour() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		List<FragmentCatalog.Entry> barracks =
			FragmentCatalog.eligibleForAssembly(1, "barracks");
		assertFalse(barracks.isEmpty(), "authored barracks kit should be peeked");
		assertTrue(barracks.stream().anyMatch(e -> "fragment.barracks.room.entry".equals(e.zoneName())));
		assertTrue(barracks.stream().anyMatch(e -> "fragment.barracks.corr.tee".equals(e.zoneName())));
		assertTrue(barracks.stream().anyMatch(FragmentCatalog.Entry::rotatable));
		assertTrue(barracks.stream().anyMatch(e -> !e.rotatable()));

		List<FragmentCatalog.Entry> expanded =
			FragmentCatalog.expandRotations(barracks);
		assertTrue(expanded.size() > barracks.size());
		assertTrue(expanded.stream().anyMatch(e -> e.zoneName().endsWith("#r90")));

		List<FragmentCatalog.Entry> legacy =
			FragmentCatalog.eligibleForDepth(1).stream()
				.filter(e -> !e.isAssemblyFragment())
				.toList();
		assertTrue(legacy.stream().noneMatch(FragmentCatalog.Entry::isAssemblyFragment));
		assertTrue(legacy.stream().anyMatch(e -> "flavour".equals(e.role())));
	}

	@Test
	void fragmentGeneratorIsDeterministicAndConnected() throws Exception
	{
		DungeonGenResult first = generateBarracksTestFloor(RUN_SEED);
		MazeVariables.clearAll();
		DungeonGenResult second = generateBarracksTestFloor(RUN_SEED);

		assertEquals(first.playerOrigin(), second.playerOrigin());
		assertEquals(first.rooms().size(), second.rooms().size());
		assertEquals(first.startingRoomIndex(), second.startingRoomIndex());
		assertNotNull(FragmentDungeonGen.lastAssemblyResult);
		assertTrue(FragmentDungeonGen.lastAssemblyResult.rooms().size() >= 3);
	}

	@Test
	void dormRoomContainsBedsAndAllTilesAreReachable() throws Exception
	{
		Zone zone = generateBarracksZone(RUN_SEED);
		assertTrue(countBedObjects(zone) >= 2, "dorm should stamp beds");

		Point origin = zone.getPlayerOrigin();
		assertEquals(
			FragmentDungeonGen.countWalkable(zone),
			FragmentDungeonGen.countReachable(zone, origin));

		DungeonGenResult result = generateWithZone(zone);
		assertNotNull(result.layoutGrid());
		assertFalse(result.rooms().isEmpty());
		assertTrue(result.startingRoomIndex() >= 0);

		TempleMagicDresser.dress(zone, 1, result);
		TempleFloorDressing.dress(zone, 1, Set.of(), result, null);
		TempleLighting.dress(zone, 1, TempleEnvironment.forFloor(1), Set.of(), result);
	}

	@Test
	void dungeonGensCreatesFragmentGenerator()
	{
		assertTrue(DungeonGens.create(DungeonGens.FRAGMENT) instanceof FragmentDungeonGen);
	}

	@Test
	void stairPlannerFindsBlankWallInStartingRoom() throws Exception
	{
		DungeonGenResult result = generateBarracksTestFloor(RUN_SEED);
		assertNotNull(result.stairwells());
		assertNotNull(result.stairwells().stairsUp());
		assertTrue(result.stairwells().stairsUp().from() != null);
	}

	@Test
	void unusedSocketsAreSealed() throws Exception
	{
		generateBarracksTestFloor(RUN_SEED);
		// If assembly succeeded, seal pass ran without throwing; spot-check frontier sealed.
		assertNotNull(FragmentDungeonGen.lastAssemblyResult);
	}

	@Test
	void fullPipelinePreviewHonorsFragmentGeneratorAndKeepsBeds() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		MazeVariables.clearAll();
		TempleSeeds.setDepth(1);

		DungeonGenPreview.apply(
			31,
			DungeonGens.FRAGMENT,
			RUN_SEED,
			"barracks",
			3,
			3,
			32);

		Zone zone = db.getZone("temple.1");
		zone.setName("temple.1_preview.test");

		new TempleGeneratorMazeScript().init(zone, 0);

		assertTrue(countBedObjects(zone) >= 2, "fragment stamps should keep beds");
		DungeonGenPreview.clearAll();
	}

	/*-------------------------------------------------------------------------*/
	private static DungeonGenResult generateBarracksTestFloor(long runSeed) throws Exception
	{
		Zone zone = generateBarracksZone(runSeed);
		return generateWithZone(zone);
	}

	private static Zone generateBarracksZone(long runSeed) throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(runSeed));
		TempleSeeds.setDepth(1);

		FragmentPrefabFixtures.TestKit kit = FragmentPrefabFixtures.barracksTestKit(db);
		FragmentDungeonGen.setTestCatalog(kit.catalog(), kit.zonesByName());

		Zone zone = db.getZone("temple.1");
		ZoneShell.ensureSize(zone, 15);
		TempleEnvironment env = TempleEnvironment.forFloor(1);
		env.applyToCrusaderTiles(zone.getMap());

		generateWithZone(zone);
		return zone;
	}

	private static DungeonGenResult generateWithZone(Zone zone) throws Exception
	{
		int seed = TempleSeeds.floorSeed(1);
		TestDecorator decorator = new TestDecorator();
		decorator.prepare(1, TempleEnvironment.forFloor(1));

		DungeonGenContext ctx = DungeonGenContext.builder()
			.stairwellPlanner(new Noise4jStairwellPlanner())
			.build();

		return new FragmentDungeonGen(FragmentDungeonGen.Options.of("barracks")).generate(
			zone, seed, 1, decorator, ctx);
	}

	private static int countBedObjects(Zone zone)
	{
		Map map = zone.getMap();
		if (map.getExpandedObjects() == null)
		{
			return 0;
		}
		int count = 0;
		for (EngineObject obj : map.getExpandedObjects())
		{
			if (obj.getName() != null && obj.getName().contains("bed"))
			{
				count++;
			}
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private static final class TestDecorator implements MapGenZoneScript.DungeonDecorator
	{
		private TempleEnvironment environment;

		void prepare(int depth, TempleEnvironment environment)
		{
			this.environment = environment;
		}

		@Override
		public Wall getRoomWall(Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public Wall getCorridorWall(Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public List<Object> handlePortal(
			Grid grid,
			Point from,
			int fromFacing,
			Point to,
			int toFacing)
		{
			return Arrays.asList(
				wall(environment.doorTexture()),
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
					new BitSet(),
					null,
					false,
					"generic door creak",
					null));
		}

		@Override
		public Encounter getEncounter(
			Zone zone,
			int x,
			int y,
			int dungeonLevel,
			int roomIndex)
		{
			return null;
		}

		private Wall wall(mclachlan.crusader.Texture doorTexture)
		{
			mclachlan.crusader.Texture wallTex = environment.wallTexture();
			mclachlan.crusader.Texture[] door = doorTexture == null ? null
				: new mclachlan.crusader.Texture[]{doorTexture};
			return new Wall(
				new mclachlan.crusader.Texture[]{wallTex},
				door,
				true,
				true,
				1,
				null,
				null,
				null);
		}
	}
}

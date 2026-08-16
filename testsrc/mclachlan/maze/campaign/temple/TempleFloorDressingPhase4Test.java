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
import mclachlan.crusader.EngineObject;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4a.1: room-shared encounters and wall chest dressing.
 */
public class TempleFloorDressingPhase4Test extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void doorTilesInSameRoomShareEncounterVariable() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generate(db, 1);
		Map<String, List<Point>> byVar = new HashMap<>();
		for (Point p : TempleFloorDressing.findEncounterTiles(zone))
		{
			Encounter enc = encounterAt(zone, p);
			byVar.computeIfAbsent(enc.getMazeVariable(), k -> new ArrayList<>()).add(p);
		}

		assertFalse(byVar.isEmpty());
		boolean shared = byVar.values().stream().anyMatch(list -> list.size() >= 2);
		assertTrue(shared, "expected at least one room with multiple door encounters sharing a var");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void chestsUseLootTableAndPersistPick() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateWithWallChests(db, 1);
		List<Point> chests = TempleFloorDressing.findChestTiles(zone);
		assertFalse(chests.isEmpty());

		for (Point p : chests)
		{
			Chest chest = (Chest)zone.getTile(p).getScripts().stream()
				.filter(s -> s instanceof Chest)
				.findFirst()
				.orElseThrow();
			assertNotNull(chest.getChestContents());
			assertTrue(chest.getMazeVariable().startsWith("temple.d.1.loot."));
		}

		assertTrue(MazeVariables.get(TempleSeeds.rosterVar(1)) != null);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void wallChestLidFacesIntoRoom() throws Exception
	{
		for (int wallFacing : List.of(
			CrusaderEngine.Facing.NORTH,
			CrusaderEngine.Facing.SOUTH,
			CrusaderEngine.Facing.EAST,
			CrusaderEngine.Facing.WEST))
		{
			assertEquals(
				"CHEST_1_FRONT",
				TempleFloorDressing.chestTextureFacingWall(wallFacing),
				"party facing the wall should see the chest lid");
			assertEquals(
				"CHEST_1_BACK",
				TempleFloorDressing.chestTextureIntoRoom(wallFacing),
				"chest back should face into the room");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void generatedChestsUseWallPlacement() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateWithWallChests(db, 1);
		for (Point p : TempleFloorDressing.findChestTiles(zone))
		{
			Chest chest = (Chest)zone.getTile(p).getScripts().stream()
				.filter(s -> s instanceof Chest)
				.findFirst()
				.orElseThrow();
			assertNotEquals(
				EngineObject.Placement.CENTER,
				chest.getObjectPlacement(),
				"wall chest at " + p + " should hug its wall");
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Zone generate(Database db, int depth)
	{
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, Integer.toString(depth));
		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);
		return zone;
	}

	/** Wall-chest tests require a non-storage usage theme (storage uses hidden barrels). */
	private static Zone generateWithWallChests(Database db, int depth)
	{
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, Integer.toString(depth));
		MazeVariables.set(
			TempleSeededPicks.pickVar(depth, TempleUsageTheme.USAGE_PURPOSE),
			"library");
		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);
		return zone;
	}

	private static Encounter encounterAt(Zone zone, Point tile)
	{
		return (Encounter)zone.getTile(tile).getScripts().stream()
			.filter(s -> s instanceof Encounter)
			.findFirst()
			.orElseThrow();
	}
}

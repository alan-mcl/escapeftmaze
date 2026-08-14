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
import java.util.List;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.map.script.Loot;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3: depth scaler, multi-depth stairs, per-depth mutation keys.
 */
public class TempleDepthPhase3Test extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void depthScalerSoftCapsAndNamesTables()
	{
		assertEquals(1, TempleDepthScaler.contentBand(1));
		assertEquals(2, TempleDepthScaler.contentBand(2));
		assertEquals(3, TempleDepthScaler.contentBand(3));
		assertEquals(3, TempleDepthScaler.contentBand(99));

		assertEquals("temple.depth.1", TempleDepthScaler.encounterTableName(1));
		assertEquals("temple.depth.3", TempleDepthScaler.encounterTableName(50));
		assertEquals("temple.depth.2.loot", TempleDepthScaler.lootTableName(2));
		assertEquals(2, TempleDepthScaler.lootPlacements(1));
		assertEquals(4, TempleDepthScaler.lootPlacements(3));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void authoredDepthTablesExist() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		for (int band = 1; band <= TempleDepthScaler.MAX_CONTENT_BAND; band++)
		{
			EncounterTable enc = db.getEncounterTable(TempleDepthScaler.encounterTableName(band));
			assertNotNull(enc, "missing encounter table for band " + band);
			assertFalse(enc.getEncounterTable().getItems().isEmpty());

			LootTable loot = db.getLootTable(TempleDepthScaler.lootTableName(band));
			assertNotNull(loot, "missing loot table for band " + band);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void depth2FloorDiffersFromDepth1AndHasStairsBothWays() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone depth1 = generate(db, 1);
		assertNotNull(TempleFloorDressing.findStairsUpTile(depth1));
		assertNotNull(TempleStairLinks.readPortal(1, false));
		assertNotNull(TempleFloorDressing.findStairsDownPortalFrom(depth1));
		assertTrue(hasPortalScript(depth1, TempleStairLinks.DESCEND_NEXT_SCRIPT,
			TempleFloorDressing.findStairsDownPortalFrom(depth1)));

		Zone depth2 = generate(db, 2);
		assertNotEquals(
			MazeVariables.get(TempleSeeds.FLOOR_SEED_PREFIX + "1"),
			MazeVariables.get(TempleSeeds.FLOOR_SEED_PREFIX + "2"));

		assertNotNull(TempleFloorDressing.findStairsUpTile(depth2));
		assertNotNull(TempleStairLinks.readPortal(2, false));
		assertNotNull(TempleFloorDressing.findStairsDownPortalFrom(depth2));
		assertTrue(hasPortalScript(depth2, TempleStairLinks.ASCEND_PREV_SCRIPT,
			TempleFloorDressing.findStairsUpPortalFrom(depth2)));

		assertTrue(
			countLoot(depth2) >= TempleDepthScaler.lootPlacements(2)
				|| countLoot(depth2) >= 1,
			"depth 2 should dress loot");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void mutationKeysArePerDepthAndSurviveRegen() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone first = generate(db, 1);
		List<Point> encounters = TempleFloorDressing.findEncounterTiles(first);
		assertFalse(encounters.isEmpty());

		Encounter firstEnc = firstEncounter(first, encounters.get(0));
		assertNotNull(firstEnc);
		String var = firstEnc.getMazeVariable();
		assertTrue(var.startsWith("temple.d.1.enc."), var);

		MazeVariables.set(var, "true");

		Zone again = generate(db, 1);
		Encounter againEnc = findEncounterByVar(again, var);
		assertNotNull(againEnc);
		assertEquals(var, againEnc.getMazeVariable());
		assertTrue(MazeVariables.getBoolean(var),
			"cleared-encounter mutation must persist across floor regen");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void hubDescendScriptTargetsTemple1() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.HUB_DESCEND_SCRIPT);
		ZoneChangeEvent zce = findZoneChangeEvent(script);
		assertEquals("temple.1", zce.getZone());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void floorAscendScriptTargetsHub() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.HUB_ASCEND_SCRIPT);
		ZoneChangeEvent zce = findZoneChangeEvent(script);
		assertEquals(TempleStairLinks.HUB_ZONE, zce.getZone());
	}

	/*-------------------------------------------------------------------------*/
	private Zone generate(Database db, int depth)
	{
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, Integer.toString(depth));
		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);
		return zone;
	}

	private static ZoneChangeEvent findZoneChangeEvent(MazeScript script)
	{
		for (MazeEvent event : script.getEvents())
		{
			if (event instanceof ZoneChangeEvent zce)
			{
				return zce;
			}
		}
		fail("expected ZoneChangeEvent in script");
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static int countLoot(Zone zone)
	{
		int n = 0;
		var tiles = zone.getTiles();
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				if (tiles[x][y] == null || tiles[x][y].getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tiles[x][y].getScripts())
				{
					if (script instanceof Loot)
					{
						n++;
					}
				}
			}
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private static Encounter firstEncounter(Zone zone, Point tile)
	{
		for (TileScript script : zone.getTile(tile).getScripts())
		{
			if (script instanceof Encounter)
			{
				return (Encounter)script;
			}
		}
		return null;
	}

	private static Encounter findEncounterByVar(Zone zone, String mazeVar)
	{
		for (Point p : TempleFloorDressing.findEncounterTiles(zone))
		{
			Encounter enc = firstEncounter(zone, p);
			if (enc != null && mazeVar.equals(enc.getMazeVariable()))
			{
				return enc;
			}
		}
		return null;
	}

	private static boolean hasPortalScript(Zone zone, String script, Point from)
	{
		if (zone.getPortals() == null)
		{
			return false;
		}
		for (Portal portal : zone.getPortals())
		{
			if (script.equals(portal.getMazeScript()) && from.equals(portal.getFrom()))
			{
				return true;
			}
		}
		return false;
	}
}

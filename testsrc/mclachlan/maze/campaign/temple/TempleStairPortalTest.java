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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.IncrementMazeVariableEvent;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Portal-based stairwells: hub ↔ generated floors and depth N ↔ N±1.
 */
public class TempleStairPortalTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	@Test
	void depth1HasUpPortalBackToHubAndDownPortalToNextDepth() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 1);
		Point up = TempleFloorDressing.findStairsUpPortalFrom(zone);

		assertNotNull(up, "expected up stair portal");
		assertTrue(hasPortalScript(zone, TempleStairLinks.HUB_ASCEND_SCRIPT, up));
		assertNotNull(TempleFloorDressing.findStairsDownPortalFrom(zone));
		assertTrue(hasPortalScript(zone, TempleStairLinks.DESCEND_NEXT_SCRIPT,
			TempleFloorDressing.findStairsDownPortalFrom(zone)));
	}

	@Test
	void upPortalIsNotOnDoorJunction() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 1);
		Portal up = findUpPortal(zone);
		assertNotNull(up, "expected up stair portal");

		for (Portal portal : zone.getPortals())
		{
			if ("generic door creak".equals(portal.getMazeScript()))
			{
				assertFalse(
					up.getFrom().equals(portal.getFrom()) && up.getFromFacing() == portal.getFromFacing(),
					"up stair must not share a door junction");
				assertFalse(
					up.getTo().equals(portal.getTo()),
					"up stair to-tile must not be a corridor door opening");
			}
		}
	}

	@Test
	void enteringFromHubSpawnsAtUpPortalFacingAway() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.set(TempleStairLinks.TRANSITION_MODE, "from_hub");
		Zone zone = db.getZone("temple.1");
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");
		List<MazeEvent> initEvents = zone.getScript().init(zone, 0);

		Portal up = findUpPortal(zone);
		assertNotNull(up, "expected up stair portal");
		assertEquals(up.getFrom(), zone.getPlayerOrigin(),
			"descending from hub should spawn on up-portal tile");

		MovePartyEvent move = findMoveParty(initEvents);
		assertEquals(up.getFrom(), move.getPos());
		assertEquals(StairPortalSpec.oppositeFacing(up.getFromFacing()), move.getFacing(),
			"arrival should face away from the stair texture");
		assertEquals(move.getFacing(), Maze.resolveSpawnFacing(
			ZoneChangeEvent.Facing.UNCHANGED, initEvents, CrusaderEngine.Facing.NORTH));
	}

	@Test
	void depth2UsesAscendPrevAndDescendNext() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 2);
		Point up = TempleFloorDressing.findStairsUpPortalFrom(zone);
		Point down = TempleFloorDressing.findStairsDownPortalFrom(zone);

		assertNotNull(up);
		assertNotNull(down);
		assertTrue(hasPortalScript(zone, TempleStairLinks.ASCEND_PREV_SCRIPT, up));
		assertTrue(hasPortalScript(zone, TempleStairLinks.DESCEND_NEXT_SCRIPT, down));
	}

	@Test
	void enteringFromAboveSpawnsAtUpPortalFacingAway() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.set(TempleStairLinks.TRANSITION_MODE, "from_above");
		Zone zone = db.getZone("temple.1");
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "2");
		List<MazeEvent> initEvents = zone.getScript().init(zone, 0);

		Portal up = findUpPortal(zone);
		assertNotNull(up);
		MovePartyEvent move = findMoveParty(initEvents);
		assertEquals(up.getFrom(), move.getPos());
		assertEquals(StairPortalSpec.oppositeFacing(up.getFromFacing()), move.getFacing());
	}

	@Test
	void enteringFromBelowSpawnsAtDownPortalFacingAway() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.set(TempleStairLinks.TRANSITION_MODE, "from_below");
		Zone zone = db.getZone("temple.1");
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");
		List<MazeEvent> initEvents = zone.getScript().init(zone, 0);

		Point down = TempleFloorDressing.findStairsDownPortalFrom(zone);
		assertNotNull(down);
		MovePartyEvent move = findMoveParty(initEvents);
		assertEquals(down, move.getPos());

		StairPortalSpec downSpec = TempleStairLinks.readPortal(1, false);
		assertNotNull(downSpec);
		assertEquals(downSpec.spawnFacing(), move.getFacing());
	}

	@Test
	void hubDescendScriptChangesToTemple1() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.HUB_DESCEND_SCRIPT);
		ZoneChangeEvent zce = findZoneChange(script);
		assertEquals(TempleStairLinks.FLOOR_ZONE, zce.getZone());
		assertEquals(ZoneChangeEvent.Facing.UNCHANGED, zce.getFacing(),
			"generated floor facing comes from stair spawn, not a hardcoded compass");
	}

	@Test
	void descendNextScriptIncrementsDepthAndReloadsFloorShell() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.DESCEND_NEXT_SCRIPT);
		IncrementMazeVariableEvent inc = findIncrement(script);
		assertEquals(TempleSeeds.DEPTH, inc.getMazeVariable());
		assertEquals(1, inc.getAmount());
		assertEquals("from_above", findSetVar(script, TempleStairLinks.TRANSITION_MODE));

		ZoneChangeEvent zce = findZoneChange(script);
		assertEquals(TempleStairLinks.FLOOR_ZONE, zce.getZone());
		assertEquals(ZoneChangeEvent.Facing.UNCHANGED, zce.getFacing());
	}

	@Test
	void ascendPrevScriptDecrementsDepthAndReloadsFloorShell() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.ASCEND_PREV_SCRIPT);
		IncrementMazeVariableEvent inc = findIncrement(script);
		assertEquals(TempleSeeds.DEPTH, inc.getMazeVariable());
		assertEquals(-1, inc.getAmount());
		assertEquals("from_below", findSetVar(script, TempleStairLinks.TRANSITION_MODE));

		ZoneChangeEvent zce = findZoneChange(script);
		assertEquals(TempleStairLinks.FLOOR_ZONE, zce.getZone());
		assertEquals(ZoneChangeEvent.Facing.UNCHANGED, zce.getFacing());
	}

	@Test
	void floorAscendScriptChangesToHubAndClearsDepth() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeScript script = db.getMazeScript(TempleStairLinks.HUB_ASCEND_SCRIPT);
		ZoneChangeEvent zce = findZoneChange(script);
		TempleStairLinks.SpawnSpec hubSpawn = TempleStairLinks.hubSpawn();
		assertEquals(TempleStairLinks.HUB_ZONE, zce.getZone());
		assertEquals(hubSpawn.pos(), zce.getPos());
		assertEquals(hubSpawn.facing(), zce.getFacing(),
			"return to hub should face away from the descend-stair texture");
		assertEquals("0", findSetVar(script, TempleSeeds.DEPTH));
	}

	@Test
	void generatedFloorKeepsShellNameAndSetsDisplayName() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth(db, 2);
		assertEquals(TempleStairLinks.FLOOR_ZONE, zone.getName());
		assertEquals("Temple Depth 2", zone.getDisplayName());
		assertEquals("Temple Depth 2", zone.getUiName());

		Zone hub = db.getZone(TempleStairLinks.HUB_ZONE);
		assertEquals("Temple Courtyard", hub.getUiName());
	}

	/*-------------------------------------------------------------------------*/
	private static Zone generateDepth(Database db, int depth)
	{
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, Integer.toString(depth));
		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);
		return zone;
	}

	private static MovePartyEvent findMoveParty(List<MazeEvent> events)
	{
		for (MazeEvent event : events)
		{
			if (event instanceof MovePartyEvent mpe)
			{
				return mpe;
			}
		}
		fail("expected MovePartyEvent");
		return null;
	}

	private static ZoneChangeEvent findZoneChange(MazeScript script)
	{
		for (MazeEvent event : script.getEvents())
		{
			if (event instanceof ZoneChangeEvent zce)
			{
				return zce;
			}
		}
		fail("expected ZoneChangeEvent");
		return null;
	}

	private static IncrementMazeVariableEvent findIncrement(MazeScript script)
	{
		for (MazeEvent event : script.getEvents())
		{
			if (event instanceof IncrementMazeVariableEvent ime)
			{
				return ime;
			}
		}
		fail("expected IncrementMazeVariableEvent");
		return null;
	}

	private static String findSetVar(MazeScript script, String mazeVariable)
	{
		for (MazeEvent event : script.getEvents())
		{
			if (event instanceof mclachlan.maze.map.script.SetMazeVariableEvent sme
				&& mazeVariable.equals(sme.getMazeVariable()))
			{
				return sme.getValue();
			}
		}
		fail("expected SetMazeVariableEvent for " + mazeVariable);
		return null;
	}

	private static Portal findUpPortal(Zone zone)
	{
		if (zone.getPortals() == null)
		{
			return null;
		}
		for (Portal portal : zone.getPortals())
		{
			String script = portal.getMazeScript();
			if (TempleStairLinks.HUB_ASCEND_SCRIPT.equals(script)
				|| TempleStairLinks.ASCEND_PREV_SCRIPT.equals(script))
			{
				return portal;
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

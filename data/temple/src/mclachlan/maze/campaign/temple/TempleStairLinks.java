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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 * Persists stair portal geometry per depth and resolves spawn for depth transitions.
 */
public final class TempleStairLinks
{
	public static final String TRANSITION_MODE = "temple.transition.mode";
	public static final String HUB_PORTAL_DOWN = "temple.hub.portal.down";
	public static final String HUB_DESCEND_SCRIPT = "temple.descend.1";
	public static final String HUB_ASCEND_SCRIPT = "temple.ascend.1";
	public static final String DESCEND_NEXT_SCRIPT = "temple.descend.next";
	public static final String ASCEND_PREV_SCRIPT = "temple.ascend.prev";
	public static final String FLOOR_ZONE = TempleFloorLabels.FLOOR_ZONE_ID;
	public static final String HUB_ZONE = "Temple Hub";

	private static final Point FALLBACK_HUB_STAIR = new Point(5, 8);

	private TempleStairLinks()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void clearTransition()
	{
		MazeVariables.clear(TRANSITION_MODE);
		MazeVariables.clear(TempleSeeds.TRANSITION_SOURCE_DEPTH);
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGenContext.EntryMode currentEntryMode()
	{
		String raw = MazeVariables.get(TRANSITION_MODE);
		if (raw == null || raw.isEmpty())
		{
			return DungeonGenContext.EntryMode.FRESH;
		}
		try
		{
			return DungeonGenContext.EntryMode.valueOf(raw.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return DungeonGenContext.EntryMode.FRESH;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static int transitionSourceDepth()
	{
		String raw = MazeVariables.get(TempleSeeds.TRANSITION_SOURCE_DEPTH);
		if (raw == null || raw.isEmpty())
		{
			return -1;
		}
		return Integer.parseInt(raw);
	}

	/*-------------------------------------------------------------------------*/
	public static void writePortal(int depth, boolean up, StairPortalSpec spec)
	{
		if (spec == null)
		{
			MazeVariables.clear(portalVar(depth, up));
			return;
		}
		MazeVariables.set(portalVar(depth, up), spec.encode());
	}

	/*-------------------------------------------------------------------------*/
	public static StairPortalSpec readPortal(int depth, boolean up)
	{
		String raw = MazeVariables.get(portalVar(depth, up));
		if (raw == null || raw.isEmpty())
		{
			return null;
		}
		return StairPortalSpec.decode(raw);
	}

	/*-------------------------------------------------------------------------*/
	public static void persistPlan(int depth, mclachlan.dungeongen.StairwellPlan plan)
	{
		if (plan == null)
		{
			return;
		}
		writePortal(depth, true, plan.stairsUp());
		writePortal(depth, false, plan.stairsDown());
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGenContext buildGenContext(int depth)
	{
		return DungeonGenContext.builder()
			.entryMode(currentEntryMode())
			.sourceDepth(transitionSourceDepth())
			.restoredUp(readPortal(depth, true))
			.restoredDown(readPortal(depth, false))
			.stairwellPlanner(new Noise4jStairwellPlanner())
			.build();
	}

	/*-------------------------------------------------------------------------*/
	public record SpawnSpec(Point pos, int facing)
	{
	}

	/*-------------------------------------------------------------------------*/
	/** Spawn for hub return after ascending from depth 1. */
	public static SpawnSpec hubSpawn()
	{
		StairPortalSpec hub = hubDownPortal();
		return new SpawnSpec(hub.from(), hub.spawnFacing());
	}

	/*-------------------------------------------------------------------------*/
	public static StairPortalSpec hubDownPortal()
	{
		String cached = MazeVariables.get(HUB_PORTAL_DOWN);
		if (cached != null && !cached.isEmpty())
		{
			return StairPortalSpec.decode(cached);
		}

		StairPortalSpec discovered = discoverHubDownPortal();
		MazeVariables.set(HUB_PORTAL_DOWN, discovered.encode());
		return discovered;
	}

	/*-------------------------------------------------------------------------*/
	private static StairPortalSpec discoverHubDownPortal()
	{
		try
		{
			Zone hub = Database.getInstance().getZone(HUB_ZONE);
			if (hub.getPortals() != null)
			{
				for (Portal portal : hub.getPortals())
				{
					if (HUB_DESCEND_SCRIPT.equals(portal.getMazeScript()))
					{
						return new StairPortalSpec(
							portal.getFrom(),
							portal.getFromFacing(),
							portal.getTo(),
							portal.getToFacing(),
							portal.getFromFacing() == CrusaderEngine.Facing.NORTH
								|| portal.getFromFacing() == CrusaderEngine.Facing.SOUTH,
							StairPortalSpec.StairMask.STAIR_DOWN);
					}
				}
			}
		}
		catch (Exception ignored)
		{
			// tests without full hub load fall back
		}

		return new StairPortalSpec(
			FALLBACK_HUB_STAIR,
			CrusaderEngine.Facing.WEST,
			new Point(FALLBACK_HUB_STAIR.x - 1, FALLBACK_HUB_STAIR.y),
			CrusaderEngine.Facing.NORTH,
			true,
			StairPortalSpec.StairMask.STAIR_DOWN);
	}

	/*-------------------------------------------------------------------------*/
	private static String portalVar(int depth, boolean up)
	{
		return TempleSeeds.portalVar(depth, up);
	}

	/*-------------------------------------------------------------------------*/
	public static String ascendScriptForDepth(int depth)
	{
		return depth <= 1 ? HUB_ASCEND_SCRIPT : ASCEND_PREV_SCRIPT;
	}

	/*-------------------------------------------------------------------------*/
	public static String descendScriptForDepth(int depth)
	{
		return DESCEND_NEXT_SCRIPT;
	}
}

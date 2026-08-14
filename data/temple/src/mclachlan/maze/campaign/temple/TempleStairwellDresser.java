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
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.dungeongen.StairwellPlan;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 * Applies stair portal walls and {@link Portal} objects to a generated floor.
 */
public final class TempleStairwellDresser
{
	private TempleStairwellDresser()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void apply(Zone zone, StairwellPlan plan)
	{
		if (plan == null)
		{
			return;
		}

		List<Portal> portals = new ArrayList<>();
		if (zone.getPortals() != null)
		{
			portals.addAll(Arrays.asList(zone.getPortals()));
		}

		if (plan.stairsUp() != null)
		{
			applyPortal(zone, plan.stairsUp(), portals, TempleStairLinks.HUB_ASCEND_SCRIPT);
		}
		if (plan.stairsDown() != null)
		{
			// Visual down-stair only until multi-depth zone changes return.
			applyStairWall(zone, plan.stairsDown());
		}

		zone.setPortals(portals.toArray(new Portal[0]));
		zone.getMap().init();
	}

	/*-------------------------------------------------------------------------*/
	private static void applyPortal(
		Zone zone,
		StairPortalSpec spec,
		List<Portal> portals,
		String mazeScript)
	{
		applyStairWall(zone, spec);
		portals.add(new Portal(
			null,
			Portal.State.UNLOCKED,
			spec.from(),
			spec.fromFacing(),
			spec.to(),
			spec.toFacing(),
			false,
			true,
			true,
			true,
			1,
			1,
			new int[]{0, 0, 0, 0, 0, 0, 0, 0},
			new BitSet(),
			null,
			false,
			mazeScript,
			null));
	}

	/*-------------------------------------------------------------------------*/
	private static void applyStairWall(Zone zone, StairPortalSpec spec)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		Wall stairWall = stairWall(spec.mask());

		if (spec.horizontalWall())
		{
			map.getHorizontalWalls()[wallIndex(spec, width)] = stairWall;
		}
		else
		{
			map.getVerticalWalls()[wallIndex(spec, width)] = stairWall;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static int wallIndex(StairPortalSpec spec, int width)
	{
		Point from = spec.from();
		if (spec.horizontalWall())
		{
			if (spec.fromFacing() == mclachlan.crusader.CrusaderEngine.Facing.NORTH)
			{
				return from.x + from.y * width;
			}
			return from.x + (from.y + 1) * width;
		}
		if (spec.fromFacing() == mclachlan.crusader.CrusaderEngine.Facing.WEST)
		{
			return from.x + from.y * (width + 1);
		}
		return from.x + from.y * (width + 1) + 1;
	}

	/*-------------------------------------------------------------------------*/
	private static Wall stairWall(StairPortalSpec.StairMask mask)
	{
		Texture wallTex =
			Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture();
		Texture maskTex =
			Database.getInstance().getMazeTexture(mask.textureName()).getTexture();
		return new Wall(
			new Texture[]{wallTex},
			new Texture[]{maskTex},
			true,
			true,
			1,
			null,
			null,
			null);
	}
}

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

package mclachlan.maze.editor.swing;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;

/**
 * Layout-only decorator for dungeon-gen previews: shell wall textures and doors.
 */
public final class PreviewDecorator implements MapGenZoneScript.DungeonDecorator
{
	private final Texture wallTexture;
	private final Texture doorTexture;

	public PreviewDecorator(Zone shell)
	{
		Wall sample = sampleSolidWall(shell.getMap());
		wallTexture = sample == null ? Map.NO_WALL : sample.getTexture(0);
		doorTexture = sample == null ? null : sample.getMaskTexture(0);
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
			wall(doorTexture),
			new Portal(
				null,
				Portal.State.UNLOCKED,
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
	public Encounter getEncounter(Zone zone, int x, int y, int dungeonLevel, int roomIndex)
	{
		return null;
	}

	private Wall wall(Texture door)
	{
		Texture[] doorMask = door == null ? null : new Texture[]{door};
		return new Wall(
			new Texture[]{wallTexture},
			doorMask,
			true,
			true,
			1,
			null,
			null,
			null);
	}

	private static Wall sampleSolidWall(Map map)
	{
		for (Wall w : map.getHorizontalWalls())
		{
			if (w != null && w.isSolid())
			{
				return w;
			}
		}
		for (Wall w : map.getVerticalWalls())
		{
			if (w != null && w.isSolid())
			{
				return w;
			}
		}
		return null;
	}
}

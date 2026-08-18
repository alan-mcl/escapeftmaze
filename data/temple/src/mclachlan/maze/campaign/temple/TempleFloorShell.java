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

import mclachlan.dungeongen.DungeonGenPreview;
import mclachlan.dungeongen.ZoneShell;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;

/**
 * Shrinks the {@code temple.1} palette shell before generation for faster testing.
 */
public final class TempleFloorShell
{
	/** Odd size friendly to Noise4j; raise when full 31×31 floors are desired. */
	public static final int GEN_SIZE = 15;

	/** Editor Tools override; cleared after preview generate. */
	public static final String PREVIEW_SIZE_VAR = DungeonGenPreview.SIZE_VAR;

	private TempleFloorShell()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void ensureGenSize(Zone zone)
	{
		ensureGenSize(zone, resolveGenSize());
	}

	/*-------------------------------------------------------------------------*/
	public static void ensureGenSize(Zone zone, int size)
	{
		ZoneShell.ensureSize(zone, size);
	}

	/*-------------------------------------------------------------------------*/
	private static int resolveGenSize()
	{
		String override = MazeVariables.get(PREVIEW_SIZE_VAR);
		if (override != null && !override.isEmpty())
		{
			try
			{
				return Integer.parseInt(override.trim());
			}
			catch (NumberFormatException e)
			{
				// fall through
			}
		}
		return GEN_SIZE;
	}
}

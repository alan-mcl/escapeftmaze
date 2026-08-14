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

import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.util.MazeException;

/**
 * Selects a {@link DungeonGen} implementation for temple floor layout by depth.
 * Orthogonal to {@link TempleDepthScaler} (content bands vs layout algorithm).
 * <p>
 * Registered ids: {@link #NOISE4J}. Future: {@link #WFC}, {@link #BSP}.
 */
public final class TempleLayoutPolicy
{
	public static final String NOISE4J = "noise4j";
	public static final String WFC = "wfc";
	public static final String BSP = "bsp";

	private TempleLayoutPolicy()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Layout generator for the given delve depth. All depths use Noise4j until
	 * a WFC/BSP {@link DungeonGen} is registered.
	 */
	public static DungeonGen forDepth(int depth)
	{
		return create(generatorIdForDepth(depth));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return generator id for depth (extension point for per-depth algorithms).
	 */
	public static String generatorIdForDepth(int depth)
	{
		// Future: e.g. depth >= 4 -> WFC. For now every depth uses Noise4j.
		return NOISE4J;
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGen create(String generatorId)
	{
		if (NOISE4J.equals(generatorId))
		{
			return new Noise4jDungeonGen();
		}
		throw new MazeException("Unknown temple layout generator [" + generatorId + "]");
	}
}

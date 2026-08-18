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

package mclachlan.dungeongen;

import java.util.*;
import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.util.MazeException;

/**
 * Built-in {@link DungeonGen} factory driven by campaign configuration.
 */
public final class DungeonGens
{
	public static final String NOISE4J = "noise4j";
	public static final String FRAGMENT = "fragment";

	private static final List<String> BUILT_IN_ORDER = List.of(NOISE4J, FRAGMENT);
	private static final Set<String> BUILT_INS = Set.copyOf(BUILT_IN_ORDER);

	private DungeonGens()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static List<String> builtInIds()
	{
		return BUILT_IN_ORDER;
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGen create(String id)
	{
		return create(id, FragmentDungeonGen.Options.defaults());
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGen create(String id, FragmentDungeonGen.Options fragmentOptions)
	{
		if (NOISE4J.equals(id))
		{
			return new Noise4jDungeonGen();
		}
		if (FRAGMENT.equals(id))
		{
			return new FragmentDungeonGen(fragmentOptions);
		}
		throw new MazeException("Unknown dungeon generator [" + id + "]");
	}

	/*-------------------------------------------------------------------------*/
	public static DungeonGen createDefault(Campaign campaign)
	{
		String id = campaign == null ? NOISE4J : campaign.getDefaultDungeonGenerator();
		if (id == null || id.isEmpty())
		{
			id = NOISE4J;
		}
		return create(id);
	}

	/*-------------------------------------------------------------------------*/
	public static List<String> idsFor(Campaign campaign)
	{
		List<String> configured = campaign == null
			? List.of()
			: campaign.getDungeonGenerators();
		if (configured.isEmpty())
		{
			return List.of(NOISE4J);
		}

		List<String> result = new ArrayList<>();
		for (String id : configured)
		{
			if (id == null || id.isEmpty())
			{
				continue;
			}
			String trimmed = id.trim();
			if (BUILT_INS.contains(trimmed))
			{
				if (!result.contains(trimmed))
				{
					result.add(trimmed);
				}
			}
			else
			{
				System.err.println("DungeonGens: ignoring unknown generator id [" + trimmed + "]");
			}
		}
		if (result.isEmpty())
		{
			return List.of(NOISE4J);
		}
		return result;
	}
}

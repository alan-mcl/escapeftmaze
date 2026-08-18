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

import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.maze.game.MazeVariables;

/**
 * Editor Tools preview overrides via {@link MazeVariables}. Cleared after each
 * preview generate so live crawls are unaffected.
 */
public final class DungeonGenPreview
{
	public static final String SIZE_VAR = "dungeongen.size";
	public static final String GENERATOR_VAR = "dungeongen.generator";
	public static final String SEED_VAR = "dungeongen.seed";
	public static final String FRAGMENT_USAGE_VAR = "dungeongen.fragment.usage";
	public static final String FRAGMENT_MIN_ROOMS_VAR = "dungeongen.fragment.minRooms";
	public static final String FRAGMENT_TARGET_ROOMS_VAR = "dungeongen.fragment.targetRooms";
	public static final String FRAGMENT_MAX_ATTEMPTS_VAR = "dungeongen.fragment.maxAttempts";

	private DungeonGenPreview()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void apply(
		int size,
		String generatorId,
		long seed,
		String fragmentUsage,
		int fragmentMinRooms,
		int fragmentTargetRooms,
		int fragmentMaxAttempts)
	{
		MazeVariables.set(SIZE_VAR, Integer.toString(size));
		MazeVariables.set(GENERATOR_VAR, generatorId);
		MazeVariables.set(SEED_VAR, Long.toString(seed));
		if (DungeonGens.FRAGMENT.equals(generatorId))
		{
			MazeVariables.set(FRAGMENT_USAGE_VAR, fragmentUsage);
			MazeVariables.set(FRAGMENT_MIN_ROOMS_VAR, Integer.toString(fragmentMinRooms));
			MazeVariables.set(
				FRAGMENT_TARGET_ROOMS_VAR,
				Integer.toString(fragmentTargetRooms));
			MazeVariables.set(
				FRAGMENT_MAX_ATTEMPTS_VAR,
				Integer.toString(fragmentMaxAttempts));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void clearAll()
	{
		MazeVariables.clear(SIZE_VAR);
		MazeVariables.clear(GENERATOR_VAR);
		MazeVariables.clear(SEED_VAR);
		MazeVariables.clear(FRAGMENT_USAGE_VAR);
		MazeVariables.clear(FRAGMENT_MIN_ROOMS_VAR);
		MazeVariables.clear(FRAGMENT_TARGET_ROOMS_VAR);
		MazeVariables.clear(FRAGMENT_MAX_ATTEMPTS_VAR);
	}

	/*-------------------------------------------------------------------------*/
	public static String previewGeneratorId()
	{
		String id = MazeVariables.get(GENERATOR_VAR);
		if (id == null || id.isEmpty())
		{
			return null;
		}
		return id.trim();
	}

	/*-------------------------------------------------------------------------*/
	public static Integer previewSeed()
	{
		String raw = MazeVariables.get(SEED_VAR);
		if (raw == null || raw.isEmpty())
		{
			return null;
		}
		try
		{
			long value = Long.parseLong(raw.trim());
			return (int)(value & 0x7fffffff);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static FragmentDungeonGen.Options fragmentOptions(String fallbackUsage)
	{
		String usage = MazeVariables.get(FRAGMENT_USAGE_VAR);
		if (usage == null || usage.isEmpty())
		{
			usage = fallbackUsage;
		}
		return new FragmentDungeonGen.Options(
			usage,
			intOrDefault(FRAGMENT_MIN_ROOMS_VAR, 3),
			intOrDefault(FRAGMENT_TARGET_ROOMS_VAR, 3),
			intOrDefault(FRAGMENT_MAX_ATTEMPTS_VAR, 8),
			1);
	}

	/*-------------------------------------------------------------------------*/
	private static int intOrDefault(String key, int defaultValue)
	{
		String raw = MazeVariables.get(key);
		if (raw == null || raw.isEmpty())
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(raw.trim());
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
}

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

import java.util.Random;
import mclachlan.maze.game.MazeVariables;

/**
 * Temple-campaign seed helpers. Run seed is chosen once per new game; floor
 * seeds are derived so layouts are reproducible for saves and balance runs.
 * Mutation keys are per-depth maze variables for cleared encounters / loot.
 */
public final class TempleSeeds
{
	public static final String RUN_SEED = "temple.run.seed";
	public static final String DEPTH = "temple.depth";
	public static final String FLOOR_SEED_PREFIX = "temple.floor.seed.";
	public static final String MUTATIONS_PREFIX = "temple.d.";
	public static final String PORTAL_UP_SUFFIX = ".portal.up";
	public static final String PORTAL_DOWN_SUFFIX = ".portal.down";
	public static final String TRANSITION_SOURCE_DEPTH = "temple.transition.sourceDepth";

	private TempleSeeds()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static long ensureRunSeed()
	{
		String existing = MazeVariables.get(RUN_SEED);
		if (existing != null && !existing.isEmpty())
		{
			return Long.parseLong(existing);
		}

		long seed = new Random().nextLong();
		// avoid Long.MIN_VALUE edge cases in abs/hash
		if (seed == Long.MIN_VALUE)
		{
			seed = 1L;
		}
		MazeVariables.set(RUN_SEED, Long.toString(seed));
		return seed;
	}

	/*-------------------------------------------------------------------------*/
	public static int getDepth()
	{
		String d = MazeVariables.get(DEPTH);
		if (d == null || d.isEmpty())
		{
			return 0;
		}
		return Integer.parseInt(d);
	}

	/*-------------------------------------------------------------------------*/
	public static void setDepth(int depth)
	{
		MazeVariables.set(DEPTH, Integer.toString(depth));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Deterministic floor seed from the run seed and depth. Stored in maze
	 * variables the first time a depth is generated so saves keep the layout.
	 */
	public static int floorSeed(int depth)
	{
		String key = FLOOR_SEED_PREFIX + depth;
		String existing = MazeVariables.get(key);
		if (existing != null && !existing.isEmpty())
		{
			return Integer.parseInt(existing);
		}

		long run = ensureRunSeed();
		// mix run + depth into a stable 31-bit positive int for Noise4j
		long mixed = run ^ (depth * 0x9E3779B97F4A7C15L);
		mixed ^= (mixed >>> 33);
		mixed *= 0xff51afd7ed558ccdL;
		mixed ^= (mixed >>> 33);
		int seed = (int)(mixed & 0x7fffffff);
		MazeVariables.set(key, Integer.toString(seed));
		return seed;
	}

	/*-------------------------------------------------------------------------*/
	/** Cleared-encounter mutation key (stable across regen of the same depth). */
	public static String encounterVar(int depth, int index)
	{
		return MUTATIONS_PREFIX + depth + ".enc." + index;
	}

	/*-------------------------------------------------------------------------*/
	/** Once-only loot mutation key. */
	public static String lootVar(int depth, int index)
	{
		return MUTATIONS_PREFIX + depth + ".loot." + index;
	}

	/** Persisted foe-entry subset for this depth ({@link TempleFoeRoster}). */
	public static String rosterVar(int depth)
	{
		return TempleSeededPicks.pickVar(depth, TempleFoeRoster.ROSTER_PURPOSE);
	}

	/** Index of the quiet entry room ({@link mclachlan.dungeongen.DungeonGenResult#startingRoomIndex()}). */
	public static String startRoomVar(int depth)
	{
		return MUTATIONS_PREFIX + depth + ".startRoom";
	}

	/** Fragment encounter / once-only mutation key. */
	public static String fragmentVar(int depth, String zoneName, int index)
	{
		return MUTATIONS_PREFIX + depth + ".frag." + zoneName + "." + index;
	}

	/** Persisted stair portal encoding for a depth ({@link mclachlan.dungeongen.StairPortalSpec#encode}). */
	public static String portalVar(int depth, boolean up)
	{
		return MUTATIONS_PREFIX + depth + (up ? PORTAL_UP_SUFFIX : PORTAL_DOWN_SUFFIX);
	}

	/** Set after the player has seen first-visit atmosphere flavour for a depth. */
	public static String visitedVar(int depth)
	{
		return MUTATIONS_PREFIX + depth + ".visited";
	}

	/*-------------------------------------------------------------------------*/
	/** Stub namespace for future per-floor mutation blobs. */
	public static String mutationsKey(int depth)
	{
		return MUTATIONS_PREFIX + depth + ".blob";
	}
}

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

import java.util.*;
import java.util.function.Function;
import mclachlan.maze.game.MazeVariables;

/**
 * Persist-once seeded picks for temple floor dressing. Rolls are derived from
 * the stored floor seed and a purpose string; chosen names are written to maze
 * variables so regen, hub trips, and save/load keep the same choices.
 * <p>
 * Never mutates Database-cached collections — always shuffle a copy.
 */
public final class TempleSeededPicks
{
	private static final String PICK_PREFIX = ".pick.";

	private TempleSeededPicks()
	{
	}

	/*-------------------------------------------------------------------------*/
	/** Independent RNG stream for a depth and purpose (never Noise4j {@code Generators}). */
	public static Random rng(int depth, String purpose)
	{
		long seed = TempleSeeds.floorSeed(depth);
		long mixed = seed ^ (long)purpose.hashCode() * 0x9E3779B97F4A7C15L;
		mixed ^= mixed >>> 33;
		mixed *= 0xff51afd7ed558ccdL;
		mixed ^= mixed >>> 33;
		return new Random(mixed);
	}

	/*-------------------------------------------------------------------------*/
	public static String pickVar(int depth, String purpose)
	{
		return TempleSeeds.MUTATIONS_PREFIX + depth + PICK_PREFIX + purpose;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns {@code n} items from {@code source}, remembering comma-separated
	 * {@link #nameOf} values in a maze variable on first use.
	 */
	public static <T> List<T> pickAndRemember(
		int depth,
		String purpose,
		List<T> source,
		int n,
		Function<T, String> nameOf,
		Function<String, T> resolve)
	{
		if (source == null || source.isEmpty() || n <= 0)
		{
			return List.of();
		}

		String var = pickVar(depth, purpose);
		String existing = MazeVariables.get(var);
		if (existing != null && !existing.isEmpty())
		{
			return restore(existing, resolve);
		}

		List<T> copy = new ArrayList<>(source);
		Random random = rng(depth, purpose);
		Collections.shuffle(copy, random);
		int take = Math.min(n, copy.size());
		List<T> picked = new ArrayList<>(copy.subList(0, take));
		MazeVariables.set(var, encode(picked, nameOf));
		return picked;
	}

	/*-------------------------------------------------------------------------*/
	/** Pick one element from {@code candidates}, remembering its encoded key. */
	public static <T> T pickOneAndRemember(
		int depth,
		String purpose,
		List<T> candidates,
		Function<T, String> nameOf,
		Function<String, T> resolve)
	{
		List<T> picked = pickAndRemember(depth, purpose, candidates, 1, nameOf, resolve);
		return picked.isEmpty() ? null : picked.get(0);
	}

	/*-------------------------------------------------------------------------*/
	private static <T> String encode(List<T> items, Function<T, String> nameOf)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++)
		{
			if (i > 0)
			{
				sb.append(',');
			}
			sb.append(nameOf.apply(items.get(i)));
		}
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	private static <T> List<T> restore(String encoded, Function<String, T> resolve)
	{
		String[] parts = encoded.split(",", -1);
		List<T> result = new ArrayList<>(parts.length);
		for (String part : parts)
		{
			if (part.isEmpty())
			{
				continue;
			}
			T item = resolve.apply(part);
			if (item != null)
			{
				result.add(item);
			}
		}
		return result;
	}
}

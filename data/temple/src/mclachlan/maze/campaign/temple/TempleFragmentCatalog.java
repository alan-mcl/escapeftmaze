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
import mclachlan.maze.data.Database;

/**
 * Selects stamp templates from zone metadata ({@code fragment.*} keys).
 */
public final class TempleFragmentCatalog
{
	public static final String PREFIX = "fragment.";
	public static final String KEY_FRAGMENT = "fragment";
	public static final String KEY_ROLE = "fragment.role";
	public static final String KEY_DEPTH_MIN = "fragment.depthMin";
	public static final String KEY_DEPTH_MAX = "fragment.depthMax";
	public static final String KEY_WEIGHT = "fragment.weight";
	public static final String KEY_MAX_PER_FLOOR = "fragment.maxPerFloor";

	public record Entry(
		String zoneName,
		String role,
		int depthMin,
		int depthMax,
		int weight,
		int maxPerFloor)
	{
	}

	private TempleFragmentCatalog()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static List<Entry> eligibleForDepth(int depth)
	{
		Map<String, Map<String, String>> byPrefix =
			Database.getInstance().peekZoneMetadataByPrefix(PREFIX);

		List<Entry> result = new ArrayList<>();
		for (Map.Entry<String, Map<String, String>> e : byPrefix.entrySet())
		{
			Entry entry = fromMetadata(e.getKey(), e.getValue());
			if (entry != null && depth >= entry.depthMin() && depth <= entry.depthMax())
			{
				result.add(entry);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	static Entry fromMetadata(String zoneName, Map<String, String> metadata)
	{
		if (metadata == null || metadata.isEmpty())
		{
			return null;
		}

		String flagged = metadata.get(KEY_FRAGMENT);
		String role = metadata.get(KEY_ROLE);
		if (!"true".equalsIgnoreCase(flagged) && (role == null || role.isEmpty()))
		{
			return null;
		}

		return new Entry(
			zoneName,
			role != null ? role : "flavour",
			parseInt(metadata.get(KEY_DEPTH_MIN), 1),
			parseInt(metadata.get(KEY_DEPTH_MAX), 99),
			Math.max(1, parseInt(metadata.get(KEY_WEIGHT), 1)),
			Math.max(1, parseInt(metadata.get(KEY_MAX_PER_FLOOR), 1)));
	}

	/*-------------------------------------------------------------------------*/
	public static List<Entry> pickForFloor(int depth, int floorSeed, int floorWideCap)
	{
		List<Entry> eligible = eligibleForDepth(depth);
		if (eligible.isEmpty())
		{
			return List.of();
		}

		Random rng = new Random(floorSeed ^ 0x7a4f7261L);
		List<Entry> result = new ArrayList<>();
		Map<String, Integer> usedPerZone = new HashMap<>();

		while (result.size() < floorWideCap && !eligible.isEmpty())
		{
			Entry picked = weightedPick(eligible, rng);
			if (picked == null)
			{
				break;
			}

			int used = usedPerZone.getOrDefault(picked.zoneName(), 0);
			if (used >= picked.maxPerFloor())
			{
				eligible.remove(picked);
				continue;
			}

			result.add(picked);
			usedPerZone.put(picked.zoneName(), used + 1);

			if (used + 1 >= picked.maxPerFloor())
			{
				eligible.remove(picked);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static Entry weightedPick(List<Entry> entries, Random rng)
	{
		int total = 0;
		for (Entry e : entries)
		{
			total += e.weight();
		}
		if (total <= 0)
		{
			return null;
		}

		int roll = rng.nextInt(total);
		for (Entry e : entries)
		{
			roll -= e.weight();
			if (roll < 0)
			{
				return e;
			}
		}
		return entries.get(entries.size() - 1);
	}

	/*-------------------------------------------------------------------------*/
	private static int parseInt(String value, int defaultValue)
	{
		if (value == null || value.isEmpty())
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
}

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

package mclachlan.dungeongen.fragment;

import java.util.*;
import mclachlan.maze.data.Database;

/**
 * Selects stamp templates from zone metadata ({@code fragment.*} keys).
 */
public final class FragmentCatalog
{
	public static final String PREFIX = "fragment.";
	public static final String KEY_FRAGMENT = "fragment";
	public static final String KEY_ROLE = "fragment.role";
	public static final String KEY_DEPTH_MIN = "fragment.depthMin";
	public static final String KEY_DEPTH_MAX = "fragment.depthMax";
	public static final String KEY_WEIGHT = "fragment.weight";
	public static final String KEY_MAX_PER_FLOOR = "fragment.maxPerFloor";
	public static final String KEY_USAGE = "fragment.usage";
	public static final String KEY_KIND = "fragment.kind";
	public static final String KEY_START = "fragment.start";
	public static final String KEY_ROTATE = "fragment.rotate";

	public enum Kind
	{
		ROOM("room"),
		CORRIDOR("corridor");

		private final String id;

		Kind(String id)
		{
			this.id = id;
		}

		public String id()
		{
			return id;
		}

		static Kind fromId(String id)
		{
			if (id == null || id.isEmpty())
			{
				return null;
			}
			for (Kind kind : values())
			{
				if (kind.id.equalsIgnoreCase(id))
				{
					return kind;
				}
			}
			return null;
		}
	}

	public record Entry(
		String zoneName,
		String sourceZoneName,
		int quarterTurns,
		boolean rotatable,
		String role,
		String usage,
		Kind kind,
		boolean start,
		int depthMin,
		int depthMax,
		int weight,
		int maxPerFloor)
	{
		public Entry(
			String zoneName,
			String role,
			String usage,
			Kind kind,
			boolean start,
			int depthMin,
			int depthMax,
			int weight,
			int maxPerFloor)
		{
			this(
				zoneName,
				zoneName,
				0,
				true,
				role,
				usage,
				kind,
				start,
				depthMin,
				depthMax,
				weight,
				maxPerFloor);
		}

		public boolean isAssemblyFragment()
		{
			return usage != null && !usage.isEmpty() && kind != null;
		}

		public Entry withQuarterTurns(int turns)
		{
			int q = ((turns % 4) + 4) % 4;
			String suffix = switch (q)
			{
				case 0 -> "";
				case 1 -> "#r90";
				case 2 -> "#r180";
				case 3 -> "#r270";
				default -> throw new IllegalStateException("turns " + q);
			};
			return new Entry(
				sourceZoneName + suffix,
				sourceZoneName,
				q,
				rotatable,
				role,
				usage,
				kind,
				start && q == 0,
				depthMin,
				depthMax,
				weight,
				maxPerFloor);
		}
	}

	private FragmentCatalog()
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

		boolean rotatable = !"false".equalsIgnoreCase(metadata.get(KEY_ROTATE));

		return new Entry(
			zoneName,
			zoneName,
			0,
			rotatable,
			role != null ? role : "flavour",
			metadata.get(KEY_USAGE),
			Kind.fromId(metadata.get(KEY_KIND)),
			"true".equalsIgnoreCase(metadata.get(KEY_START)),
			parseInt(metadata.get(KEY_DEPTH_MIN), 1),
			parseInt(metadata.get(KEY_DEPTH_MAX), 99),
			Math.max(1, parseInt(metadata.get(KEY_WEIGHT), 1)),
			Math.max(1, parseInt(metadata.get(KEY_MAX_PER_FLOOR), 1)));
	}

	/*-------------------------------------------------------------------------*/
	/** Assembly fragments for a layout theme ({@code fragment.usage} + {@code fragment.kind}). */
	public static List<Entry> eligibleForAssembly(int depth, String usage)
	{
		Map<String, Map<String, String>> byPrefix =
			Database.getInstance().peekZoneMetadataByPrefix(PREFIX);

		List<Entry> result = new ArrayList<>();
		for (Map.Entry<String, Map<String, String>> e : byPrefix.entrySet())
		{
			Entry entry = fromMetadata(e.getKey(), e.getValue());
			if (entry != null
				&& entry.isAssemblyFragment()
				&& usage.equals(entry.usage())
				&& depth >= entry.depthMin()
				&& depth <= entry.depthMax())
			{
				result.add(entry);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/** Distinct {@code fragment.usage} ids among assembly fragments in the catalog. */
	public static List<String> usageIds()
	{
		Map<String, Map<String, String>> byPrefix =
			Database.getInstance().peekZoneMetadataByPrefix(PREFIX);

		Set<String> usages = new TreeSet<>();
		for (Map.Entry<String, Map<String, String>> e : byPrefix.entrySet())
		{
			Entry entry = fromMetadata(e.getKey(), e.getValue());
			if (entry != null && entry.isAssemblyFragment())
			{
				usages.add(entry.usage());
			}
		}
		return List.copyOf(usages);
	}

	/*-------------------------------------------------------------------------*/
	/** Expands authored entries into quarter-turn variants for assembly. */
	public static List<Entry> expandRotations(List<Entry> entries)
	{
		List<Entry> result = new ArrayList<>();
		for (Entry entry : entries)
		{
			if (!entry.isAssemblyFragment())
			{
				result.add(entry);
				continue;
			}
			int variants = entry.rotatable() ? 4 : 1;
			for (int q = 0; q < variants; q++)
			{
				result.add(entry.withQuarterTurns(q));
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<Entry> filterByKind(List<Entry> entries, Kind kind)
	{
		List<Entry> result = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e.kind() == kind)
			{
				result.add(e);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<Entry> filterStartRooms(List<Entry> entries)
	{
		List<Entry> result = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e.kind() == Kind.ROOM && e.start())
			{
				result.add(e);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<Entry> pickForFloor(int depth, int floorSeed, int floorWideCap)
	{
		List<Entry> eligible = new ArrayList<>();
		for (Entry entry : eligibleForDepth(depth))
		{
			if (!entry.isAssemblyFragment())
			{
				eligible.add(entry);
			}
		}
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

			int used = usedPerZone.getOrDefault(picked.sourceZoneName(), 0);
			if (used >= picked.maxPerFloor())
			{
				eligible.remove(picked);
				continue;
			}

			result.add(picked);
			usedPerZone.put(picked.sourceZoneName(), used + 1);

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

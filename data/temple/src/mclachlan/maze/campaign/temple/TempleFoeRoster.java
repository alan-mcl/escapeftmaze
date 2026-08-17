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
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.stat.PercentageTable;

/**
 * Per-depth foe subset for a temple run. Clones the authored band table, picks
 * a persist-once subset via {@link TempleSeededPicks}, and returns a runtime
 * encounter table without mutating Database caches.
 */
public final class TempleFoeRoster
{
	public static final String ROSTER_PURPOSE = "roster";

	private TempleFoeRoster()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Encounter table for this depth's floor encounters (subset of the band pool).
	 */
	public static EncounterTable forFloor(int depth)
	{
		String bandName = TempleDepthScaler.encounterTableName(depth);
		EncounterTable band = Database.getInstance().getEncounterTable(bandName);
		if (band == null)
		{
			band = Database.getInstance().getEncounterTable(TempleDepthScaler.encounterTableName(1));
		}
		if (band == null || band.getEncounterTable() == null)
		{
			throw new mclachlan.maze.util.MazeException("Missing temple encounter table [" + bandName + "]");
		}

		PercentageTable<FoeEntry> pool = band.getEncounterTable();
		List<FoeEntry> source = new ArrayList<>(pool.getItems());
		int n = Math.min(TempleDepthScaler.foeSubsetSize(depth), source.size());

		List<FoeEntry> picked = TempleSeededPicks.pickAndRemember(
			depth,
			ROSTER_PURPOSE,
			source,
			n,
			FoeEntry::getName,
			name -> Database.getInstance().getFoeEntries().get(name));

		return buildRuntimeTable(bandName + ".run", pool, picked);
	}

	/*-------------------------------------------------------------------------*/
	static EncounterTable buildRuntimeTable(
		String runtimeName,
		PercentageTable<FoeEntry> sourcePool,
		List<FoeEntry> entries)
	{
		if (entries.isEmpty())
		{
			return new EncounterTable(runtimeName, new PercentageTable<>(sourcePool.shouldSumTo100()));
		}

		List<Integer> weights = new ArrayList<>();
		int sum = 0;
		for (FoeEntry entry : entries)
		{
			int w = sourcePool.getPercentage(entry);
			weights.add(w);
			sum += w;
		}

		if (sum <= 0)
		{
			int even = 100 / entries.size();
			weights.clear();
			for (int i = 0; i < entries.size(); i++)
			{
				weights.add(even);
			}
			sum = weights.stream().mapToInt(Integer::intValue).sum();
		}

		// Runtime roster tables drive mandatory door encounters; they must always
		// resolve to a foe entry (unlike band tables that may sum below 100%).
		if (sum != 100)
		{
			weights = normalizeTo100(weights, sum);
		}

		PercentageTable<FoeEntry> subset = new PercentageTable<>(
			entries.toArray(new FoeEntry[0]),
			weights.toArray(new Integer[0]),
			true);
		return new EncounterTable(runtimeName, subset);
	}

	/** Rescale subset weights to total 100 for tables that require it. */
	private static List<Integer> normalizeTo100(List<Integer> weights, int sum)
	{
		List<Integer> scaled = new ArrayList<>(weights.size());
		int scaledSum = 0;
		for (int w : weights)
		{
			int pct = (w * 100) / sum;
			scaled.add(pct);
			scaledSum += pct;
		}
		int remainder = 100 - scaledSum;
		if (remainder != 0)
		{
			scaled.set(0, scaled.get(0) + remainder);
		}
		return scaled;
	}
}

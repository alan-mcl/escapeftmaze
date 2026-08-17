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
import mclachlan.maze.stat.combat.CombatStatistics;

/**
 * Aggregated metrics from an automated temple delve run.
 */
public final class TempleRunMetrics
{
	public static final class FloorMetrics
	{
		public int depth;
		public int tileSteps;
		public int combats;
		public int combatRounds;
		public int partyHpLost;
		public int lootBaseCost;
		public int xpGained;
	}

	private long runSeed;
	private int maxDepthReached;
	private boolean wiped;
	private int totalCombats;
	private int totalCombatRounds;
	private int totalPartyHpLost;
	private int totalLootBaseCost;
	private int totalXpGained;
	private int levelsGained;
	private long startTurn;
	private long endTurn;
	private final List<FloorMetrics> floors = new ArrayList<>();

	public long getRunSeed()
	{
		return runSeed;
	}

	public void setRunSeed(long runSeed)
	{
		this.runSeed = runSeed;
	}

	public int getMaxDepthReached()
	{
		return maxDepthReached;
	}

	public void setMaxDepthReached(int maxDepthReached)
	{
		this.maxDepthReached = maxDepthReached;
	}

	public boolean isWiped()
	{
		return wiped;
	}

	public void setWiped(boolean wiped)
	{
		this.wiped = wiped;
	}

	public int getTotalCombats()
	{
		return totalCombats;
	}

	public int getTotalCombatRounds()
	{
		return totalCombatRounds;
	}

	public int getTotalPartyHpLost()
	{
		return totalPartyHpLost;
	}

	public int getTotalLootBaseCost()
	{
		return totalLootBaseCost;
	}

	public int getTotalXpGained()
	{
		return totalXpGained;
	}

	public int getLevelsGained()
	{
		return levelsGained;
	}

	public long getStartTurn()
	{
		return startTurn;
	}

	public void setStartTurn(long startTurn)
	{
		this.startTurn = startTurn;
	}

	public long getEndTurn()
	{
		return endTurn;
	}

	public void setEndTurn(long endTurn)
	{
		this.endTurn = endTurn;
	}

	public List<FloorMetrics> getFloors()
	{
		return Collections.unmodifiableList(floors);
	}

	public void setLevelsGained(int levelsGained)
	{
		this.levelsGained = levelsGained;
	}

	public void addFloor(FloorMetrics floor)
	{
		floors.add(floor);
		totalCombats += floor.combats;
		totalCombatRounds += floor.combatRounds;
		totalPartyHpLost += floor.partyHpLost;
		totalLootBaseCost += floor.lootBaseCost;
		totalXpGained += floor.xpGained;
	}

	public void recordCombatStats(List<CombatStatistics> stats)
	{
		for (CombatStatistics s : stats)
		{
			totalCombats++;
			totalCombatRounds += s.getCombatRounds();
		}
	}

	@Override
	public String toString()
	{
		return "TempleRunMetrics{seed=" + runSeed
			+ ", depth=" + maxDepthReached
			+ ", wiped=" + wiped
			+ ", combats=" + totalCombats
			+ ", rounds=" + totalCombatRounds
			+ ", hpLost=" + totalPartyHpLost
			+ ", loot=" + totalLootBaseCost
			+ ", xp=" + totalXpGained
			+ ", levels=" + levelsGained + "}";
	}
}

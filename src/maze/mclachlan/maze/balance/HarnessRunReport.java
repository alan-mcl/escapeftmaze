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

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;

/**
 * Campaign-agnostic metrics snapshot from a headless dungeon run, for HTML reports.
 */
public final class HarnessRunReport
{
	public String title = "Headless dungeon run";
	public long seed;
	public String outcome = "Incomplete";
	public int maxDepthReached;
	public long startTurn;
	public long endTurn;
	public int totalCombats;
	public int totalCombatRounds;
	public int totalPartyHpLost;
	public int totalLootBaseCost;
	public int totalXpGained;
	public int levelsGained;
	public int gold;
	public int partySize;
	public int partyAlive;
	/** Absolute path to session log file when a {@link mclachlan.maze.game.Log} was installed. */
	public String logPath;
	public final List<PartyMember> party = new ArrayList<>();
	public final List<Floor> floors = new ArrayList<>();

	public static final class PartyMember
	{
		public String name;
		public String gender;
		public String race;
		public String characterClass;
		public int level;
		public int hpCurrent;
		public int hpMax;
		public int experience;
		public boolean alive;
	}

	public static final class Floor
	{
		public int depth;
		public int tileSteps;
		public int combats;
		public int combatRounds;
		public int partyHpLost;
		public int lootBaseCost;
		public int xpGained;
	}

	/*-------------------------------------------------------------------------*/
	public static List<PartyMember> snapshotParty(PlayerParty party)
	{
		List<PartyMember> result = new ArrayList<>();
		if (party == null)
		{
			return result;
		}
		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			PartyMember m = new PartyMember();
			m.name = pc.getName();
			m.gender = pc.getGender() != null ? pc.getGender().getName() : "";
			m.race = pc.getRace() != null ? pc.getRace().getName() : "";
			m.characterClass = pc.getCharacterClass() != null
				? pc.getCharacterClass().getName() : "";
			m.level = pc.getLevel();
			m.hpCurrent = pc.getHitPoints().getCurrent();
			m.hpMax = pc.getHitPoints().getMaximum();
			m.experience = pc.getExperience();
			m.alive = pc.getHitPoints().getCurrent() > 0;
			result.add(m);
		}
		return result;
	}

	public double averageRoundsPerCombat()
	{
		if (totalCombats <= 0)
		{
			return 0;
		}
		return (double)totalCombatRounds / totalCombats;
	}

	public long turnsElapsed()
	{
		return Math.max(0, endTurn - startTurn);
	}
}

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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatStatistics;

/**
 * Drives live combat on the maze event thread until victory, defeat, or cap.
 */
public final class CombatDriver
{
	public static final int DEFAULT_MAX_ROUNDS = 200;

	private CombatDriver()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static CombatStatistics runToCompletion(Maze maze, int maxRounds)
	{
		CombatStatistics stats = new CombatStatistics("harness");
		boolean capturedStart = false;
		int rounds = 0;

		while (maze.getCurrentCombat() != null && rounds < maxRounds)
		{
			Combat combat = maze.getCurrentCombat();
			if (!capturedStart)
			{
				stats.captureCombatStart(maze.getParty(), combat.getFoes());
				stats.captureAmbushStatus(combat.getAmbushStatus());
				capturedStart = true;
			}

			if (HarnessRunProgress.isEnabled())
			{
				HarnessRunProgress.line(
					"    combat round %d/%d starting (queue=%d)",
					rounds + 1,
					maxRounds,
					maze.getEventQueueSizeForTesting());
			}

			maze.executeCombatRound(combat);
			drainEvents(maze);
			rounds++;
			stats.incCombatRounds();

			if (HarnessRunProgress.isEnabled())
			{
				HarnessRunProgress.line(
					"    combat round %d/%d done (queue=%d, combat=%s)",
					rounds,
					maxRounds,
					maze.getEventQueueSizeForTesting(),
					maze.getCurrentCombat() != null ? "active" : "ended");
			}
		}

		if (HarnessRunProgress.isEnabled() && maze.getCurrentCombat() != null)
		{
			HarnessRunProgress.line(
				"    WARN combat hit round cap %d with combat still active (queue=%d)",
				maxRounds,
				maze.getEventQueueSizeForTesting());
		}

		if (maze.getParty() != null && maze.getParty().numAlive() > 0
			&& maze.getCurrentCombat() == null)
		{
			stats.captureCombatEnd(true);
		}

		return stats;
	}

	/*-------------------------------------------------------------------------*/
	public static void drainEvents(Maze maze)
	{
		HeadlessHarnessSupport.ensureNpcManagerStarted();

		int guard = 0;
		while (!maze.isEventQueueEmptyForTesting() && guard++ < 10_000)
		{
			maze.resolveQueuedEventsForTesting();
		}

		if (HarnessRunProgress.isEnabled() && !maze.isEventQueueEmptyForTesting())
		{
			HarnessRunProgress.line(
				"    WARN event queue not drained after %d passes (remaining=%d)",
				guard - 1,
				maze.getEventQueueSizeForTesting());
		}
	}
}

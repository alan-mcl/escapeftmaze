/*
 * Copyright (c) 2011 Alan McLachlan
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

package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.ItemCacheManager;
import mclachlan.maze.stat.TurnCache;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class GameTime
{
	private static long turnNr = 0;

	/*-------------------------------------------------------------------------*/
	/**
	 * To be called at the end of every turn.  All end-of-turn actions here.
	 */
	public static void incTurn()
	{
		//
		// End of turn actions:
		//

		// Update Conditions
		ConditionManager.getInstance().endOfTurn(getTurnNr());

		// Flush turn cache
		TurnCache.getInstance().endOfTurn(getTurnNr());

		// Regenerate Resources
		regenerateResources();

		// Update all NPCs
		NpcManager.getInstance().endOfTurn(getTurnNr());

		// Update item caches
		ItemCacheManager.getInstance().endOfTurn(getTurnNr());
		
		// Update the current zone
		Maze.getInstance().getZone().endOfTurn(getTurnNr());

		// Refresh character options
		Maze.getInstance().refreshCharacterData();

		//
		// Start next turn
		//
		setTurnNr(getTurnNr() + 1);
		Maze.log(Log.MEDIUM, "------[ turn "+ getTurnNr() +" ]------");
	}

	/*-------------------------------------------------------------------------*/
	private static void regenerateResources()
	{
		Maze.log("regenerating actor resources...");
		
		boolean combat = Maze.getInstance().getState() == Maze.State.COMBAT;
		boolean resting = Maze.getInstance().getState() == Maze.State.RESTING;

		// regen player characters
		List<UnifiedActor> party = Maze.getInstance().getParty().getActors();
		int max = party.size();
		for (int i = 0; i < max; i++)
		{
			UnifiedActor pc = party.get(i);
			pc.regenerateResources(getTurnNr(), resting, combat, Maze.getInstance().getCurrentTile());
		}

		// regen foes in combat
		if (combat)
		{
			List<FoeGroup> allFoes = Maze.getInstance().getCurrentCombat().getFoes();

			for (FoeGroup fg : allFoes)
			{
				for (UnifiedActor f : fg.getActors())
				{
					f.regenerateResources(getTurnNr(), resting, combat, Maze.getInstance().getCurrentTile());
				}
			}
		}

		// todo: regen NPC resources?

		Maze.log("finished regenerating actor resources");
	}

	/*-------------------------------------------------------------------------*/
	public static long getTurnNr()
	{
		return turnNr;
	}

	/*-------------------------------------------------------------------------*/
	public static void setTurnNr(long turnNr)
	{
		GameTime.turnNr = turnNr;
	}
}

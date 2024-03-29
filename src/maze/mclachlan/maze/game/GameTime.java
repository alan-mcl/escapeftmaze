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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.ItemCacheManager;
import mclachlan.maze.stat.TurnCache;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class GameTime
{
	public static final int TURNS_PER_DAY = 300;

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
		Maze.getPerfLog().enter("ConditionManager.endOfTurn");
		ConditionManager.getInstance().endOfTurn(getTurnNr());
		Maze.getPerfLog().exit("ConditionManager.endOfTurn");

		// Flush turn cache
		Maze.getPerfLog().enter("TurnCache.endOfTurn");
		TurnCache.getInstance().endOfTurn(getTurnNr());
		Maze.getPerfLog().exit("TurnCache.endOfTurn");

		// Regenerate Resources
		Maze.getPerfLog().enter("GameTime::regenerateResources");
		regenerateResources();
		Maze.getPerfLog().exit("GameTime::regenerateResources");

		// Update all NPCs
		Maze.getPerfLog().enter("NpcManager.endOfTurn");
		NpcManager.getInstance().endOfTurn(getTurnNr());
		Maze.getPerfLog().exit("NpcManager.endOfTurn");

		// Update item caches
		Maze.getPerfLog().enter("ItemCacheManager.endOfTurn");
		ItemCacheManager.getInstance().endOfTurn(getTurnNr());
		Maze.getPerfLog().exit("ItemCacheManager.endOfTurn");
		
		// Update the current zone
		Maze.getPerfLog().enter("Zone.endOfTurn");
		Maze.getInstance().getCurrentZone().endOfTurn(getTurnNr());
		Maze.getPerfLog().exit("Zone.endOfTurn");

		// Refresh character options
		Maze.getPerfLog().enter("GameTime::refreshCharacterData");
		Maze.getInstance().refreshCharacterData();
		Maze.getPerfLog().exit("GameTime::refreshCharacterData");

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

		Combat currentCombat = Maze.getInstance().getCurrentCombat();
		boolean combat = currentCombat != null && Maze.getInstance().getState() == Maze.State.COMBAT;
		boolean resting = Maze.getInstance().getState() == Maze.State.RESTING;

		// regen player characters
		List<UnifiedActor> party = Maze.getInstance().getParty().getActors();
		int max = party.size();
		for (int i = 0; i < max; i++)
		{
			UnifiedActor pc = party.get(i);
//			Maze.getPerfLog().enter("GameTime::pc::regenerateResources");
			pc.regenerateResources(getTurnNr(), resting, combat, Maze.getInstance().getCurrentTile());
//			Maze.getPerfLog().exit("GameTime::pc::regenerateResources");
		}

		// regen foes in combat
		if (combat)
		{
			List<FoeGroup> allFoes = currentCombat.getFoes();

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

	/*-------------------------------------------------------------------------*/
	public static GameDate getGameDate(long turnNr)
	{
		long dn = 1+ turnNr / TURNS_PER_DAY;
		long tn = turnNr % TURNS_PER_DAY;

		return new GameDate(dn, tn);
	}

	/*-------------------------------------------------------------------------*/
	public static GameDate getGameDate()
	{
		return getGameDate(getTurnNr());
	}

	/*-------------------------------------------------------------------------*/
	public static void startGame()
	{
		setTurnNr(0);
	}

	/*-------------------------------------------------------------------------*/
	public static class GameDate
	{
		private long dayNr, turnNr;

		public GameDate(long dayNr, long turnNr)
		{
			this.dayNr = dayNr;
			this.turnNr = turnNr;
		}

		public String toFormattedString()
		{
			return StringUtil.getUiLabel("common.gametime", dayNr, turnNr);
		}
	}
}

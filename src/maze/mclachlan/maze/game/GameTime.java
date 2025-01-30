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
import mclachlan.maze.util.MazeException;

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
	public static List<MazeEvent> incTurn()
	{
		//
		// End of turn actions:
		//

		List<MazeEvent> result = new ArrayList<>();

		// Update Conditions
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "ConditionManager.endOfTurn"));
		result.addAll(ConditionManager.getInstance().endOfTurn(getTurnNr()));
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT, "ConditionManager.endOfTurn"));

		// Flush turn cache
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "TurnCache.endOfTurn"));
		result.addAll(TurnCache.getInstance().endOfTurn(getTurnNr()));
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT,"TurnCache.endOfTurn"));

		// Regenerate Resources
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "GameTime::regenerateResources"));
		result.addAll(regenerateResources());
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT, "GameTime::regenerateResources"));

		// Update all NPCs
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "NpcManager.endOfTurn"));
		result.addAll(NpcManager.getInstance().endOfTurn(getTurnNr()));
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT,"NpcManager.endOfTurn"));

		// Update item caches
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "ItemCacheManager.endOfTurn"));
		result.addAll(ItemCacheManager.getInstance().endOfTurn(getTurnNr()));
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT, "ItemCacheManager.endOfTurn"));
		
		// Update the current zone
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "Zone.endOfTurn"));
		result.addAll(Maze.getInstance().getCurrentZone().endOfTurn(getTurnNr()));
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT, "Zone.endOfTurn"));

		// Refresh character options
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.ENTER, "GameTime::refreshCharacterData"));
		result.addAll(Maze.getInstance().refreshCharacterData());
		result.add(new PerfLogEvent(PerfLogEvent.PerfEvent.EXIT, "GameTime::refreshCharacterData"));

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				//
				// Start next turn
				//
				setTurnNr(getTurnNr() + 1);
				return null;
			}
		});
		result.add(new LogEvent(Log.MEDIUM, "------[ turn "+ getTurnNr() +" ]------"));
		return result;
	}

	private static class PerfLogEvent extends MazeEvent
	{
		String tag;
		PerfEvent event;

		enum PerfEvent {ENTER, EXIT};

		public PerfLogEvent(PerfEvent event, String tag)
		{
			this.tag = tag;
			this.event = event;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			switch (event)
			{
				case ENTER -> Maze.getPerfLog().enter(tag);
				case EXIT -> Maze.getPerfLog().exit(tag);
				default -> throw new MazeException("invalid "+event);
			}
			return null;
		}
	}

	private static class LogEvent extends MazeEvent
	{
		String msg;
		int level;

		public LogEvent(int level, String msg)
		{
			this.msg = msg;
			this.level = level;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			Maze.log(level, msg);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<MazeEvent> regenerateResources()
	{
		Maze.log("regenerating actor resources...");

		List<MazeEvent> result = new ArrayList<>();

		Combat currentCombat = Maze.getInstance().getCurrentCombat();
		boolean combat = currentCombat != null && Maze.getInstance().getState() == Maze.State.COMBAT;
		boolean resting = Maze.getInstance().getState() == Maze.State.RESTING;

		// regen player characters
		List<UnifiedActor> party = Maze.getInstance().getParty().getActors();
		int max = party.size();
		for (int i = 0; i < max; i++)
		{
			UnifiedActor pc = party.get(i);
			result.add(new EndOfTurnRegen(pc, resting, combat));
		}

		// regen foes in combat
		if (combat)
		{
			List<FoeGroup> allFoes = currentCombat.getFoes();

			for (FoeGroup fg : allFoes)
			{
				for (UnifiedActor f : fg.getActors())
				{
					result.add(new EndOfTurnRegen(f, resting, combat));
				}
			}
		}

		// todo: regen NPC resources?

		Maze.log("finished regenerating actor resources");

		return result;
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
		private final long dayNr, turnNr;

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

	/*-------------------------------------------------------------------------*/
	private static class EndOfTurnRegen extends MazeEvent
	{
		private final UnifiedActor actor;
		private final boolean resting, combat;

		public EndOfTurnRegen(UnifiedActor actor, boolean resting, boolean combat)
		{
			this.actor = actor;
			this.resting = resting;
			this.combat = combat;
		}

		@Override
		public List<MazeEvent> resolve()
		{
//			Maze.getPerfLog().enter("GameTime::pc::regenerateResources");
			actor.regenerateResources(getTurnNr(), resting, combat, Maze.getInstance().getCurrentTile());
//			Maze.getPerfLog().exit("GameTime::pc::regenerateResources");
			return null;
		}
	}
}

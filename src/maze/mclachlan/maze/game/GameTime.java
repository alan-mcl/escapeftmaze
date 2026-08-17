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
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.TurnCache;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.map.Zone;

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
		Zone zone = Maze.getInstance().getCurrentZone();
		if (zone != null)
		{
			result.addAll(zone.endOfTurn(getTurnNr()));
		}
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
		PlayerParty playerParty = Maze.getInstance().getParty();
		if (playerParty != null)
		{
			List<UnifiedActor> party = playerParty.getActors();
			int max = party.size();
			for (int i = 0; i < max; i++)
			{
				UnifiedActor pc = party.get(i);
				result.add(new EndOfTurnRegen(pc, resting, combat));
			}
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
	public static long getDayNr(long turnNr)
	{
		return 1 + turnNr / TURNS_PER_DAY;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Turn index within the current day, 0 .. {@link #TURNS_PER_DAY}-1.
	 */
	public static long getTurnOfDay(long turnNr)
	{
		return turnNr % TURNS_PER_DAY;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Position within the day as a fraction 0..1 (turn 0 = start of day).
	 */
	public static double getDayFraction(long turnNr)
	{
		return getTurnOfDay(turnNr) / (double)TURNS_PER_DAY;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Smooth night weight: 0 at noon, 1 at midnight (turn 0 / end of day).
	 */
	public static double getNightAmount(long turnNr)
	{
		double radians = 2.0 * Math.PI * getTurnOfDay(turnNr) / TURNS_PER_DAY;
		return (1.0 + Math.cos(radians)) / 2.0;
	}

	/*-------------------------------------------------------------------------*/
	public static TimeOfDay getTimeOfDay(long turnNr)
	{
		return TimeOfDay.fromTurnOfDay(getTurnOfDay(turnNr));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Smallest {@code newTurnNr >= turnNr} whose turn-of-day equals
	 * {@code targetTurnOfDay}. Never decreases {@code turnNr}.
	 */
	public static long computeForwardAdvanceToTurnOfDay(long turnNr, int targetTurnOfDay)
	{
		long tod = getTurnOfDay(turnNr);
		if (tod == targetTurnOfDay)
		{
			return turnNr;
		}

		long delta;
		if (tod < targetTurnOfDay)
		{
			delta = targetTurnOfDay - tod;
		}
		else
		{
			delta = TURNS_PER_DAY - tod + targetTurnOfDay;
		}

		return turnNr + delta;
	}

	/*-------------------------------------------------------------------------*/
	public static GameDate getGameDate(long turnNr)
	{
		return new GameDate(getDayNr(turnNr), getTurnOfDay(turnNr));
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
		private final long dayNr, turnOfDay;

		public GameDate(long dayNr, long turnOfDay)
		{
			this.dayNr = dayNr;
			this.turnOfDay = turnOfDay;
		}

		public long getDayNr()
		{
			return dayNr;
		}

		public long getTurnOfDay()
		{
			return turnOfDay;
		}

		public TimeOfDay getTimeOfDay()
		{
			return TimeOfDay.fromTurnOfDay(turnOfDay);
		}

		public String toFormattedString()
		{
			return StringUtil.getUiLabel("common.gametime", dayNr, turnOfDay);
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

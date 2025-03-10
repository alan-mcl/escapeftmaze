/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 * A class for managing caches that are cleared after each turn.
 */
public class TurnCache implements GameCache
{
	// singleton members
	private static final TurnCache instance = new TurnCache();
	private final Object mutex = new Object();

	// a cache of modifiers that each character practised each round
	private Map<UnifiedActor, StatModifier> practise = new HashMap<>();

	/*-------------------------------------------------------------------------*/
	public static TurnCache getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Loads item caches state.
	 */
	public void loadGame(String name, Loader loader,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		// practise state is not saved
		this.practise = new HashMap<>();
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		// practise state is not saved
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		synchronized (mutex)
		{
			Maze.log("processing turn cache...");

			// assess turn practise
			Maze.log(Log.DEBUG, "Assessing turn practise");

			List<MazeEvent> result = new ArrayList<>();

			for (UnifiedActor actor : practise.keySet())
			{
				result.add(new EndOfTurnPractise(actor, new HashMap<>(practise)));
			}

			result.add(new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					// flush the cache
					practise.clear();
					return null;
				}
			});

			Maze.log("finished processing turn cache");
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void practice(UnifiedActor actor, Stats.Modifier modifier, int amount)
	{
		synchronized (mutex)
		{
			if (!(actor instanceof PlayerCharacter))
			{
				// only the PCs need practice
				return;
			}

			PlayerCharacter pc = (PlayerCharacter)actor;

			if (!pc.isActiveModifier(modifier))
			{
				// can't practice an inactive modifier
				return;
			}

			StatModifier sm = practise.get(pc);

			if (sm == null)
			{
				sm = new StatModifier();
				practise.put(pc, sm);
			}

			sm.incModifier(modifier, amount);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class EndOfTurnPractise extends MazeEvent
	{
		private final UnifiedActor actor;
		private final Map<UnifiedActor, StatModifier> practiseInternal;

		public EndOfTurnPractise(UnifiedActor actor, Map<UnifiedActor, StatModifier> practiseInternal)
		{
			this.actor = actor;
			this.practiseInternal = practiseInternal;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			StatModifier sm = practiseInternal.get(actor);

			if (sm == null)
			{
				System.out.println(actor);
			}

			for (Stats.Modifier modifier : sm.getModifiers().keySet())
			{
				if (sm.getModifier(modifier) > 0)
				{
					GameSys.getInstance().practiseAtEndOfTurn(actor, modifier, 1);
				}
			}

			return null;
		}
	}
}

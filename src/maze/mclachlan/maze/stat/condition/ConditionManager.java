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

package mclachlan.maze.stat.condition;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.GameCache;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ConditionManager implements GameCache
{
	/**
	 * Map of conditions by condition bearer
	 */
	private Map<ConditionBearer, List<Condition>> conditions = new HashMap<>();
	private static final ConditionManager instance = new ConditionManager();
	private final Object mutex = new Object();
	private static final List<Condition> emptyList = Collections.unmodifiableList(new ArrayList<>());

	/*-------------------------------------------------------------------------*/
	public static ConditionManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public void loadGame(String name, Loader loader,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		this.conditions = loader.loadConditions(name, playerCharacterCache);
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.saveConditions(saveGameName, conditions);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return updateConditions(turnNr);
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> updateConditions(long turnNr)
	{
		synchronized (mutex)
		{
			Maze.log("Updating conditions...");

			ArrayList<MazeEvent> result = new ArrayList<>();
			for (ConditionBearer bearer : conditions.keySet())
			{
				result.add(new EndOfTurnConditions(bearer, turnNr));
			}

			Maze.log("Finished updating conditions");

			return result;
		}

	}

	/*-------------------------------------------------------------------------*/
	public void addCondition(ConditionBearer bearer, Condition c)
	{
		c.setTarget(bearer);
		synchronized (mutex)
		{
			List<Condition> list = conditions.computeIfAbsent(bearer, k -> new ArrayList<>());
			list.add(c);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeCondition(ConditionBearer bearer, Condition c)
	{
		synchronized (mutex)
		{
			List<Condition> list = conditions.get(bearer);
			if (list == null)
			{
				// todo: rather do nothing?
				throw new MazeException("invalid attempt to remove condition");
			}
			list.remove(c);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Condition> getConditions(ConditionBearer bearer)
	{
		synchronized(mutex)
		{
			return conditions.getOrDefault(bearer, emptyList);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Removes all conditions from the given bearer
	 */
	public void removeConditions(ConditionBearer bearer)
	{
		synchronized(mutex)
		{
			conditions.remove(bearer);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Directly sets the conditions for a bearer. Replaces all existing.
	 */
	public void setConditions(ConditionBearer bearer, List<Condition> conditions)
	{
		synchronized (mutex)
		{
			this.conditions.put(bearer, conditions);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class EndOfTurnConditions extends MazeEvent
	{
		private final ConditionBearer bearer;
		private final long turnNr;

		public EndOfTurnConditions(ConditionBearer bearer, long turnNr)
		{
			this.bearer = bearer;
			this.turnNr = turnNr;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			ArrayList<MazeEvent> result = new ArrayList<>();

			ListIterator<Condition> li = conditions.get(bearer).listIterator();
			while (li.hasNext())
			{
				Condition c = li.next();
				Maze.log("condition "+c.getName()+" on "+c.getTarget().getName());

				// Any end of turn condition effects:
				List<MazeEvent> list = c.endOfTurn(turnNr);
				if (list != null)
				{
					result.addAll(list);
				}

				// check for SHED_BLIGHTS
				int shedBlights = bearer.getModifier(Stats.Modifier.SHED_BLIGHTS);
				Maze.log("shedBlights = [" + shedBlights + "]");
				if (shedBlights > 0)
				{
					if (c.isAffliction() && Dice.d100.roll("shed blights") <= shedBlights)
					{
						c.setDuration(-1);
						c.setStrength(-1);
					}
				}

				// Expire conditions
				if (c.getDuration() < 0)
				{
					li.remove();
					c.expire();
					ConditionTemplate template = c.getTemplate();
					if (template != null)
					{
						String exitSpellEffect = template.getExitSpellEffect();
						if (exitSpellEffect != null)
						{
							SpellEffect spellEffect = Database.getInstance().getSpellEffect(exitSpellEffect);
							result.addAll(
								spellEffect.getUnsavedResult().apply(
									c.getSource(),
									c.getTarget(),
									c.getCastingLevel(),
									spellEffect,
									null));
						}
					}
				}
			}

			return result;
		}
	}
}

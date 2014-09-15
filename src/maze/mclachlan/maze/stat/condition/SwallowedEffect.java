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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.CombatAction;

/**
 *
 */
public class SwallowedEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public SwallowedEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SwallowedEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		// can't do nothing
		return CombatAction.DO_NOTHING;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isImmobile(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isRemovedByRevitalise(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean askForCombatIntentions(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isAware(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPresent(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean canBeAttacked(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public String getImmunityModifier()
	{
		return Stats.Modifiers.IMMUNE_TO_SWALLOW;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(Condition condition, long turnNr)
	{
		if (condition.getSource().getHitPoints().getCurrent() <= 0)
		{
			// swallower is dead, expire this condition
			ConditionBearer target = condition.getTarget();
			condition.setDuration(-1);

			List<MazeEvent> result = new ArrayList<MazeEvent>();
			result.add(new FlavourTextEvent(
				target.getName()+" emerges!",
				Maze.getInstance().getUserConfig().getCombatDelay(),
				true));
			return result;
		}

		return null;
	}
}

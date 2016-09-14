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

import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.AttackAlliesAction;
import mclachlan.maze.stat.combat.CombatAction;


/**
 *
 */
public class PossessionEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public PossessionEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public PossessionEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		// always attack allies
		if (!GameSys.getInstance().isActorImmobile(actor))
		{
			return new AttackAlliesAction(actor.getAttackWithOptions().get(0));
		}
		else
		{
			return action;
		}
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
	public Stats.Modifier getImmunityModifier()
	{
		return Stats.Modifier.IMMUNE_TO_POSSESSION;
	}
}

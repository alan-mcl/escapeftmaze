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

import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.event.DamageEvent;

/**
 *
 */
public class SleepEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public SleepEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SleepEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		return CombatAction.DO_NOTHING;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isImmobile(UnifiedActor actor, Condition condition)
	{
		return true;
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
	public Stats.Modifier getImmunityModifier()
	{
		return Stats.Modifier.IMMUNE_TO_SLEEP;
	}

	/*-------------------------------------------------------------------------*/
	public int damageToTarget(UnifiedActor actor, Condition condition, int damage, DamageEvent event)
	{
		// 10% per damage point, max 99%
		int percent = Math.min(damage*10, 99);

		if (Dice.d100.roll() <= percent)
		{
			actor.removeCondition(condition);
		}

		return damage;
	}
}

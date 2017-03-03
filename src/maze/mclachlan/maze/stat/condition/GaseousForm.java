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

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.DefendAction;

/**
 *
 */
public class GaseousForm extends ConditionEffect
{
	private static StatModifier stats;

	/*-------------------------------------------------------------------------*/
	static
	{
		stats = new StatModifier();
		stats.setModifier(Stats.Modifier.DEFENCE, 20);
		stats.setModifier(Stats.Modifier.SNEAKING, 5);
		stats.setModifier(Stats.Modifier.TO_RUN_AWAY, 5);
		stats.setModifier(Stats.Modifier.RESIST_BLUDGEONING, 100);
		stats.setModifier(Stats.Modifier.RESIST_PIERCING, 100);
		stats.setModifier(Stats.Modifier.RESIST_SLASHING, 100);
		stats.setModifier(Stats.Modifier.RESIST_AIR, -25);
	}

	/*-------------------------------------------------------------------------*/
	public GaseousForm()
	{
	}

	/*-------------------------------------------------------------------------*/
	public GaseousForm(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		if (action instanceof AttackAction)
		{
			// can't attack melee or ranged
			action = new DefendAction();
		}

		return action;
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(Stats.Modifier modifier, Condition condition, ConditionBearer bearer)
	{
		return stats.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isRemovedByRevitalise(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean canBeAttacked(UnifiedActor actor, Condition condition)
	{
		return true;
	}
}
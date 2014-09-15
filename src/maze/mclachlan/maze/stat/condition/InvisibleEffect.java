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
import mclachlan.maze.stat.combat.SpellAction;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class InvisibleEffect extends ConditionEffect
{
	static StatModifier invisible;

	/*-------------------------------------------------------------------------*/
	static
	{
		invisible = new StatModifier();
		invisible.setModifier(Stats.Modifiers.SNEAKING, 20);
		invisible.setModifier(Stats.Modifiers.TO_RUN_AWAY, 10);
	}

	/*-------------------------------------------------------------------------*/
	public InvisibleEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public InvisibleEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		if (action instanceof AttackAction)
		{
			actor.removeCondition(condition);
		}

		if (action instanceof SpellAction)
		{
			SpellAction sa = (SpellAction)action;
			Spell s = sa.getSpell();
			if (s.getTargetType() != MagicSys.SpellTargetType.CASTER)
			{
				actor.removeCondition(condition);
			}
		}

		return action;
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(String modifier, Condition condition, ConditionBearer bearer)
	{
		return invisible.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isRemovedByRevitalise(UnifiedActor actor, Condition condition)
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
		return Stats.Modifiers.IMMUNE_TO_INVISIBLE;
	}
}
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

import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.CowerInFearAction;
import mclachlan.maze.stat.combat.FreezeInTerrorAction;
import mclachlan.maze.stat.combat.RunAwayAction;
import mclachlan.maze.stat.*;

/**
 *
 */
public class FearEffect extends ConditionEffect
{
	private static final StatModifier fear = new StatModifier();

	/*-------------------------------------------------------------------------*/
	static
	{
		fear.setModifier(Stats.Modifier.ATTACK, -5);
		fear.setModifier(Stats.Modifier.DEFENCE, -5);
	}

	/*-------------------------------------------------------------------------*/
	public FearEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FearEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		int roll = Dice.d100.roll();
		if (roll <= 10)
		{
			return new CowerInFearAction();
		}
		else if (roll <= 20)
		{
			return new FreezeInTerrorAction();
		}
		else if (roll <= 30 && !GameSys.getInstance().isActorImmobile(actor))
		{
			return new RunAwayAction();
		}
		else
		{
			return action;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getModifier(Stats.Modifier modifier, Condition condition, ConditionBearer bearer)
	{
		return fear.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Stats.Modifier getImmunityModifier()
	{
		return Stats.Modifier.IMMUNE_TO_FEAR;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		return Personality.BasicSpeech.CONDITION_FEAR.getKey();
	}
}

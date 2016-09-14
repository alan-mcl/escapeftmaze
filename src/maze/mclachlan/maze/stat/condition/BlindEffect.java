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

import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.StumbleBlindlyAction;


/**
 *
 */
public class BlindEffect extends ConditionEffect
{
	private static final StatModifier blindness = new StatModifier();

	static
	{
		blindness.setModifier(Stats.Modifier.ATTACK, -15);
		blindness.setModifier(Stats.Modifier.DEFENCE, -15);
	}

	/*-------------------------------------------------------------------------*/
	public BlindEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public BlindEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		if (actor.getModifier(Stats.Modifier.BLIND_FIGHTING) > 0)
		{
			// actor can fight unimpaired
			return action;
		}

		int roll = Dice.d100.roll();

		if (roll <= 20)
		{
			return new StumbleBlindlyAction();
		}

		return action;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getModifier(Stats.Modifier modifier, Condition condition, ConditionBearer bearer)
	{
		if (Stats.Modifier.BLIND_FIGHTING.equals(modifier))
		{
			// to prevent an infinite loop
			return 0;
		}

		if (bearer instanceof UnifiedActor)
		{
			if (((UnifiedActor)bearer).getModifier(Stats.Modifier.BLIND_FIGHTING, false) > 0)
			{
				// actor can fight unimpaired.
				return 0;
			}
		}

		return blindness.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isAware(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Stats.Modifier getImmunityModifier()
	{
		return Stats.Modifier.IMMUNE_TO_BLIND;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		return Personality.BasicSpeech.CONDITION_BLIND.getKey();
	}
}

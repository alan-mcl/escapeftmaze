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
import mclachlan.maze.stat.combat.GagsHelplesslyAction;
import mclachlan.maze.stat.combat.RetchesNoisilyAction;


/**
 *
 */
public class NauseaEffect extends ConditionEffect
{
	private static final StatModifier nausea = new StatModifier();

	/*-------------------------------------------------------------------------*/
	static
	{
		nausea.setModifier(Stats.Modifiers.ATTACK, -5);
		nausea.setModifier(Stats.Modifiers.DEFENCE, -5);
		nausea.setModifier(Stats.Modifiers.INITIATIVE, -2);
		nausea.setModifier(Stats.Modifiers.BONUS_STRIKES, -1);
	}

	/*-------------------------------------------------------------------------*/
	public NauseaEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public NauseaEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		int roll = Dice.d100.roll();

		if (roll <= 15)
		{
			return new GagsHelplesslyAction();
		}
		else if (roll <= 30)
		{
			return new RetchesNoisilyAction();
		}

		return action;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getModifier(String modifier, Condition condition, ConditionBearer bearer)
	{
		return nausea.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getImmunityModifier()
	{
		return Stats.Modifiers.IMMUNE_TO_NAUSEA;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		return Personality.BasicSpeech.CONDITION_NAUSEA.getKey();
	}
}

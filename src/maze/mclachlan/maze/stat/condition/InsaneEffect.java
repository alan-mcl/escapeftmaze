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
import mclachlan.maze.stat.combat.AttackAlliesAction;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.DancesWildlyAction;
import mclachlan.maze.stat.combat.LaughsMadlyAction;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class InsaneEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public InsaneEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public InsaneEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		int roll = Dice.d100.roll();

		if (roll <= 25)
		{
			return new DancesWildlyAction();
		}
		else if (roll <= 50)
		{
			return new LaughsMadlyAction();
		}
		else if (roll <= 75 && !GameSys.getInstance().isActorImmobile(actor))
		{
			return new AttackAlliesAction(actor.getAttackWithOptions().get(0));
		}
		else
		{
			return action;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getImmunityModifier()
	{
		return Stats.Modifiers.IMMUNE_TO_INSANE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		switch (Dice.d3.roll())
		{
			case 1: return Personality.BasicSpeech.CONDITION_INSANE_1.getKey();
			case 2: return Personality.BasicSpeech.CONDITION_INSANE_2.getKey();
			case 3: return Personality.BasicSpeech.CONDITION_INSANE_3.getKey();
			default: throw new MazeException("oops");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String modifyPersonalitySpeech(String speechKey, String text, Personality p)
	{
		if (SilencedEffect.SILENCED_SPEECH.equals(text))
		{
			// do not override the silence
			return text;
		}

		switch (Dice.d3.roll())
		{
			case 1: return p.getWords(Personality.BasicSpeech.CONDITION_INSANE_1.getKey());
			case 2: return p.getWords(Personality.BasicSpeech.CONDITION_INSANE_2.getKey());
			case 3: return p.getWords(Personality.BasicSpeech.CONDITION_INSANE_3.getKey());
			default: throw new MazeException("oops");
		}
	}
}

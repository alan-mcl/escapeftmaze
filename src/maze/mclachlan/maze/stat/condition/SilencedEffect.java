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

import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.SpellAction;
import mclachlan.maze.stat.combat.SpellSilencedAction;


/**
 *
 */
public class SilencedEffect extends ConditionEffect
{
	public static final String SILENCED_SPEECH = " ... ";

	// todo: only disrupt CHANT and RHYME spells?

	/*-------------------------------------------------------------------------*/
	public SilencedEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SilencedEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		if (action instanceof SpellAction)
		{
			SpellAction sa = (SpellAction)action;
			
			return new SpellSilencedAction(
				sa.getTarget(),
				sa.getSpell(),
				sa.getCastingLevel());
		}

		return action;
	}

	/*-------------------------------------------------------------------------*/
	public String getImmunityModifier()
	{
		return Stats.Modifiers.IMMUNE_TO_SILENCE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String modifyPersonalitySpeech(String speechKey, String text,
		Personality p)
	{
		return SILENCED_SPEECH;
	}
}

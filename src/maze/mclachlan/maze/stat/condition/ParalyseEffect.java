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


/**
 *
 */
public class ParalyseEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public ParalyseEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ParalyseEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		return CombatAction.DO_NOTHING;
	}
	
	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isImmobile(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean askForCombatIntentions(UnifiedActor actor, Condition condition)
	{
		// todo: able to cast thought-only spells?
		return false;
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
		return Stats.Modifier.IMMUNE_TO_PARALYSE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		return Personality.BasicSpeech.CONDITION_PLYZE.getKey();
	}
}

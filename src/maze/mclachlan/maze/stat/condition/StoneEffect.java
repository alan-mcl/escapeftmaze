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

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.CombatAction;


/**
 *
 */
public class StoneEffect extends ConditionEffect
{
	static StatModifier stoned;

	/*-------------------------------------------------------------------------*/
	static
	{
		stoned = new StatModifier();
		stoned.setModifier(Stats.Modifier.VS_PENETRATE, 20);

		// stone resistences
		stoned.setModifier(Stats.Modifier.RESIST_BLUDGEONING, 75);
		stoned.setModifier(Stats.Modifier.RESIST_PIERCING, 75);
		stoned.setModifier(Stats.Modifier.RESIST_SLASHING, 75);
		stoned.setModifier(Stats.Modifier.RESIST_FIRE, 75);
		stoned.setModifier(Stats.Modifier.RESIST_WATER, 75);
		stoned.setModifier(Stats.Modifier.RESIST_AIR, 75);

		// stone immunities
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_BLIND, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_FEAR, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_INSANE, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_IRRITATE, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_KO, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_NAUSEA, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_PARALYSE, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_POISON, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_POSSESSION, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_SLEEP, 1);
		stoned.setModifier(Stats.Modifier.IMMUNE_TO_PSYCHIC, 1);
	}

	/*-------------------------------------------------------------------------*/
	public StoneEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public StoneEffect(String name)
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
	public int getModifier(Stats.Modifier modifier, Condition condition, ConditionBearer bearer)
	{
		return stoned.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isRemovedByRevitalise(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean askForCombatIntentions(UnifiedActor actor, Condition condition)
	{
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
		return Stats.Modifier.IMMUNE_TO_STONE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isImmobile(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isHelpless(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(Condition condition, long turnNr)
	{
		// let stone never expire naturally
		condition.decDuration(-1);
		return null;
	}
}

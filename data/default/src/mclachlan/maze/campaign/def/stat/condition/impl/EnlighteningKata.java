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

package mclachlan.maze.campaign.def.stat.condition.impl;

import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;

/**
 * A custom condition impl for the Warlock's enlightening kata ability
 */
public class EnlighteningKata extends Condition
{
	private Stats.Modifier favouredEnemyModifier;

	/*-------------------------------------------------------------------------*/
	public EnlighteningKata(Stats.Modifier favouredEnemyModifier)
	{
		this.favouredEnemyModifier = favouredEnemyModifier;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "enlightening kata";
	}

	@Override
	public String getDisplayName()
	{
		return "Enlightening Kata";
	}

	@Override
	public String getIcon()
	{
		return "condition/enlighteningkata";
	}

	@Override
	public String getAdjective()
	{
		return "enlightened";
	}

	@Override
	public int getModifier(Stats.Modifier modifier, ConditionBearer bearer)
	{
		if (favouredEnemyModifier == modifier)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	@Override
	public ConditionEffect getEffect()
	{
		return ConditionEffect.NONE;
	}

	@Override
	public boolean isStrengthWanes()
	{
		return false;
	}

	@Override
	public void setCastingLevel(int castingLevel)
	{
		super.setCastingLevel(castingLevel);
		super.setDuration(2*castingLevel);
	}

	@Override
	public MagicSys.SpellEffectType getType()
	{
		return MagicSys.SpellEffectType.MENTAL;
	}

	@Override
	public MagicSys.SpellEffectSubType getSubtype()
	{
		return MagicSys.SpellEffectSubType.NONE;
	}

	@Override
	public boolean isAffliction()
	{
		return false;
	}

	@Override
	public boolean isIdentified()
	{
		return true;
	}

	@Override
	public ConditionTemplate.ExitCondition getExitCondition()
	{
		return ConditionTemplate.ExitCondition.DURATION_EXPIRES;
	}
}

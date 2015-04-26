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

package mclachlan.maze.stat.condition.impl;

import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.combat.event.GuardianAngelEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 * A custom condition impl for when an actor passes out from fatigue.
 */
public class GuardianAngel extends Condition
{
	private static ConditionEffect effect = new GuardianAngelEffect();
	int hitPoints;
	
	/*-------------------------------------------------------------------------*/
	public GuardianAngel()
	{
		setDuration(1);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.GUARDIAN_ANGEL;
	}

	@Override
	public String getDisplayName()
	{
		return "Guardian Angel";
	}

	@Override
	public String getIcon()
	{
		return "condition/guardianangel";
	}

	@Override
	public String getAdjective()
	{
		return "warded";
	}

	@Override
	public int getModifier(String modifier, ConditionBearer bearer)
	{
		return 0;
	}

	@Override
	public ConditionEffect getEffect()
	{
		return effect;
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
		super.setDuration(2+castingLevel);
		this.hitPoints = castingLevel*7;
	}

	@Override
	public MagicSys.SpellEffectType getType()
	{
		return MagicSys.SpellEffectType.ENERGY;
	}

	@Override
	public MagicSys.SpellEffectSubType getSubtype()
	{
		return MagicSys.SpellEffectSubType.NONE;
	}
	
	/*-------------------------------------------------------------------------*/
	static class GuardianAngelEffect extends ConditionEffect
	{
		@Override
		public int damageToTarget(UnifiedActor actor, Condition condition, int damage, DamageEvent event)
		{
			if (damage <= 0)
			{
				return damage;
			}
			
			GuardianAngel ga = (GuardianAngel)condition;
			
			int damageToCharacter = 0;
			int damageToAngel = 0;
			
			if (ga.hitPoints >= damage)
			{
				damageToCharacter = 0;
				damageToAngel = damage;
			}
			else
			{
				damageToCharacter = damage - ga.hitPoints;
				damageToAngel = ga.hitPoints;
			}
			
			ga.hitPoints -= damageToAngel;
			Maze.getInstance().resolveEvent(new GuardianAngelEvent(damageToAngel), true);
			if (ga.hitPoints <= 0)
			{
				actor.removeCondition(ga);
			}
			return damageToCharacter;
		}
	}
}

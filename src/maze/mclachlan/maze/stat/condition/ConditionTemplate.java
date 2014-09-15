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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.util.MazeException;

/**
 */
public class ConditionTemplate
{
	/** The name of this condition */
	String name;

	/** The display name of this condition */
	String displayName;

	/** The icon for this condition */
	String icon;

	/** How this condition is described in combat */
	String adjective;

	/** Any special game effect that this condition has */
	ConditionEffect conditionEffect;

	/** The duration for this condition */
	Value duration;

	/** The strength of this condition */
	Value strength;

	/** The damage dealt by this condition every turn */
	Value hitPointDamage;
	
	/** The stamina damage dealt by this condition every turn */
	Value staminaDamage;
	
	/** The stealth damage dealt by this condition every turn */
	Value actionPointDamage;
	
	/** The magic damage dealt by this condition every turn */
	Value magicPointDamage;

	/** The modifiers to actors affected by this condition */
	StatModifier statModifier;

	/** The modifiers to other actors in the same group as the actor bearing this condition */
	StatModifier bannerModifier;

	/** true if the stat modifiers for the condition should be multiplied by it's strength */
	boolean scaleModifierWithStrength;

	/** true if the strength of this condition wanes by 1 every turn */
	boolean strengthWanes;

	/** an alternative, custom implementation */
	Class impl;

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate(
		String name,
		String displayName,
		Value duration,
		Value strength,
		ConditionEffect effect,
		StatModifier statModifier,
		StatModifier bannerModifier,
		Value hitPointDamage,
		Value staminaDamage,
		Value actionPointDamage,
		Value magicPointDamage,
		String icon,
		String adjective,
		boolean scaleModifierWithStrength,
		boolean strengthWanes)
	{
		this.displayName = displayName;
		this.bannerModifier = bannerModifier;
		this.magicPointDamage = magicPointDamage;
		this.actionPointDamage = actionPointDamage;
		this.staminaDamage = staminaDamage;
		this.strength = strength;
		this.conditionEffect = effect;
		this.hitPointDamage = hitPointDamage;
		this.duration = duration;
		this.icon = icon;
		this.name = name;
		this.statModifier = statModifier;
		this.adjective = adjective;
		this.scaleModifierWithStrength = scaleModifierWithStrength;
		this.strengthWanes = strengthWanes;
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate(String name, Class impl)
	{
		this.name = name;
		this.impl = impl;
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate(String adjective)
	{
		this.adjective = adjective;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setImpl(Class impl)
	{
		this.impl = impl;
	}

	/*-------------------------------------------------------------------------*/
	public String getAdjective()
	{
		return adjective;
	}

	public Value getHitPointDamage()
	{
		return hitPointDamage;
	}

	public Value getDuration()
	{
		return duration;
	}

	public ConditionEffect getConditionEffect()
	{
		return conditionEffect;
	}

	public String getIcon()
	{
		return icon;
	}

	public Class getImpl()
	{
		return impl;
	}

	public boolean scaleModifierWithStrength()
	{
		return scaleModifierWithStrength;
	}

	public StatModifier getStatModifier()
	{
		return statModifier;
	}

	public StatModifier getBannerModifier()
	{
		return bannerModifier;
	}

	public Value getStrength()
	{
		return strength;
	}

	public boolean strengthWanes()
	{
		return strengthWanes;
	}

	public Value getMagicPointDamage()
	{
		return magicPointDamage;
	}

	public Value getStaminaDamage()
	{
		return staminaDamage;
	}

	public Value getActionPointDamage()
	{
		return actionPointDamage;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/*-------------------------------------------------------------------------*/
	public void setAdjective(String adjective)
	{
		this.adjective = adjective;
	}

	public void setConditionEffect(ConditionEffect conditionEffect)
	{
		this.conditionEffect = conditionEffect;
	}

	public void setHitPointDamage(Value hitPointDamage)
	{
		this.hitPointDamage = hitPointDamage;
	}

	public void setDuration(Value duration)
	{
		this.duration = duration;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setScaleModifierWithStrength(boolean scaleModifierWithStrength)
	{
		this.scaleModifierWithStrength = scaleModifierWithStrength;
	}

	public void setStatModifier(StatModifier statModifier)
	{
		this.statModifier = statModifier;
	}

	public void setBannerModifier(StatModifier bannerModifier)
	{
		this.bannerModifier = bannerModifier;
	}

	public void setStrength(Value strength)
	{
		this.strength = strength;
	}

	public void setStrengthWanes(boolean strengthWanes)
	{
		this.strengthWanes = strengthWanes;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setMagicPointDamage(Value magicPointDamage)
	{
		this.magicPointDamage = magicPointDamage;
	}

	public void setStaminaDamage(Value staminaDamage)
	{
		this.staminaDamage = staminaDamage;
	}

	public void setActionPointDamage(Value actionPointDamage)
	{
		this.actionPointDamage = actionPointDamage;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @param source
	 * 	The source of the condition
	 * @param target
	 * 	The target of the condition
	 * @param castingLevel
	 * 	The casting level of the condition
	 * @param type
	 * 	The type of the condition, a constant from
	 * 	{@link mclachlan.maze.stat.magic.MagicSys.SpellEffectType}
	 * @return
	 */
	public Condition create(
		UnifiedActor source,
		ConditionBearer target,
		int castingLevel,
		int type,
		MagicSys.SpellEffectSubType subtype)
	{
		if (impl != null)
		{
			// a custom condition impl
			try
			{
				Condition result = (Condition)impl.newInstance();
				result.setTemplate(this);
				result.setSource(source);
				result.setTarget(target);
				result.setCastingLevel(castingLevel);
				return result;
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}
		else
		{
			// a bog standard condition
			return new Condition(this, source, target, castingLevel, type, subtype);
		}
	}
}

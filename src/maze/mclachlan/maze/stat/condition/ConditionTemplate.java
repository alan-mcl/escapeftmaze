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
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.util.MazeException;

/**
 */
public class ConditionTemplate extends DataObject
{
	/** The name of this condition */
	private String name;

	/** The display name of this condition */
	private String displayName;

	/** The icon for this condition */
	private String icon;

	/** How this condition is described in combat */
	private String adjective;

	/** Any special game effect that this condition has */
	private ConditionEffect conditionEffect;

	/** The duration for this condition */
	private ValueList duration;

	/** The strength of this condition */
	private ValueList strength;

	/** The damage dealt by this condition every turn */
	private ValueList hitPointDamage;
	
	/** The stamina damage dealt by this condition every turn */
	private ValueList staminaDamage;
	
	/** The stealth damage dealt by this condition every turn */
	private ValueList actionPointDamage;
	
	/** The magic damage dealt by this condition every turn */
	private ValueList magicPointDamage;

	/** The modifiers to actors affected by this condition */
	private StatModifier statModifier;

	/** The modifiers to other actors in the same group as the actor bearing this condition */
	private StatModifier bannerModifier;

	/** true if the stat modifiers for the condition should be multiplied by it's strength */
	private boolean scaleModifierWithStrength;

	/** true if the strength of this condition wanes by 1 every turn */
	private boolean strengthWanes;

	/** exit condition for this condition */
	private ExitCondition exitCondition;

	/** any exit condition probability, if needed. A number out of 1000 */
	private int exitConditionChance;

	/** spell effect to apply to bearer on exit*/
	private String exitSpellEffect;

	/** any repeated spell effects applied to the bearer of the condition */
	private List<RepeatedSpellEffect> repeatedSpellEffects;

	/** an alternative, custom implementation */
	private Class impl;

	/*-------------------------------------------------------------------------*/
	public static enum ExitCondition
	{
		NEVER,
		DURATION_EXPIRES,
		CHANCE_AT_EOT
		// todo: chance at successful save
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate(
		String name,
		String displayName,
		ValueList duration,
		ValueList strength,
		ConditionEffect effect,
		StatModifier statModifier,
		StatModifier bannerModifier,
		ValueList hitPointDamage,
		ValueList staminaDamage,
		ValueList actionPointDamage,
		ValueList magicPointDamage,
		String icon,
		String adjective,
		boolean scaleModifierWithStrength,
		boolean strengthWanes,
		ExitCondition exitCondition,
		int exitConditionChance,
		String exitSpellEffect,
		List<RepeatedSpellEffect> repeatedSpellEffects)
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
		this.exitCondition = exitCondition;
		this.exitConditionChance = exitConditionChance;
		this.exitSpellEffect = exitSpellEffect;
		this.repeatedSpellEffects = repeatedSpellEffects;
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate(String name, Class impl)
	{
		this.name = name;
		this.impl = impl;
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

	public ValueList getHitPointDamage()
	{
		return hitPointDamage;
	}

	public ValueList getDuration()
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

	public boolean isScaleModifierWithStrength()
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

	public ValueList getStrength()
	{
		return strength;
	}

	public boolean isStrengthWanes()
	{
		return strengthWanes;
	}

	public ValueList getMagicPointDamage()
	{
		return magicPointDamage;
	}

	public ValueList getStaminaDamage()
	{
		return staminaDamage;
	}

	public ValueList getActionPointDamage()
	{
		return actionPointDamage;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public ExitCondition getExitCondition()
	{
		return exitCondition;
	}

	public int getExitConditionChance()
	{
		return exitConditionChance;
	}

	public List<RepeatedSpellEffect> getRepeatedSpellEffects()
	{
		return repeatedSpellEffects;
	}

	public String getExitSpellEffect()
	{
		return exitSpellEffect;
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

	public void setHitPointDamage(ValueList hitPointDamage)
	{
		this.hitPointDamage = hitPointDamage;
	}

	public void setDuration(ValueList duration)
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

	public void setStrength(ValueList strength)
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

	public void setMagicPointDamage(ValueList magicPointDamage)
	{
		this.magicPointDamage = magicPointDamage;
	}

	public void setStaminaDamage(ValueList staminaDamage)
	{
		this.staminaDamage = staminaDamage;
	}

	public void setActionPointDamage(ValueList actionPointDamage)
	{
		this.actionPointDamage = actionPointDamage;
	}

	public void setRepeatedSpellEffects(
		List<RepeatedSpellEffect> repeatedSpellEffects)
	{
		this.repeatedSpellEffects = repeatedSpellEffects;
	}

	public void setExitCondition(ExitCondition exitCondition)
	{
		this.exitCondition = exitCondition;
	}

	public void setExitConditionChance(int exitConditionChance)
	{
		this.exitConditionChance = exitConditionChance;
	}

	public void setExitSpellEffect(String exitSpellEffect)
	{
		this.exitSpellEffect = exitSpellEffect;
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
		MagicSys.SpellEffectType type,
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
			ValueList durationValue = getDuration();
			int duration = 0;
			if (durationValue != null)
			{
				duration = durationValue.compute(source, castingLevel);
			}
			int strength = 0;
			ValueList strengthValue = getStrength();
			if (strengthValue != null)
			{
				strength = strengthValue.compute(source, castingLevel);
			}
			ValueList hpDamage = getHitPointDamage() == null ? null :
				getHitPointDamage().getSnapShotValue(source, castingLevel);
			ValueList apDamage = getActionPointDamage() == null ? null :
				getActionPointDamage().getSnapShotValue(source, castingLevel);
			ValueList mpDamage = getMagicPointDamage() == null ? null :
				getMagicPointDamage().getSnapShotValue(source, castingLevel);
			ValueList staminaDamage = getStaminaDamage() == null ? null :
				getStaminaDamage().getSnapShotValue(source, castingLevel);

			// could use a smarter algorithm here
			boolean hostile = true;
			if (source instanceof PlayerCharacter && target instanceof PlayerCharacter)
			{
				hostile = false;
			}

			Condition result = new Condition(
				this,
				duration,
				strength,
				castingLevel,
				hpDamage,
				apDamage,
				mpDamage,
				staminaDamage,
				type,
				subtype,
				source,
				false,
				false,
				GameTime.getTurnNr(),
				hostile);

			// strength never begins identified
			result.setStrengthIdentified(false);

			// bit of a hack, but it's easy and safe to say that afflictions
			// targeting PCs are the only things the need to be not identified
			// at the moment.
			if (result.isAffliction() && target instanceof PlayerCharacter)
			{
				result.setIdentified(false);

				if (((PlayerCharacter)target).getModifier(Stats.Modifier.SELF_AWARENESS) > 0)
				{
					result.setIdentified(true);
				}

				if (((PlayerCharacter)target).getModifier(Stats.Modifier.SELF_AWARENESS) > 1)
				{
					result.setStrengthIdentified(true);
				}
			}
			else
			{
				result.setIdentified(true);
			}

			result.setTarget(target);

			return result;
		}
	}
}

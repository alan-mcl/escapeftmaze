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
import mclachlan.maze.stat.DamagePacket;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageActionPointsEvent;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.combat.event.DamageMagicEvent;
import mclachlan.maze.stat.combat.event.FatigueEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Value;

/**
 * Conditions can be attached to: a PlayerCharacter, a Foe, a Tile
 */
public class Condition
{
	ConditionTemplate template;
	UnifiedActor source;
	ConditionBearer target;
	int duration;
	int strength;
	int castingLevel;
	Value hitPointDamage;
	int type;
	MagicSys.SpellEffectSubType subtype;

	/*-------------------------------------------------------------------------*/
	protected Condition()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Re-creates a condition from the given properties
	 */
	public Condition(
		ConditionTemplate template,
		int duration,
		int strength,
		int castingLevel,
		Value damage,
		int type,
		MagicSys.SpellEffectSubType subtype,
		UnifiedActor source)
	{
		// todo: this constructor should take stamina/action/magic damage values
		
		this.template = template;
		this.hitPointDamage = damage;
		this.subtype = subtype;
		this.source = source;
		this.type = type;
		this.duration = duration;
		this.strength = strength;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new Condition from the given template, with the given params
	 */
	protected Condition(
		ConditionTemplate template,
		UnifiedActor source,
		ConditionBearer target,
		int castingLevel,
		int type,
		MagicSys.SpellEffectSubType subtype)
	{
		this.template = template;
		this.target = target;
		this.source = source;
		this.type = type;
		this.subtype = subtype;
		this.duration = template.duration.compute(source, castingLevel);
		this.strength = template.strength.compute(source, castingLevel);
		this.castingLevel = castingLevel;

		if (template.hitPointDamage != null)
		{
			// snapshot the Value
			this.hitPointDamage = template.hitPointDamage.getSnapShotValue(source, castingLevel);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setTemplate(ConditionTemplate template)
	{
		this.template = template;
	}

	public void setSource(UnifiedActor source)
	{
		this.source = source;
	}

	public void setTarget(ConditionBearer target)
	{
		this.target = target;
	}

	public void setCastingLevel(int castingLevel)
	{
		this.castingLevel = castingLevel;
	}

	public void setHitPointDamage(Value hitPointDamage)
	{
		this.hitPointDamage = hitPointDamage;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public void setStrength(int strength)
	{
		this.strength = strength;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setSubtype(MagicSys.SpellEffectSubType subtype)
	{
		this.subtype = subtype;
	}

	/*-------------------------------------------------------------------------*/
	public Value getHitPointDamage()
	{
		return hitPointDamage;
	}

	public int getDuration()
	{
		return duration;
	}

	public String getIcon()
	{
		return template.icon;
	}

	public String getName()
	{
		return template.name;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public int getModifier(String modifier, ConditionBearer bearer)
	{
		int result = 0;

		if (template.statModifier != null)
		{
			result += template.statModifier.getModifier(modifier);
		}
		
		if (template.scaleModifierWithStrength)
		{
			result *= strength;
		}

		result += template.conditionEffect.getModifier(modifier, this, bearer);
		
		return result;
	}

	public int getStrength()
	{
		return strength;
	}

	public String getAdjective()
	{
		return template.adjective;
	}

	public String getDisplayName()
	{
		return template.displayName;
	}

	public ConditionBearer getTarget()
	{
		return target;
	}

	public UnifiedActor getSource()
	{
		return source;
	}

	public int getType()
	{
		return type;
	}

	public MagicSys.SpellEffectSubType getSubtype()
	{
		return subtype;
	}

	public ConditionEffect getEffect()
	{
		return template.conditionEffect;
	}

	public void decDuration(int value)
	{
		duration -= value;
	}

	public void decStrength(int value)
	{
		strength -= value;
	}

	public void expire()
	{
		target.removeCondition(this);
	}

	public boolean strengthWanes()
	{
		return template.strengthWanes;
	}

	public ConditionTemplate getTemplate()
	{
		return template;
	}
	
	public Value getMagicPointDamage()
	{
		return template.magicPointDamage;
	}

	public Value getStaminaDamage()
	{
		return template.staminaDamage;
	}

	public Value getActionPointDamage()
	{
		return template.actionPointDamage;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.
	 * @param turnNr
	 */
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		List<MazeEvent> effectEvents = getEffect().endOfTurn(this, turnNr);
		if (effectEvents != null)
		{
			for (MazeEvent effectEvent : effectEvents)
			{
				result.add(effectEvent);
			}
		}

		if (getTarget() instanceof UnifiedActor
			&& ((UnifiedActor)getTarget()).getHitPoints().getCurrent() > 0
			&& duration > -1)
		{
			// tiles are immune to damage, obviously

			if (getHitPointDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();
				int damage = getHitPointDamage().compute(
					target,
					getCastingLevel());
				result.add(
					new DamageEvent(
						target,
						getSource(),
						new DamagePacket(damage, 1), 
						type,
						subtype,
						null, null));
			}
			
			if (getStaminaDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();
				result.add(
					new FatigueEvent(
						target,
						getSource(),
						getStaminaDamage().compute(
							target,
							getCastingLevel()),
						type,
						subtype));
			}
			
			if (getActionPointDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();
				result.add(
					new DamageActionPointsEvent(
						target,
						getSource(),
						getActionPointDamage().compute(
							target,
							getCastingLevel()),
						type,
						subtype));
			}
			
			if (getMagicPointDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();
				result.add(
					new DamageMagicEvent(
						target,
						getSource(),
						getMagicPointDamage().compute(
							target,
							getCastingLevel()),
						type,
						subtype));
			}
		}

		decDuration(1);
		if (strengthWanes())
		{
			decStrength(1);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String modifyPersonalitySpeech(String speechKey, String text, Personality p)
	{
		if (template != null)
		{
			ConditionEffect effect = template.getConditionEffect();

			if (effect != null)
			{
				return effect.modifyPersonalitySpeech(speechKey, text, p);
			}
		}

		return text;
	}
}

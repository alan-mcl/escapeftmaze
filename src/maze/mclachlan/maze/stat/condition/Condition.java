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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.DamagePacket;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.SpellTargetUtils;
import mclachlan.maze.stat.combat.event.DamageActionPointsEvent;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.combat.event.DamageMagicEvent;
import mclachlan.maze.stat.combat.event.FatigueEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.Value;

import static mclachlan.maze.data.StringUtil.getGamesysString;

/**
 * Conditions can be attached to: a PlayerCharacter, a Foe, a Tile
 */
public class Condition
{
	private ConditionTemplate template;
	private UnifiedActor source;
	private ConditionBearer target;
	private int duration;
	private int strength;
	private int castingLevel;
	private Value hitPointDamage, actionPointDamage, magicPointDamage, staminaDamage;
	private MagicSys.SpellEffectType type;
	private MagicSys.SpellEffectSubType subtype;

	private boolean identified, strengthIdentified;

	/** turn that this condition was created */
	private long createdTurn;

	/** is this a hostile condition or a friendly one? */
	private boolean hostile;

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
		Value hpDamage,
		Value apDamage,
		Value mpDamage,
		Value staminaDamage,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subtype,
		UnifiedActor source,
		boolean identified,
		boolean strengthIdentified,
		long createdTurn,
		boolean hostile)
	{
		this.template = template;
		this.hitPointDamage = hpDamage;
		this.actionPointDamage = apDamage;
		this.magicPointDamage = mpDamage;
		this.hitPointDamage = hpDamage;
		this.staminaDamage = staminaDamage;
		this.subtype = subtype;
		this.source = source;
		this.type = type;
		this.duration = duration;
		this.strength = strength;
		this.castingLevel = castingLevel;
		this.identified = identified;
		this.strengthIdentified = strengthIdentified;
		this.createdTurn = createdTurn;
		this.hostile = hostile;
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

	public void setActionPointDamage(Value actionPointDamage)
	{
		this.actionPointDamage = actionPointDamage;
	}

	public void setMagicPointDamage(Value magicPointDamage)
	{
		this.magicPointDamage = magicPointDamage;
	}

	public void setStaminaDamage(Value staminaDamage)
	{
		this.staminaDamage = staminaDamage;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public void setStrength(int strength)
	{
		this.strength = strength;
	}

	public void setType(MagicSys.SpellEffectType type)
	{
		this.type = type;
	}

	public void setSubtype(MagicSys.SpellEffectSubType subtype)
	{
		this.subtype = subtype;
	}

	public void setHostile(boolean hostile)
	{
		this.hostile = hostile;
	}

	public void setCreatedTurn(long createdTurn)
	{
		this.createdTurn = createdTurn;
	}

	/*-------------------------------------------------------------------------*/

	public int getDuration()
	{
		return duration;
	}

	public String getIcon()
	{
		return template.getIcon();
	}

	public String getName()
	{
		return template.getName();
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public int getModifier(String modifier, ConditionBearer bearer)
	{
		int result = 0;

		if (template.getStatModifier() != null)
		{
			result += template.getStatModifier().getModifier(modifier);
		}
		
		if (template.isScaleModifierWithStrength())
		{
			result *= strength;
		}

		result += template.getConditionEffect().getModifier(modifier, this, bearer);
		
		return result;
	}

	public int getStrength()
	{
		return strength;
	}

	public String getAdjective()
	{
		return template.getAdjective();
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		if (isIdentified())
		{
			return template.getDisplayName();
		}
		else if (isAffliction())
		{
			return getGamesysString("cond.unknown.affliction");
		}
		else
		{
			return getGamesysString("cond.unknown.condition");
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayIcon()
	{
		if (isIdentified())
		{
			return getIcon();
		}
		else
		{
			return "condition/unknown_condition";
		}
	}

	/*-------------------------------------------------------------------------*/

	public ConditionBearer getTarget()
	{
		return target;
	}

	public UnifiedActor getSource()
	{
		return source;
	}

	public MagicSys.SpellEffectType getType()
	{
		return type;
	}

	public MagicSys.SpellEffectSubType getSubtype()
	{
		return subtype;
	}

	public ConditionEffect getEffect()
	{
		return template.getConditionEffect();
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
		Maze.log(Log.DEBUG, "condition ["+template.getName()+ "] on ["+
			target.getName()+"] expires");
		target.removeCondition(this);
	}

	public boolean isStrengthWanes()
	{
		return template.isStrengthWanes();
	}

	public ConditionTemplate getTemplate()
	{
		return template;
	}

	public Value getHitPointDamage()
	{
		return hitPointDamage;
	}
	
	public Value getMagicPointDamage()
	{
		return this.magicPointDamage;
	}

	public Value getStaminaDamage()
	{
		return this.staminaDamage;
	}

	public Value getActionPointDamage()
	{
		return this.actionPointDamage;
	}

	public boolean isIdentified()
	{
		return identified;
	}

	public void setIdentified(boolean isIdentified)
	{
		this.identified = isIdentified;
	}

	public boolean isStrengthIdentified()
	{
		return strengthIdentified;
	}

	public void setStrengthIdentified(boolean strengthIdentified)
	{
		this.strengthIdentified = strengthIdentified;
	}

	public long getCreatedTurn()
	{
		return createdTurn;
	}

	public boolean isHostile()
	{
		return hostile;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.
	 * @param turnNr
	 */
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		Maze.log(Log.DEBUG, "End of turn processing for condition ["+
			this.getName()+"] on ["+target.getName()+"]");

		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		//
		// Apply any condition effect events
		//
		List<MazeEvent> effectEvents = getEffect().endOfTurn(this, turnNr);
		if (effectEvents != null)
		{
			for (MazeEvent effectEvent : effectEvents)
			{
				Maze.log(Log.DEBUG, " + "+effectEvent.toString());
				result.add(effectEvent);
			}
		}

		//
		// Apply any repeated spell effects
		//
		if (target instanceof UnifiedActor &&
			template.getRepeatedSpellEffects() != null &&
			!template.getRepeatedSpellEffects().isEmpty())
		{
			long conditionTurnNr = turnNr - createdTurn;

			Maze.log(Log.DEBUG, "Repeated spell effects / cond turn nr=["+conditionTurnNr+"]");

			for (RepeatedSpellEffect rse : template.getRepeatedSpellEffects())
			{
				Maze.log(Log.DEBUG, " ... "+rse.getSpellEffect());

				if (
					(conditionTurnNr >= rse.getStartTurn() &&
						(conditionTurnNr <= rse.getEndTurn() || rse.getEndTurn() == -1)
					&&
					(conditionTurnNr % rse.getTurnMod() == 0)))
				{
					// This RSE is within it's period of activity

					if (Dice.d100.roll() <= rse.getProbability())
					{
						SpellEffect spellEffect =
							Database.getInstance().getSpellEffect(rse.getSpellEffect());

						if (isHostile())
						{
							result.addAll(
								SpellTargetUtils.applySpellEffectToUnwillingVictim(
									spellEffect,
									(UnifiedActor)target,
									source,
									castingLevel,
									castingLevel));
						}
						else
						{
							result.addAll(
								SpellTargetUtils.applySpellEffectToWillingTarget(
									spellEffect,
									(UnifiedActor)target,
									source,
									castingLevel));
						}
					}
				}
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

				DamageEvent event = new DamageEvent(
					target,
					getSource(),
					new DamagePacket(damage, 1),
					type,
					subtype,
					null, null);

				Maze.log(Log.DEBUG, " + " + event.toString());

				result.add(event);
			}
			
			if (getStaminaDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();

				FatigueEvent event = new FatigueEvent(
					target,
					getSource(),
					getStaminaDamage().compute(
						target,
						getCastingLevel()),
					type,
					subtype);

				Maze.log(Log.DEBUG, " + "+event.toString());

				result.add(event);
			}
			
			if (getActionPointDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();

				DamageActionPointsEvent event = new DamageActionPointsEvent(
					target,
					getSource(),
					getActionPointDamage().compute(
						target,
						getCastingLevel()),
					type,
					subtype);

				Maze.log(Log.DEBUG, " + "+event.toString());

				result.add(event);
			}
			
			if (getMagicPointDamage() != null)
			{
				UnifiedActor target = (UnifiedActor)getTarget();

				DamageMagicEvent event = new DamageMagicEvent(
					target,
					getSource(),
					getMagicPointDamage().compute(
						target,
						getCastingLevel()),
					type,
					subtype);

				Maze.log(Log.DEBUG, " + "+event.toString());

				result.add(event);
			}
		}

		// check condition exit conditions

		if (template.getExitCondition() == ConditionTemplate.ExitCondition.DURATION_EXPIRES)
		{
			// Duration only matters for conditions with this exit condition

			decDuration(1);
			if (isStrengthWanes())
			{
				decStrength(1);
			}

			Maze.log(Log.DEBUG, "exit condition ["+template.getExitCondition()+
				"] duration ["+getDuration()+"] strength ["+getStrength()+"]");
		}
		else if (template.getExitCondition() == ConditionTemplate.ExitCondition.CHANCE_AT_EOT)
		{
			// Check for EOT chance expiration

			if (Dice.d1000.roll() <= template.getExitConditionChance())
			{
				//expire condition
				setDuration(-1);
				Maze.log(Log.DEBUG, "exit condition ["+template.getExitCondition()+ "] expires!");
			}
			else
			{
				Maze.log(Log.DEBUG, "exit condition ["+template.getExitCondition()+
					"] does not expire!");
			}
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

	/*-------------------------------------------------------------------------*/
	public boolean isAffliction()
	{
		return
			MagicSys.SpellEffectSubType.CURSE.equals(subtype) ||
			MagicSys.SpellEffectSubType.POISON.equals(subtype) ||
			MagicSys.SpellEffectSubType.DISEASE.equals(subtype);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Integer> getModifiers()
	{
		return getTemplate().getStatModifier().getModifiers();
	}
}

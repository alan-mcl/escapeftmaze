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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatStatistics;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DamageEvent extends MazeEvent
{
	private Combat combat;
	private UnifiedActor defender;
	private UnifiedActor attacker;
	private DamagePacket damagePacket;
	private MagicSys.SpellEffectType type;
	private MagicSys.SpellEffectSubType subtype;
	/** may be null in the case of spells/conditions */
	private AttackWith attackWith;

	private int finalDamage;
	private CombatStatistics stats;
	private AnimationContext animationContext;

	/*-------------------------------------------------------------------------*/
	public DamageEvent(
		Combat combat,
		UnifiedActor defender,
		UnifiedActor attacker,
		DamagePacket damagePacket,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subtype,
		AttackWith attackWith,
		AnimationContext animationContext)
	{
		this.combat = combat;
		this.defender = defender;
		this.attacker = attacker;
		this.damagePacket = damagePacket;
		this.type = type;
		this.subtype = subtype;
		this.attackWith = attackWith;
		if (combat != null)
		{
			this.stats = combat.getCombatStatistics();
		}
		this.animationContext = animationContext;

		if (subtype == null)
		{
			throw new MazeException("null subtype");
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public DamagePacket getDamagePacket()
	{
		return damagePacket;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Applies the damage to the defender. 
	 */ 
	public List<MazeEvent> resolve()
	{
		// todo: maybe move all this into GameSys?

		int damage = this.damagePacket.getAmount();

		// apply resistances and immunities
		if (GameSys.getInstance().isActorImmuneToSpellEffect(defender, subtype))
		{
			damage = 0;
		}
		else
		{
			damage -= (damage * GameSys.getInstance().getResistance(defender, attacker, type) / 100);
		}

		List<Condition> list = new ArrayList<Condition>(defender.getConditions());
		for (Condition c : list)
		{
			damage = c.getEffect().damageToTarget(defender, c, damage, this);
		}

		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		damage = Math.max(damage, 0);

		damage *= damagePacket.getMultiplier();

		finalDamage = damage;

		if (stats != null)
		{
			stats.captureAttackDamage(attacker, finalDamage);
		}

		CurMaxSub hitPoints = defender.getHitPoints();
		boolean criticalHit = false;
		if (finalDamage > 0
			&& GameSys.getInstance().isCriticalHit(attacker, defender, attackWith)
			&& !GameSys.getInstance().saveVsCritical(defender, attacker))
		{
			result.add(new CriticalHitEvent(defender));
			hitPoints.setCurrent(0);
			criticalHit = true;
		}
		else
		{
			hitPoints.decCurrent(finalDamage);
		}
		
		if (hitPoints.getCurrent() <= 0 || criticalHit)
		{
			// defender is dead
			if (GameSys.getInstance().actorCheatsDeath(defender))
			{
				GameSys.getInstance().cheatDeath(defender);
				result.add(new ActorCheatsDeathEvent(defender));
			}
			else
			{
				CombatantData data = defender.getCombatantData();
				if (data != null)
				{
					data.setActive(false);
				}

				// check for DYING_BLOW
				if (defender.getModifier(Stats.Modifier.DYING_BLOW) > 0 &&
					!attackWith.isRanged())
				{
					Item attackWith = defender.getPrimaryWeapon();
					result.add(
						new UiMessageEvent(
							StringUtil.getEventText(
								"msg.dying.blow",
								defender.getDisplayName())));
					result.add(
						new AttackEvent(
							combat,
							defender,
							attacker,
							attackWith,
							GameSys.getInstance().getAttackType(attackWith),
							0,
							1,
							attackWith.getAttackScript(),
							attackWith.getDefaultDamageType(),
							animationContext, null));
				}
	
				result.add(new ActorDiesEvent(defender, attacker));
			}
		}
		else if (hitPoints.getSub() >= hitPoints.getCurrent())
		{
			ConditionTemplate kot = Database.getInstance().getConditionTemplate(
				Constants.Conditions.FATIGUE_KO);
			Condition ko = kot.create(
				attacker, defender, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);

			// defender is KO
			result.add(new ConditionEvent(defender, ko));
		}
		
		// take damage reflection into account (for melee attacks only)
		int damageReflection = defender.getModifier(Stats.Modifier.RAZOR_CLOAK);
		if (damageReflection > 0 && attackWith != null)
		{
			Dice d = new Dice(1, damageReflection, damageReflection/2);
			int damageReflected = Math.min(finalDamage, d.roll());

			// reverse the attacker and defender
			result.add(new DamageEvent(
				combat,
				attacker,
				defender,
				new DamagePacket(damageReflected, 1),
				type,
				subtype, 
				null,
				animationContext));
		}

		// check for BLOODTHIRSTY
		if (defender.getModifier(Stats.Modifier.BLOODTHIRSTY) > 0 &&
			defender.getHitPoints().getCurrent() > 0 &&
			defender.getHitPoints().getRatio() <= 0.2)
		{
			ConditionTemplate ct = Database.getInstance().getConditionTemplate("bloodthirsty");
			result.add(
				new ConditionEvent(
					defender,
					ct.create(
						defender,
						defender,
						defender.getLevel(),
						MagicSys.SpellEffectType.NONE,
						MagicSys.SpellEffectSubType.NONE)));
		}

		// check for BERSERK
		if (defender.getModifier(Stats.Modifier.BERSERKER) > 0 &&
			defender.getHitPoints().getCurrent() >= 0)
		{
			if (GameSys.getInstance().actorGoesBeserk(defender))
			{
				result.add(new BerserkEvent(defender));
			}
		}

		// character speech if badly wounded
		if (defender instanceof PlayerCharacter &&
			defender.getHitPoints().getCurrent() > 0 &&
			defender.getHitPoints().getRatio() <= 0.1)
		{
			result.addAll(SpeechUtil.getInstance().badlyWoundedSpeech((PlayerCharacter)defender));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		String s = finalDamage + " damage ("+ MagicSys.SpellEffectType.describe(type)+")";

		if (getDamagePacket().getMultiplier() > 1)
		{
			s += " (x"+getDamagePacket().getMultiplier()+")";
		}

		return s;
	}
}

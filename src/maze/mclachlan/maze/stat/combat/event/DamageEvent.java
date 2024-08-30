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

import java.awt.Color;
import java.awt.Rectangle;
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
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.BloodSplatAnimation;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DamageEvent extends MazeEvent
{
	private final Combat combat;
	private final UnifiedActor defender;
	private final UnifiedActor attacker;
	private final DamagePacket damagePacket;
	private final MagicSys.SpellEffectType type;
	private final MagicSys.SpellEffectSubType subtype;
	/** may be null in the case of spells/conditions */
	private final AttackWith attackWith;

	private int finalDamage;
	private CombatStatistics stats;
	private final AnimationContext animationContext;

	/** a bag of random other state carried along with the attack */
	private final Set<String> tags = new HashSet<String>();

	/*-------------------------------------------------------------------------*/
	public DamageEvent(
		Combat combat,
		UnifiedActor defender,
		UnifiedActor attacker,
		DamagePacket damagePacket,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subtype,
		AttackWith attackWith,
		AnimationContext animationContext,
		Collection<String> tags)
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

		if (tags != null)
		{
			this.tags.addAll(tags);
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
		final Maze maze = Maze.getInstance();

		// maybe this should all be moved this into GameSys?

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

		// animation
		Rectangle origination;
		if (defender instanceof PlayerCharacter)
		{
			origination = maze.getUi().getPlayerCharacterPortraitBounds((PlayerCharacter)defender);
		}
		else
		{
			origination = maze.getUi().getObjectBounds(((Foe)defender).getSprite());
		}
		Animation a = new BloodSplatAnimation(Color.RED,
			/*"-"+*/String.valueOf(finalDamage),
			origination,
			1000);
		Maze.getInstance().startAnimation(a, null, new AnimationContext(defender));

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
				// check for CHEAT_DEATH
				GameSys.getInstance().cheatDeath(defender);
				result.add(new ActorCheatsDeathEvent(defender));
			}
			else if (maze.getCurrentCombat() != null &&
				defender.getModifier(Stats.Modifier.DIE_HARD) > 0)
			{
				// check for DIE_HARD
				// set to 1 hp so as to keep fighting
				defender.getHitPoints().setCurrent(1);
				defender.getCombatantData().setDieHard(true);
				result.add(new UiMessageEvent(StringUtil.getEventText("msg.die.hard", defender.getDisplayName())));
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
					attackWith != null && !attackWith.isRanged())
				{
					Item dyingBlowAttackWith = defender.getPrimaryWeapon();
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
							dyingBlowAttackWith,
							GameSys.getInstance().getAttackType(dyingBlowAttackWith),
							0,
							1,
							dyingBlowAttackWith.getAttackScript(),
							dyingBlowAttackWith.getDefaultDamageType(),
							animationContext,
							null,
							Stats.Modifier.DYING_BLOW.toString()));
				}

				result.add(new ActorDiesEvent(defender, attacker));

				// check for MELEE_CLEAVE
				int meleeCleave = attacker.getModifier(Stats.Modifier.MELEE_CLEAVE);
				if (meleeCleave > 0 && attackWith != null && !attackWith.isRanged())
				{
					// only add an attack if this is the first one, or the modifier is >1
					if (meleeCleave > 2 ||
						!tags.contains(Stats.Modifier.MELEE_CLEAVE.toString()))
					{
						UnifiedActor defender = combat.getRandomFoeWithinRangeOf(attacker);

						result.add(
							new UiMessageEvent(
								StringUtil.getEventText(
									"msg.melee.cleave",
									attacker.getDisplayName())));
						result.add(
							new AttackEvent(
								combat,
								attacker,
								defender,
								attackWith,
								GameSys.getInstance().getAttackType(attackWith),
								0,
								1,
								attackWith.getAttackScript(),
								attackWith.getDefaultDamageType(),
								animationContext,
								null,
								Stats.Modifier.MELEE_CLEAVE.toString()));
					}
				}
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
			int damageReflected = Math.min(finalDamage, d.roll("razor cloak damage reflection"));

			// reverse the attacker and defender
			result.add(new DamageEvent(
				combat,
				attacker,
				defender,
				new DamagePacket(damageReflected, 1),
				type,
				subtype, 
				null,
				animationContext,
				null));
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

		if (defender.getModifier(Stats.Modifier.BATTLE_MASTER) > 0 && finalDamage > 0)
		{
			defender.getActionPoints().incCurrent(1);
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

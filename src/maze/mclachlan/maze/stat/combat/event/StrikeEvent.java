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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 * Resolves one strike
 */
public class StrikeEvent extends MazeEvent
{
	private Combat combat;
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private AttackWith attackWith;
	private AttackType attackType;
	private MagicSys.SpellEffectType damageType;
	private AnimationContext animationContext;
	private StatModifier modifiers;
	private BodyPart bodyPart;

	/*-------------------------------------------------------------------------*/
	public StrikeEvent(
		Combat combat,
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackWith weapon,
		AttackType attackType,
		MagicSys.SpellEffectType damageType,
		AnimationContext animationContext,
		StatModifier modifiers)
	{
		this.combat = combat;
		this.attacker = attacker;
		this.defender = defender;
		this.attackWith = weapon;
		this.attackType = attackType;
		this.damageType = damageType;
		this.animationContext = animationContext;
		this.modifiers = modifiers;
	}
	
	/*-------------------------------------------------------------------------*/
	public UnifiedActor getAttacker()
	{
		return attacker;
	}

	public UnifiedActor getDefender()
	{
		return defender;
	}

	public AttackWith getAttackWith()
	{
		return attackWith;
	}

	public AttackType getAttackType()
	{
		return attackType;
	}

	public int getDelay()
	{
		return 0;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// check for early exit - maybe the defender was killed already
		if (!defender.isAlive())
		{
			return null;
		}

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// Determine random body part targeted
		bodyPart = getRandomBodyPart(attacker, defender);

		// Deduct ammo if required. We assume that the ammo type matches
		if (attackWith.getAmmoRequired() != null)
		{
			this.attacker.deductAmmo(this);
		}

		// Determine hit or miss
		int hitPercent = GameSys.getInstance().calcHitPercent(this);
		if (Dice.d100.roll() <= hitPercent)
		{
			DamagePacket damagePacket = GameSys.getInstance().calcDamage(this);
			combat.getCombatStatistics().captureAttackHit(this, combat);

			if (GameSys.getInstance().isSurpriseRiposted(defender, attackWith))
			{
				// equip hidden blade and riposte
				result.addAll(
					ActorActionResolver.resolveAction(
						getHiddenBladeSpecialAbilityAction(),
						combat));
				result.add(new AttackRipostedEvent(attacker, defender, combat, animationContext));
			}
			else if (GameSys.getInstance().isSurpriseParried(defender, attackWith))
			{
				// equip hidden blade and parry
				result.addAll(
					ActorActionResolver.resolveAction(
						getHiddenBladeSpecialAbilityAction(),
						combat));
				result.add(new AttackParriedEvent(attacker, defender));
			}
			else if (GameSys.getInstance().isAttackDodged(attacker, defender, attackWith))
			{
				// dodge the attack
				result.add(new AttackDodgeEvent(defender));
			}
			else if (GameSys.getInstance().isAttackDeflected(attacker, defender, attackWith))
			{
				// deflected
				result.add(new AttackDeflectedEvent(attacker, defender, bodyPart));
			}
			else if (GameSys.getInstance().isAttackParried(defender, attackWith))
			{
				// parried
				result.add(new AttackParriedEvent(attacker, defender));
			}
			else if (GameSys.getInstance().isAttackRiposted(defender, attackWith))
			{
				// riposte
				result.add(new AttackRipostedEvent(attacker, defender, combat, animationContext));
			}
			else
			{
				result.add(new AttackHitEvent(
					attacker,
					defender,
					bodyPart));

				result.add(new DamageEvent(
					combat,
					defender,
					attacker,
					damagePacket,
					damageType,
					MagicSys.SpellEffectSubType.NORMAL_DAMAGE,
					attackWith,
					animationContext));

				// apply any spell effects to the victim
				if (damagePacket.getAmount() > 0)
				{
					List<AttackSpellEffects> effects =
						GameSys.getInstance().getAttackSpellEffects(this);

					if (effects != null && effects.size() > 0)
					{
						for (AttackSpellEffects ase : effects)
						{
							if (ase.getSpellEffects() != null && ase.getSpellEffects().size() > 0)
							{
								result.addAll(
									SpellTargetUtils.applySpellToUnwillingVictim(
										ase.getSpellEffects(),
										defender,
										attacker,
										ase.getCastingLevel(),
										ase.getSpellLevel(),
										animationContext));
							}
						}
					}
				}
			}
		}
		else
		{
			// attack missed
			result.add(new AttackMissEvent(attacker, defender));
			combat.getCombatStatistics().captureAttackMiss(this, combat);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public SpecialAbilityAction getHiddenBladeSpecialAbilityAction()
	{
		// the default
		Spell spell = Database.getInstance().getSpell("Hidden Blade 1");

		// find a hidden_blade special ability
		SpellLikeAbility ability = new SpellLikeAbility(
			spell,
			new Value(defender.getCurrentClassLevel(), Value.SCALE.NONE));
		List<SpellLikeAbility> spellLikeAbilities = this.defender.getSpellLikeAbilities();
		for (SpellLikeAbility sla : spellLikeAbilities)
		{
			if (sla.getName().startsWith("Hidden Blade"))
			{
				ability = sla;
				break;
			}
		}

		SpecialAbilityAction result = new SpecialAbilityAction(
			ability.getSpell().getDescription(),
			defender,
			ability.getSpell(),
			ability.getCastingLevel().compute(defender));

		result.setActor(defender);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public SpellLikeAbility getHiddenBlade(UnifiedActor defender)
	{
		// the default
		Spell spell = Database.getInstance().getSpell("Hidden Blade 1");

		// find a hidden_blade special ability
		SpellLikeAbility ability = new SpellLikeAbility(
			spell,
			new Value(defender.getCurrentClassLevel(), Value.SCALE.NONE));
		List<SpellLikeAbility> spellLikeAbilities = this.defender.getSpellLikeAbilities();
		for (SpellLikeAbility sla : spellLikeAbilities)
		{
			if (sla.getName().startsWith("Hidden Blade"))
			{
				ability = sla;
				break;
			}
		}
		return ability;
	}

	/*-------------------------------------------------------------------------*/
	private static BodyPart getRandomBodyPart(UnifiedActor attacker, UnifiedActor defender)
	{
		if (attacker instanceof Foe)
		{
			if (defender instanceof PlayerCharacter)
			{
				// foe attacks PC
				PlayerCharacter pc = (PlayerCharacter)defender;
				String bodyPart = ((Foe)attacker).getPlayerBodyParts().getRandomItem();
				if (PlayerCharacter.BodyParts.HEAD.equals(bodyPart)) return pc.getRace().getHead();
				else if (PlayerCharacter.BodyParts.TORSO.equals(bodyPart)) return pc.getRace().getTorso();
				else if (PlayerCharacter.BodyParts.LEG.equals(bodyPart)) return pc.getRace().getLeg();
				else if (PlayerCharacter.BodyParts.HAND.equals(bodyPart)) return pc.getRace().getHand();
				else if (PlayerCharacter.BodyParts.FOOT.equals(bodyPart)) return pc.getRace().getFoot();
				else
				{
					throw new MazeException("Invalid body part ["+bodyPart+"]");
				}
			}
			else
			{
				// foe attacks foe
				return ((Foe)defender).getBodyParts().getRandomItem();
			}
		}
		else
		{
			if (defender instanceof Foe)
			{
				// PC attacks foe
				return ((Foe)defender).getBodyParts().getRandomItem();
			}
			else
			{
				// PC attacks PC
				PlayerCharacter pc = (PlayerCharacter)defender;

				PercentageTable<BodyPart> table = new PercentageTable<BodyPart>(true);

				table.add(pc.getRace().getHead(), 25);
				table.add(pc.getRace().getTorso(), 33);
				table.add(pc.getRace().getLeg(), 26);
				table.add(pc.getRace().getHand(), 8);
				table.add(pc.getRace().getFoot(), 8);

				return table.getRandomItem();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return null;
	}

	public BodyPart getBodyPart()
	{
		return bodyPart;
	}
}

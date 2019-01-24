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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.combat.event.FumblesEvent;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 * A spell result that involves an attack with the character's equipped
 * weapon(s).
 */
public class AttackWithWeaponSpellResult extends SpellResult
{
	private ValueList nrStrikes;
	private StatModifier modifiers;
	private AttackType attackType;
	private MagicSys.SpellEffectType damageType;
	private String attackScript;
	private boolean requiresBackstabWeapon;
	private boolean requiresSnipeWeapon;
	private boolean consumesWeapon;
	private int requiredWeaponType;
	private GroupOfPossibilities<String> spellEffects;

	/*-------------------------------------------------------------------------*/

	public AttackWithWeaponSpellResult(
		ValueList nrStrikes,
		StatModifier modifiers,
		AttackType attackType,
		MagicSys.SpellEffectType damageType,
		String attackScript,
		boolean requiresBackstabWeapon,
		boolean requiresSnipeWeapon,
		boolean consumesWeapon,
		int requiredWeaponType,
		GroupOfPossibilities<String> spellEffects)
	{
		this.nrStrikes = nrStrikes;
		this.modifiers = modifiers;
		this.attackType = attackType;
		this.damageType = damageType;
		this.attackScript = attackScript;
		this.requiresBackstabWeapon = requiresBackstabWeapon;
		this.requiresSnipeWeapon = requiresSnipeWeapon;
		this.consumesWeapon = consumesWeapon;
		this.requiredWeaponType = requiredWeaponType;
		this.spellEffects = spellEffects;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(
		UnifiedActor spellSource,
		UnifiedActor spellTarget,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		Combat combat = Maze.getInstance().getCurrentCombat();
		if (combat == null)
		{
			return null;
		}

		UnifiedActor source;
		UnifiedActor target;

		// If the source and target are enemies, source attacks target
		// If the source and target are allies, target attacks an enemy group in range

		if (combat.getAllAlliesOf(spellSource).contains(spellTarget))
		{
			source = spellTarget;
			target = combat.getRandomFoeWithinRangeOf(spellTarget);
		}
		else
		{
			source = spellSource;
			target = spellTarget;
		}

		Maze.log(Log.DEBUG, "Attack with weapon spell result, source=["+
			source.getName()+"], target=["+target.getName()+"]");

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// decide the weapon to attack with
		AttackWith weapon = getAttackWith(source);

		// meets requirements? If not, it's a fumble.
		if (!meetsRequirements(source))
		{
			result.add(new FumblesEvent(source));
			return result;
		}

		// apply the damage type
		MagicSys.SpellEffectType damageTypeToUse = damageType;
		if (damageTypeToUse == null || damageTypeToUse == MagicSys.SpellEffectType.NONE)
		{
			damageTypeToUse = GameSys.getInstance().getAttackWithDamageType(source, weapon);
		}
		MagicSys.SpellEffectType actionDamageType = damageTypeToUse;

		// apply the nr of strikes
		int actionNrStrikes = nrStrikes.compute(spellSource, castingLevel);

		// select the attack script
		MazeScript actionAttackScript = getAttackScript(weapon);

		// create the attack action
		AttackAction action = getAttackAction(
			source,
			target,
			combat,
			weapon,
			actionDamageType,
			actionNrStrikes,
			actionAttackScript);

		// resolve the attack
		result.addAll(
			ActorActionResolver.attack(
				combat,
				source,
				target,
				action,
				new AnimationContext(source)));

		// consume the weapon?
		if (consumesWeapon)
		{
			result.add(
				new RemoveItemEvent(weapon.getName(), source));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor source)
	{
		AttackWith weapon = getAttackWith(source);

		// backstab requirement?
		if (requiresBackstabWeapon && !weapon.isBackstabCapable() ||
			requiresSnipeWeapon && !weapon.isSnipeCapable())
		{
			Maze.log(Log.DEBUG, source.getName()+" - weapon is not backstab/snipe capable");
			return false;
		}

		// ammo requirement?
		Item secondaryWeapon = source.getSecondaryWeapon();
		if (weapon.isRanged() && weapon.getAmmoRequired() != null &&
			(secondaryWeapon == null || !weapon.getAmmoRequired().contains(secondaryWeapon.isAmmoType())))
		{
			Maze.log(Log.DEBUG, source.getName()+" - no ammo to snipe");
			return false;
		}

		// weapon type requirement?
		if (requiredWeaponType != ItemTemplate.WeaponSubType.NONE &&
			requiredWeaponType != weapon.getWeaponType())
		{
			Maze.log(Log.DEBUG, source.getName()+" - not the right weapon type");
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private AttackAction getAttackAction(
		UnifiedActor source,
		UnifiedActor target,
		Combat combat,
		AttackWith weapon,
		MagicSys.SpellEffectType actionDamageType,
		int actionNrStrikes,
		MazeScript actionAttackScript)
	{
		AttackType actionAttackType = this.attackType;

		if (actionAttackType == null)
		{
			actionAttackType = GameSys.getInstance().getAttackType(weapon);
		}

		AttackAction action = new AttackAction(
			combat.getActorGroup(target),
			weapon,
			actionNrStrikes,
			actionAttackScript,
			true,
			actionAttackType,
			actionDamageType);
		action.setActor(source);

		// apply any modifiers to this attack;
		if (modifiers != null)
		{
			action.setModifiers(new StatModifier(modifiers));
		}
		return action;
	}

	/*-------------------------------------------------------------------------*/
	private MazeScript getAttackScript(AttackWith weapon)
	{
		MazeScript actionAttackScript;
		if (attackScript == null)
		{
			actionAttackScript = weapon.getAttackScript();
		}
		else
		{
			actionAttackScript = Database.getInstance().getScript(attackScript);
		}
		return actionAttackScript;
	}

	/*-------------------------------------------------------------------------*/
	private AttackWith getAttackWith(UnifiedActor source)
	{
		AttackWith weapon = source.getPrimaryWeapon();
		if (weapon == null)
		{
			weapon = GameSys.getInstance().getUnarmedWeapon(source, true);
		}
		return new AttackWithProxy(weapon, getSpellEffects());
	}

	/*-------------------------------------------------------------------------*/

	public ValueList getNrStrikes()
	{
		return nrStrikes;
	}

	public void setNrStrikes(ValueList nrStrikes)
	{
		this.nrStrikes = nrStrikes;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public MagicSys.SpellEffectType getDamageType()
	{
		return damageType;
	}

	public void setDamageType(MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
	}

	public String getAttackScript()
	{
		return attackScript;
	}

	public void setAttackScript(String attackScript)
	{
		this.attackScript = attackScript;
	}

	public boolean isRequiresBackstabWeapon()
	{
		return requiresBackstabWeapon;
	}

	public void setRequiresBackstabWeapon(boolean requiresBackstabWeapon)
	{
		this.requiresBackstabWeapon = requiresBackstabWeapon;
	}

	public boolean isRequiresSnipeWeapon()
	{
		return requiresSnipeWeapon;
	}

	public void setRequiresSnipeWeapon(boolean requiresSnipeWeapon)
	{
		this.requiresSnipeWeapon = requiresSnipeWeapon;
	}

	public AttackType getAttackType()
	{
		return attackType;
	}

	public void setAttackType(AttackType attackType)
	{
		this.attackType = attackType;
	}

	public int getRequiredWeaponType()
	{
		return requiredWeaponType;
	}

	public void setRequiredWeaponType(int requiredWeaponType)
	{
		this.requiredWeaponType = requiredWeaponType;
	}

	public boolean isConsumesWeapon()
	{
		return consumesWeapon;
	}

	public void setConsumesWeapon(boolean consumesWeapon)
	{
		this.consumesWeapon = consumesWeapon;
	}

	public GroupOfPossibilities<String> getSpellEffects()
	{
		return spellEffects;
	}

	public void setSpellEffects(GroupOfPossibilities<String> spellEffects)
	{
		this.spellEffects = spellEffects;
	}

	/*-------------------------------------------------------------------------*/
	public static class AttackWithProxy implements AttackWith
	{
		private AttackWith weapon;
		private GroupOfPossibilities<SpellEffect> spellEffects;

		public AttackWithProxy(
			AttackWith weapon, GroupOfPossibilities<String> extraSpellEffects)
		{
			this.weapon = weapon;

			spellEffects = new GroupOfPossibilities<SpellEffect>();
			spellEffects.addAll(weapon.getSpellEffects());
			if (extraSpellEffects != null)
			{
				for (String s : extraSpellEffects.getPossibilities())
				{
					spellEffects.add(
						Database.getInstance().getSpellEffect(s),
						extraSpellEffects.getPercentage(s));
				}
			}
		}

		@Override
		public String getName()
		{
			return weapon.getName();
		}

		@Override
		public String getDisplayName()
		{
			return weapon.getDisplayName();
		}

		@Override
		public int getToHit()
		{
			return weapon.getToHit();
		}

		@Override
		public int getToPenetrate()
		{
			return weapon.getToPenetrate();
		}

		@Override
		public int getToCritical()
		{
			return weapon.getToCritical();
		}

		@Override
		public Dice getDamage()
		{
			return weapon.getDamage();
		}

		@Override
		public MagicSys.SpellEffectType getDefaultDamageType()
		{
			return weapon.getDefaultDamageType();
		}

		@Override
		public String describe(AttackEvent e)
		{
			return weapon.describe(e);
		}

		@Override
		public String[] getAttackTypes()
		{
			return weapon.getAttackTypes();
		}

		@Override
		public int getMaxRange()
		{
			return weapon.getMaxRange();
		}

		@Override
		public int getMinRange()
		{
			return weapon.getMinRange();
		}

		@Override
		public boolean isRanged()
		{
			return weapon.isRanged();
		}

		@Override
		public boolean isBackstabCapable()
		{
			return weapon.isBackstabCapable();
		}

		@Override
		public boolean isSnipeCapable()
		{
			return weapon.isSnipeCapable();
		}

		@Override
		public GroupOfPossibilities<SpellEffect> getSpellEffects()
		{
			return spellEffects;
		}

		@Override
		public int getSpellEffectLevel()
		{
			return weapon.getSpellEffectLevel();
		}

		@Override
		public TypeDescriptor slaysFoeType()
		{
			return weapon.slaysFoeType();
		}

		@Override
		public MazeScript getAttackScript()
		{
			return weapon.getAttackScript();
		}

		@Override
		public ItemTemplate.AmmoType isAmmoType()
		{
			return weapon.isAmmoType();
		}

		@Override
		public List<ItemTemplate.AmmoType> getAmmoRequired()
		{
			return weapon.getAmmoRequired();
		}

		@Override
		public int getToInitiative()
		{
			return weapon.getToInitiative();
		}

		@Override
		public int getWeaponType()
		{
			return weapon.getWeaponType();
		}
	}
}

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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ActorIntentionResolver
{
	public static List<CombatAction> getCombatActions(
		UnifiedActor actor,
		ActorActionIntention intention)
	{
		Maze maze = Maze.getInstance();
		List<CombatAction> result = new ArrayList<>();

		if (actor.getHitPoints().getCurrent() <= 0)
		{
			return result;
		}
		else if (intention instanceof AttackIntention)
		{
			result.addAll(resolveAttackIntention(actor, (AttackIntention)intention));
		}
		else if (intention instanceof DefendIntention)
		{
			result.add(new DefendAction());
		}
		else if (intention instanceof HideIntention)
		{
			result.add(new HideAction());
		}
		else if (intention instanceof SpellIntention)
		{
			SpellIntention si = (SpellIntention)intention;

			result.add(new SpellAction(si.getTarget(),
				si.getSpell(),
				si.getCastingLevel()));
		}
		else if (intention instanceof SpecialAbilityIntention)
		{
			SpecialAbilityIntention si = (SpecialAbilityIntention)intention;

			result.add(new SpecialAbilityAction(
				si.getSpell().getDescription(),
				si.getTarget(),
				si.getSpell(),
				si.getCastingLevel()));
		}
		else if (intention instanceof UseItemIntention)
		{
			UseItemIntention ui = (UseItemIntention)intention;

			Item item = ui.getItem();

			result.add(new UseItemAction(item, ui.getTarget()));
		}
		else if (intention instanceof EquipIntention)
		{
			result.add(new EquipAction());
		}
		else if (intention instanceof RunAwayIntention)
		{
			result.add(new RunAwayAction());
		}
		else if (intention instanceof ThreatenIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new ThreatenAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof TalkIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new TalkAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof GiveIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new GiveAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof BribeIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new BribeAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof StealIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new StealAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof TradeIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				result.add(new TradeAction(maze.getCurrentActorEncounter().getLeader()));
			}
		}
		else if (intention instanceof DisarmTrapIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_CHEST)
			{
				result.add(new DisarmTrapAction(maze.getCurrentChest()));
			}
		}
		else if (intention instanceof OpenChestIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_CHEST)
			{
				result.add(new OpenChestAction(maze.getCurrentChest()));
			}
		}
		else if (intention instanceof PickLockIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_PORTAL)
			{
				result.add(new PickLockAction(maze.getCurrentPortal()));
			}
		}
		else if (intention instanceof ForceOpenIntention)
		{
			if (maze.getState() == Maze.State.ENCOUNTER_PORTAL)
			{
				result.add(new ForceOpenAction(maze.getCurrentPortal()));
			}
		}
		else
		{
			throw new MazeException("Unrecognised combat intention: " + intention);
		}

		for (CombatAction ca : result)
		{
			ca.setActor(actor);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<CombatAction> resolveAttackIntention(UnifiedActor actor,
		AttackIntention intention)
	{
		List<CombatAction> result = new ArrayList<>();

		ActorGroup targetGroup = ((AttackIntention)intention).getActorGroup();
		AttackWith attackWith = ((AttackIntention)intention).getAttackWith();

		if (attackWith == null || attackWith instanceof Item || attackWith instanceof BackstabSnipeAttack)
		{
			resolveWeaponAttack(actor, result, targetGroup, attackWith);
		}
		else if (attackWith instanceof NaturalWeapon)
		{
			resolveNaturalWeaponAttack(actor, result, targetGroup, attackWith);
		}
		else
		{
			throw new MazeException("invalid attackWith "+attackWith);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveNaturalWeaponAttack(
		UnifiedActor actor,
		List<CombatAction> result,
		ActorGroup targetGroup,
		AttackWith attackWith)
	{
		// basic attack with primary weapon, no modifiers
		int nrAttacks = GameSys.getInstance().getNrAttacks(actor, true);

		if (attackWith.getAmmoRequired() == null
			|| attackWith.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF)
			|| actor.getSecondaryWeapon() != null &&
			attackWith.getAmmoRequired().contains(actor.getSecondaryWeapon().isAmmoType()))
		{
			MazeScript missileScript;
			if (attackWith.isRanged() && actor.getSecondaryWeapon() != null)
			{
				missileScript = actor.getSecondaryWeapon().getAttackScript();
			}
			else
			{
				missileScript = attackWith.getAttackScript();
			}

			for (int i = 0; i < nrAttacks; i++)
			{
				// ammo requirements ok.  Attack
				MagicSys.SpellEffectType defaultDamageType = attackWith.getDefaultDamageType();
				if (attackWith.getAmmoRequired() != null &&
					actor.getSecondaryWeapon() != null &&
					attackWith.getAmmoRequired().contains((actor.getSecondaryWeapon()).isAmmoType()))
				{
					defaultDamageType = actor.getSecondaryWeapon().getDefaultDamageType();
				}

				AttackAction action = new AttackAction(
					targetGroup,
					attackWith,
					-1,
					missileScript,
					true,
					GameSys.getInstance().getAttackType(attackWith),
					defaultDamageType);
				action.setModifier(Stats.Modifier.INITIATIVE, -5 * i + attackWith.getToInitiative());

				result.add(action);
			}
		}
		else
		{
			// cannot attack
			result.add(new DefendAction());
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveWeaponAttack(
		UnifiedActor actor,
		List<CombatAction> result,
		ActorGroup targetGroup,
		AttackWith attackWith)
	{
		Maze.log(Log.DEBUG, "resolve weapon attack ["+actor.getName()+"]");

		boolean canAttackWithPrimary = canAttackWithPrimary(actor);
		Maze.log(Log.DEBUG, "canAttackWithPrimary ["+canAttackWithPrimary+"]");

		boolean canAttackWithSecondary = canAttackWithSecondary(actor);
		Maze.log(Log.DEBUG, "canAttackWithSecondary ["+canAttackWithSecondary+"]");

		AttackWith primaryAttackWith = attackWith;
		if (actor.getPrimaryWeapon() == null)
		{
			// actor has been disarmed at some time before event resolution
			primaryAttackWith = GameSys.getInstance().getUnarmedWeapon(actor, true);
		}

		// primary weapon
		if (canAttackWithPrimary)
		{
			// basic attack with primary weapon, no modifiers
			int nrAttacks = GameSys.getInstance().getNrAttacks(actor, true);

			if (!requiresAmmo(primaryAttackWith)
				|| primaryAttackWith.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF)
				|| (actor.getSecondaryWeapon() != null && primaryAttackWith.getAmmoRequired().contains(actor.getSecondaryWeapon().isAmmoType())))
			{
				MazeScript missileScript;
				if (primaryAttackWith.isRanged() && actor.getSecondaryWeapon() != null)
				{
					missileScript = actor.getSecondaryWeapon().getAttackScript();
				}
				else
				{
					missileScript = primaryAttackWith.getAttackScript();
				}

				for (int i = 0; i < nrAttacks; i++)
				{
					// ammo requirements ok.  Attack
					MagicSys.SpellEffectType defaultDamageType = primaryAttackWith.getDefaultDamageType();
					if (primaryAttackWith.getAmmoRequired() != null &&
						actor.getSecondaryWeapon() != null &&
						primaryAttackWith.getAmmoRequired().contains((actor.getSecondaryWeapon()).isAmmoType()))
					{
						defaultDamageType = actor.getSecondaryWeapon().getDefaultDamageType();
					}

					AttackAction action = new AttackAction(
						targetGroup,
						primaryAttackWith,
						-1,
						missileScript,
						true,
						GameSys.getInstance().getAttackType(primaryAttackWith),
						defaultDamageType);
					action.setModifier(Stats.Modifier.INITIATIVE, -5 * i + primaryAttackWith.getToInitiative());
					if (canAttackWithSecondary && actor.getSecondaryWeapon() != null)
					{
						GameSys.getInstance().setDualWeaponPenalties(action, actor, true);
					}
					result.add(action);
				}
			}
			else
			{
				// cannot attack
				result.add(new DefendAction());
				//todo: return from here?
			}
		}

		if (canAttackWithSecondary)
		{
			Item secondaryAttackWith;
			if (actor.getSecondaryWeapon() != null)
			{
				secondaryAttackWith = actor.getSecondaryWeapon();
			}
			else
			{
				secondaryAttackWith = GameSys.getInstance().getUnarmedWeapon(actor, true);
			}

			int nrAttacks = GameSys.getInstance().getNrAttacks(actor, false);

			for (int i = 0; i < nrAttacks; i++)
			{
				AttackAction secAction = new AttackAction(
					targetGroup,
					secondaryAttackWith,
					-1,
					secondaryAttackWith.getAttackScript(),
					true,
					GameSys.getInstance().getAttackType(primaryAttackWith),
					secondaryAttackWith.getDefaultDamageType());
				secAction.setModifier(Stats.Modifier.INITIATIVE, -5 * (i + 1) + primaryAttackWith.getToInitiative());
				if (actor.getSecondaryWeapon() != null)
				{
					// dual weapon penalties do not apply to unarmed combat
					GameSys.getInstance().setDualWeaponPenalties(secAction, actor, false);
				}
				result.add(secAction);
			}
		}
	}

	private static boolean requiresAmmo(AttackWith aw)
	{
		return aw.getAmmoRequired() != null && aw.getAmmoRequired().size() > 0;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean canAttackWithSecondary(UnifiedActor actor)
	{
		Item secondaryWeapon = actor.getSecondaryWeapon();
		if (secondaryWeapon != null && secondaryWeapon.isWeapon())
		{
			// must be a weapon
			if (secondaryWeapon.getAmmoRequired() == null)
			{
				// no ammo requirement, can attack
				return true;
			}
			else if (secondaryWeapon.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF))
			{
				// stacked throwing item, can secondary attack
				return true;
			}
		}
		else if (actor.getModifier(Stats.Modifier.MARTIAL_ARTS) > 0)
		{
			// no secondary weapon, but has martial arts = secondary unarmed attack
			return true;
		}

		// fall through to here - no secondary attack
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean canAttackWithPrimary(UnifiedActor actor)
	{
		Item primaryWeapon = actor.getPrimaryWeapon();
		if (primaryWeapon == null)
		{
			// can always attack unarmed
			return true;
		}
		else
		{
			// needs to be a weapon
			if (primaryWeapon.isWeapon())
			{
				if (!requiresAmmo(primaryWeapon))
				{
					// no ammo requires - can attack
					return true;
				}
				else
				{
					// check if secondary is compatible ammo
					if (primaryWeapon.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF) ||
						(actor.getSecondaryWeapon() != null && primaryWeapon.getAmmoRequired().contains(actor.getSecondaryWeapon().isAmmoType())))
					{
						return true;
					}
				}
			}
		}

		// fall through to here, means cannot attack
		return false;
	}
}

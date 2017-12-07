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
		List<CombatAction> result = new ArrayList<CombatAction>();

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

			result.add(new UseItemAction(item,
				ui.getTarget()));
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
		List<CombatAction> result = new ArrayList<CombatAction>();

		AttackIntention atkInt = (AttackIntention)intention;

		ActorGroup targetGroup = atkInt.getActorGroup();
		AttackWith attackWith = atkInt.getAttackWith();

		if (attackWith == null || attackWith instanceof Item)
		{
			resolveWeaponAttack(actor, result, targetGroup);
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
			if (attackWith.isRanged())
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
	public static void resolveWeaponAttack(UnifiedActor actor,
		List<CombatAction> result, ActorGroup targetGroup)
	{
		boolean canAttackWithPrimary =
			actor.getPrimaryWeapon() == null ||
				(actor.getPrimaryWeapon() != null && actor.getPrimaryWeapon().isWeapon());

		boolean canAttackWithSecondary =
			actor.getSecondaryWeapon() != null &&
				actor.getSecondaryWeapon().isWeapon() &&
				actor.getPrimaryWeapon().getAmmoRequired() != null &&
				actor.getPrimaryWeapon().getAmmoRequired().contains(actor.getSecondaryWeapon().isAmmoType())
				||
				actor.getSecondaryWeapon() == null && actor.getModifier(Stats.Modifier.MARTIAL_ARTS) > 0;

		Item weapon;
		if (actor.getPrimaryWeapon() != null)
		{
			weapon = actor.getPrimaryWeapon();
		}
		else
		{
			weapon = GameSys.getInstance().getUnarmedWeapon(actor, true);
		}

		// primary weapon
		if (canAttackWithPrimary)
		{
			// basic attack with primary weapon, no modifiers
			int nrAttacks = GameSys.getInstance().getNrAttacks(actor, true);

			if (weapon.getAmmoRequired() == null
				|| weapon.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF)
				|| actor.getSecondaryWeapon() != null &&
				weapon.getAmmoRequired().contains(actor.getSecondaryWeapon().isAmmoType()))
			{
				MazeScript missileScript;
				if (weapon.isRanged())
				{
					missileScript = actor.getSecondaryWeapon().getAttackScript();
				}
				else
				{
					missileScript = weapon.getAttackScript();
				}

				for (int i = 0; i < nrAttacks; i++)
				{
					// ammo requirements ok.  Attack
					MagicSys.SpellEffectType defaultDamageType = weapon.getDefaultDamageType();
					if (weapon.getAmmoRequired() != null &&
						actor.getSecondaryWeapon() != null &&
						weapon.getAmmoRequired().contains((actor.getSecondaryWeapon()).isAmmoType()))
					{
						defaultDamageType = actor.getSecondaryWeapon().getDefaultDamageType();
					}

					AttackAction action = new AttackAction(
						targetGroup,
						weapon,
						-1,
						missileScript,
						true,
						GameSys.getInstance().getAttackType(weapon),
						defaultDamageType);
					action.setModifier(Stats.Modifier.INITIATIVE, -5 * i + weapon.getToInitiative());
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
			Item attackWith;
			if (actor.getSecondaryWeapon() != null)
			{
				attackWith = actor.getSecondaryWeapon();
			}
			else
			{
				attackWith = weapon;
			}

			// basic attack with secondary weapon:
			// -5 intiative
			// -5 to hit
			int nrAttacks = GameSys.getInstance().getNrAttacks(actor, false);

			for (int i = 0; i < nrAttacks; i++)
			{
				AttackAction secAction = new AttackAction(
					targetGroup,
					attackWith,
					-1,
					attackWith.getAttackScript(),
					true,
					GameSys.getInstance().getAttackType(weapon),
					attackWith.getDefaultDamageType());
				secAction.setModifier(Stats.Modifier.INITIATIVE, -5 * (i + 1) + weapon.getToInitiative());
				if (actor.getSecondaryWeapon() != null)
				{
					// dual weapon penalties do not apply to unarmed combat
					GameSys.getInstance().setDualWeaponPenalties(secAction, actor, false);
				}
				result.add(secAction);
			}
		}
	}
}

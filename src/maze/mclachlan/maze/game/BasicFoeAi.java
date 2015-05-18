/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;

/**
 * Foe AI that simply picks a random attack with a random legal target, or
 * an appropriate stealth action.
 */
public class BasicFoeAi extends FoeCombatAi
{
	/*-------------------------------------------------------------------------*/

	@Override
	public ActorActionIntention getCombatIntention(Foe foe, Combat combat)
	{
		if (!foe.isSummoned() && Dice.d100.roll() <= foe.getFleeChance())
		{
			// summoned foes never run away
			return new RunAwayIntention();
		}

		int engagementRange = combat.getFoeEngagementRange(foe);
		int possibleGroups = combat.getNrOfEnemyGroups(foe);
		Dice possDice = new Dice(1, possibleGroups, -1);

		if (foe.getStealthBehaviour() == Foe.StealthBehaviour.STEALTH_RELIANT &&
			foe.getActionPoints().getRatio() < 0.5)
		{
			// stealth foe, needs to hide
			return new HideIntention();
		}

		if (foe.getStealthBehaviour() == Foe.StealthBehaviour.OPPORTUNISTIC &&
			foe.getActionPoints().getCurrent() < 10 &&
			Dice.d2.roll() == 1)
		{
			// opportunistic hide attempt
			return new HideIntention();
		}

		// just attack, if possible
		// todo: spells, SLAs
		if (foe.canAttack(engagementRange))
		{
			// pick a random attack
			List<AttackWith> attackWithOptions = foe.getAttackWithOptions();

			AttackWith aw;
			do
			{
				aw = attackWithOptions.get(Dice.nextInt(attackWithOptions.size()));
			}
			while (!foe.isLegalAttack(aw,engagementRange));

			// pick a random enemy group
			List<ActorGroup> foesOf = combat.getFoesOf(foe);
			ActorGroup group = foesOf.get(Dice.nextInt(foesOf.size()));

			return new AttackIntention(group, combat, aw);
		}
		else
		{
			return new DefendIntention();
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellTarget chooseTarget(Foe foe, /*FoeAttack.FoeAttackSpell spell,*/ Combat combat)
	{
		// todo

		return null;

		/*SpellTarget target;
		switch (spell.getSpell().getTargetType())
		{
			case MagicSys.SpellTargetType.ALL_FOES:
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				target = null;
				break;

			case MagicSys.SpellTargetType.PARTY:
				target = combat.getActorGroup(foe);
				break;

			case MagicSys.SpellTargetType.TILE:
				target = null;
				break;

			case MagicSys.SpellTargetType.CASTER:
				target = foe;
				break;

			case MagicSys.SpellTargetType.ALLY:
				List<UnifiedActor> allies = combat.getAllAlliesOf(foe);
				Dice d = new Dice(1, allies.size(), -1);
				target = allies.get(d.roll());
				break;

			case MagicSys.SpellTargetType.FOE:
				List<UnifiedActor> enemies = combat.getAllFoesOf(foe);
				d = new Dice(1, enemies.size(), -1);
				target = enemies.get(d.roll());
				break;

			case MagicSys.SpellTargetType.FOE_GROUP:
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				List<ActorGroup> groups = combat.getFoesOf(foe);
				d = new Dice(1, groups.size(), -1);
				target = groups.get(d.roll());
				break;

			// these should never really be cast be foes
			case MagicSys.SpellTargetType.ITEM:
			case MagicSys.SpellTargetType.NPC:
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				target = null;
				break;

			default: throw new MazeException("Invalid target type: "+
				spell.getSpell().getTargetType());
		}
		return target;*/
	}
}

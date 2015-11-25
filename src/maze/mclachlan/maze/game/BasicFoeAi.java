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
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.util.MazeException;

/**
 * Foe AI that simply picks a random attack with a random legal target, or
 * an appropriate stealth action.
 */
public class BasicFoeAi extends FoeCombatAi
{
	/*-------------------------------------------------------------------------*/
	public boolean shouldEvade(Foe foe, List<FoeGroup> groups, PlayerParty party)
	{
		switch (foe.getEvasionBehaviour())
		{
			case Foe.EvasionBehaviour.ALWAYS_EVADE:
				return true;
			case Foe.EvasionBehaviour.NEVER_EVADE:
				return false;
			case Foe.EvasionBehaviour.RANDOM_EVADE:
				return Dice.d2.roll() == 1;
			case Foe.EvasionBehaviour.CLEVER_EVADE:
				//
				// some heuristics to decide if they should attack
				//
				int foeStrength = 0;
				for (FoeGroup fg : groups)
				{
					for (UnifiedActor a : fg.getActors())
					{
						foeStrength += a.getLevel();
					}
				}

				int partyStrength = 0;
				for (UnifiedActor a : party.getActors())
				{
					partyStrength += a.getLevel();
				}

				return foeStrength >= partyStrength;
			default:
				throw new MazeException("Invalid evasion behaviour: "+
					foe.getEvasionBehaviour());
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ActorActionIntention getCombatIntention(Foe foe, Combat combat)
	{
		if (!foe.isSummoned() && Dice.d100.roll() <= foe.getFleeChance())
		{
			// summoned foes never run away
			return new RunAwayIntention();
		}

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

		PercentageTable<ActorActionIntention> perc = new PercentageTable<ActorActionIntention>();

		int attackWeight;
		int stealthWeight;
		int magicWeight;

		switch (foe.getFocus())
		{
			case COMBAT:
				attackWeight = 70;
				stealthWeight = 15;
				magicWeight = 15;
				break;
			case STEALTH:
				attackWeight = 45;
				stealthWeight = 40;
				magicWeight = 15;
				break;
			case MAGIC:
				attackWeight = 5;
				stealthWeight = 15;
				magicWeight = 80;
				break;
			default:
				throw new MazeException(""+foe.getFocus());
		}

		ActorActionIntention result = null;
		int count = 0;

		do
		{
			int roll = Dice.d100.roll();

			if (roll <= attackWeight)
			{
				result = getAttackIntention(foe, combat);
			}
			else if (roll <= attackWeight+stealthWeight)
			{
				result = getStealthIntention(foe, combat);
			}
			else
			{
				result = getMagicIntention(foe, combat);
			}

			count++;
		}
		while (result == null || count < 20);

		if (result != null)
		{
			return result;
		}
		else
		{
			return new DefendIntention();
		}
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention getMagicIntention(Foe foe, Combat combat)
	{
		SpellBook spellBook = foe.getSpellBook();
		if (spellBook != null && !spellBook.getSpells().isEmpty())
		{
			List<Spell> spells = spellBook.getSpells();

			// pick a random spell, until the foe can cast it
			Spell spell = null;
			int count = 0;

			do
			{
				spell = spells.get(Dice.nextInt(spells.size()));

				if (!foe.canCast(spell))
				{
					spell = null;
				}

				count++;
			}
			while (spell == null || count < 20);

			// work out the max casting level
			int hpCastingLevel = getMaxCastingLevel(foe, foe.getHitPoints(), spell.getHitPointCost());
			int apCastingLevel = getMaxCastingLevel(foe, foe.getActionPoints(), spell.getActionPointCost());
			int mpCastingLevel = getMaxCastingLevel(foe, foe.getMagicPoints(), spell.getMagicPointCost());

			int castingLevel = Math.min(hpCastingLevel, Math.min(apCastingLevel, mpCastingLevel));

			while (castingLevel > 1 &&
				GameSys.getInstance().getSpellFailureChance(foe, spell, castingLevel) > 10)
			{
				castingLevel--;
			}

			SpellTarget spellTarget = chooseSpellTarget(foe, spell, combat);

			if (spellTarget != null)
			{
				return new SpellIntention(spellTarget, spell, castingLevel);
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private int getMaxCastingLevel(UnifiedActor actor, CurMax resource, Value cost)
	{
		if (cost == null)
		{
			return MagicSys.MAX_CASTING_LEVEL;
		}
		else
		{
			return Math.min(resource.getCurrent() / cost.compute(actor), MagicSys.MAX_CASTING_LEVEL);
		}
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention getStealthIntention(Foe foe, Combat combat)
	{
		List<SpellLikeAbility> slas = foe.getSpellLikeAbilities();

		if (slas != null && !slas.isEmpty())
		{
			// pick a random sla, until the foe can cast it
			SpellLikeAbility sla = null;
			int count = 0;

			do
			{
				sla = slas.get(Dice.nextInt(slas.size()));

				if (!foe.canUseSpellLikeAbility(sla))
				{
					sla = null;
				}

				count++;
			}
			while (sla == null || count < 20);

			SpellTarget spellTarget = chooseSpellTarget(foe, sla.getSpell(), combat);

			if (spellTarget != null)
			{
				return new SpecialAbilityIntention(
					spellTarget,
					sla.getSpell(),
					sla.getCastingLevel().compute(foe));
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention getAttackIntention(Foe foe, Combat combat)
	{
		int engagementRange = combat.getFoeEngagementRange(foe);

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
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellTarget chooseSpellTarget(Foe foe, Spell spell, Combat combat)
	{
		SpellTarget target;
		switch (spell.getTargetType())
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
				spell.getTargetType());
		}
		return target;
	}
}

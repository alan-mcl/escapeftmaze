/*
 * Copyright (c) 2014 Alan McLachlan
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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.ActorUnaffectedEvent;
import mclachlan.maze.stat.combat.event.CloudSpellEvent;
import mclachlan.maze.stat.combat.event.DefendEvent;
import mclachlan.maze.stat.combat.event.MagicAbsorptionEvent;
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.stat.magic.CloudSpellResult;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpellTargetUtils
{
	/*-------------------------------------------------------------------------*/
	public static void resolveCloudAllGroupsSpell(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		int spellLevel,
		Spell s,
		CombatAction action)
	{
		List<ActorGroup> enemyGroups = combat.getFoesOf(caster);

		for (ActorGroup ag : enemyGroups)
		{
			resolveCloudOneGroupSpell(combat, caster, ag, castingLevel, spellLevel, s, action);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveCloudOneGroupSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int castingLevel,
		int spellLevel,
		Spell s,
		CombatAction action)
	{
		// select the victim
		ActorGroup attackedGroup = (ActorGroup)target;

		if (attackedGroup.numAlive() < 1)
		{
			// all in this group are dead
			combat.appendEvent(new DefendEvent(caster));
			return;
		}

		List<SpellEffect> spellEffects =
			processSpellEffectApplication(combat, caster, castingLevel, s.getEffects().getRandom());

		// apply to all in the group
		applyCloudSpellToActorGroup(
			combat,
			spellEffects,
			attackedGroup,
			caster,
			castingLevel,
			action.isAttackingAllies);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return the list of spell effects that apply "as per spell"
	 */
	private static List<SpellEffect> processSpellEffectApplication(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		List<SpellEffect> spellEffects)
	{
		Maze.log(Log.DEBUG, "Combat.processSpellEffectApplication");

		List<SpellEffect> result = new ArrayList<SpellEffect>();

		ListIterator<SpellEffect> li = spellEffects.listIterator();
		while (li.hasNext())
		{
			SpellEffect se = li.next();
			switch (se.getApplication())
			{
				case AS_PER_SPELL:
					result.add(se);
					break;
				case APPLY_ONCE_TO_CASTER:
					combat.appendEvents(
						applySpellEffectToWillingTarget(
							se,
							caster, caster,
							castingLevel));
					break;
				default: throw new MazeException("Invalid application ["+se.getApplication()+"]");
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static void applyCloudSpellToActorGroup(
		Combat combat,
		List<SpellEffect> effects,
		ActorGroup attackedGroup,
		UnifiedActor caster,
		int castingLevel,
		boolean isAttackingAllies)
	{
		for (SpellEffect effect : effects)
		{
			SpellResult sr = effect.getUnsavedResult();
			if (sr instanceof CloudSpellResult)
			{
				CloudSpell newCloudSpell = new CloudSpell(
					(CloudSpellResult)sr,
					caster,
					attackedGroup,
					castingLevel,
					isAttackingAllies);

				combat.appendEvent(new CloudSpellEvent(attackedGroup, newCloudSpell));
			}
			else
			{
				// todo: other cloud spell side effects? do they even make sense?
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveTileSpell(
		Combat combat,
		UnifiedActor caster, int castingLevel, List<SpellEffect> effects)
	{
		Tile tile = Maze.getInstance().getCurrentTile();

		// special case, for Tile spells add the caster as an animation target
		if (combat.getAnimationContext() != null)
		{
			combat.getAnimationContext().addTarget(caster);
		}

		List<SpellEffect> spellEffects =
			processSpellEffectApplication(combat, caster, castingLevel, effects);

		for (SpellEffect effect : spellEffects)
		{
			SpellResult sr = effect.getUnsavedResult();

			combat.appendEvents(sr.apply(caster, tile, castingLevel, effect));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveAllFoesSpell(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		int spellLevel,
		Spell s,
		CombatAction action)
	{
		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		processSpellEffectApplication(combat, caster, castingLevel, s.getEffects().getPossibilities());

		List<ActorGroup> enemyGroups = combat.getFoesOf(caster);

		for (ActorGroup ag : enemyGroups)
		{
			resolveFoeGroupSpell(combat,caster, ag, spellLevel, castingLevel, s, action);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveCasterSpell(
		Combat combat,
		UnifiedActor actor, int castingLevel, List<SpellEffect> effects)
	{
		List<SpellEffect> spellEffects =
			processSpellEffectApplication(combat, actor, castingLevel, effects);

		combat.appendEvents(
			applySpellToWillingTarget(
				spellEffects,
				actor,
				actor,
				castingLevel,
				combat.getAnimationContext()));
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveFoeGroupSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int spellLevel,
		int castingLevel,
		Spell s,
		CombatAction action)
	{
		// select the victim
		ActorGroup attackedGroup = (ActorGroup)target;

		if (attackedGroup == null || attackedGroup.numAlive() < 1)
		{
			// all in this group are dead
			combat.appendEvent(new DefendEvent(caster));
			return;
		}

		// apply to all in the group
		for (UnifiedActor victim : attackedGroup.getActors())
		{
			List<MazeEvent> events =
				applySpellToUnwillingVictim(
					s.getEffects().getRandom(),
					victim,
					caster,
					castingLevel,
					spellLevel,
					combat.getAnimationContext());

			combat.appendEvents(events);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolvePartySpell(Combat combat, UnifiedActor caster,
		int castingLevel, Spell s)
	{
		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		processSpellEffectApplication(combat, caster, castingLevel, s.getEffects().getPossibilities());

		List<MazeEvent> mazeEvents = new ArrayList<MazeEvent>();

		ActorGroup castingParty = caster.getActorGroup();
		for (UnifiedActor target : castingParty.getActors())
		{
			 mazeEvents.addAll(
				 applySpellToWillingTarget(
					 s.getEffects().getRandom(),
					 caster,
					 target,
					 castingLevel,
					 combat.getAnimationContext()));
		}

		combat.appendEvents(mazeEvents);
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveAllySpell(Combat combat, UnifiedActor caster,
		SpellTarget target, int castingLevel, Spell s)
	{
		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		processSpellEffectApplication(combat, caster, castingLevel, s.getEffects().getPossibilities());

		List<MazeEvent> mazeEvents =
			applySpellToWillingTarget(
				s.getEffects().getRandom(),
				caster,
				(UnifiedActor)target,
				castingLevel,
				combat.getAnimationContext());

		combat.appendEvents(mazeEvents);
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveFoeSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int castingLevel,
		int spellLevel,
		Spell s,
		CombatAction action)
	{
		Maze.log(Log.DEBUG, "Combat.resolveFoeSpell");

		if (target == null)
		{
			// no legal targets or all in this group are dead
			combat.appendEvent(new DefendEvent(caster));
			return;
		}

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		processSpellEffectApplication(combat, caster, castingLevel, s.getEffects().getPossibilities());

		// spell targeting not affected by Range rules.
		UnifiedActor victim = (UnifiedActor)target;
		if (victim == null)
		{
			// spell is cast but nothing happens
			return;
		}
		List<MazeEvent> events =
			applySpellToUnwillingVictim(
				s.getEffects().getRandom(),
				victim,
				caster,
				castingLevel,
				spellLevel,
				combat.getAnimationContext());

		combat.appendEvents(events);
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellToWillingTarget(
		List<SpellEffect> effects,
		UnifiedActor caster,
		UnifiedActor target,
		int castingLevel,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (animationContext != null)
		{
			animationContext.addTarget(target);
		}

		for (SpellEffect effect : effects)
		{
			if (effect.getApplication() == SpellEffect.Application.AS_PER_SPELL)
			{
				result.addAll(
					applySpellEffectToWillingTarget(
						effect, target, caster, castingLevel));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellEffectToWillingTarget(
		SpellEffect effect,
		UnifiedActor target,
		UnifiedActor caster,
		int castingLevel)
	{
		Maze.log(Log.DEBUG, "apply spell effect ["+
					effect.getName()+"] to willing target ["+target.getName()+"]");

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// no save applies
		SpellResult sr = effect.getUnsavedResult();

		if (sr.appliesTo(target))
		{
			result.addAll(sr.apply(caster, target, castingLevel, effect));
		}
		else
		{
			// no spell effect on target
			result.add(new ActorUnaffectedEvent(target));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellToUnwillingVictim(
		List<SpellEffect> effects,
		UnifiedActor victim,
		UnifiedActor caster,
		int castingLevel,
		int spellLevel,
		AnimationContext animationContext)
	{
		if (victim.getHitPoints().getCurrent() <= 0)
		{
			// no effect to dead targets
			return new ArrayList<MazeEvent>();
		}

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (animationContext != null)
		{
			animationContext.addTarget(victim);
		}

		// iterate over spell effects
		for (SpellEffect effect : effects)
		{
			if (effect.getApplication() == SpellEffect.Application.AS_PER_SPELL)
			{
				result.addAll(
					applySpellEffectToUnwillingVictim(
						effect,
						victim,
						caster,
						spellLevel,
						castingLevel));
			}
		}

		// kick in the Magic Absorption ability
		if (victim.getModifier(Stats.Modifiers.MAGIC_ABSORPTION) > 0)
		{
			result.add(new MagicAbsorptionEvent(victim));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellEffectToUnwillingVictim(
		SpellEffect effect,
		UnifiedActor victim,
		UnifiedActor caster,
		int spellLevel,
		int castingLevel)
	{
		Maze.log(Log.DEBUG, "apply spell effect ["+
			effect.getName()+"] to unwilling victim ["+victim.getName()+"]");

		SpellResult sr;
		boolean saved = false;
		saved = GameSys.getInstance().savingThrow(
			caster,
			victim,
			effect.getType(),
			effect.getSubType(),
			spellLevel,
			castingLevel,
			effect.getSaveAdjustment().compute(caster, castingLevel));

		if (!saved)
		{
			sr = effect.getUnsavedResult();
		}
		else
		{
			sr = effect.getSavedResult();
		}

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (sr != null && sr.appliesTo(victim))
		{
			result.addAll(sr.apply(caster, victim, castingLevel, effect));
		}
		else
		{
			// no spell effect on victim
			result.add(new ActorUnaffectedEvent(victim));
		}

		return result;
	}
}

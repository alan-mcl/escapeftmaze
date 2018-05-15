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
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.ActorUnaffectedEvent;
import mclachlan.maze.stat.combat.event.CloudSpellEvent;
import mclachlan.maze.stat.combat.event.DefendEvent;
import mclachlan.maze.stat.combat.event.MagicAbsorptionEvent;
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpellTargetUtils
{
	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveCloudAllGroupsSpell(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		int spellLevel,
		Spell s,
		CombatAction action)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<ActorGroup> enemyGroups = combat.getFoesOf(caster);

		for (ActorGroup ag : enemyGroups)
		{
			result.addAll(resolveCloudOneGroupSpell(combat, caster, ag, castingLevel, spellLevel, s, action));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveCloudOneGroupSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int castingLevel,
		int spellLevel,
		Spell spell,
		CombatAction action)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// select the victim
		ActorGroup attackedGroup = (ActorGroup)target;

		if (attackedGroup.numAlive() < 1)
		{
			// all in this group are dead
			result.add(new DefendEvent(caster));
			return result;
		}

		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getRandom(), result, combat);

		// apply to all in the group
		applyCloudSpellToActorGroup(
			combat,
			spellEffects,
			attackedGroup,
			caster,
			castingLevel,
			action.isAttackingAllies());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the list of spell effects that apply "as per spell". Other spell
	 * effects are added to the input param list of events.
	 */
	private static GroupOfPossibilities<SpellEffect> processSpellEffectApplication(
		Spell spell,
		UnifiedActor caster,
		int castingLevel,
		List<SpellEffect> spellEffects,
		List<MazeEvent> events,
		Combat combat)
	{
		Maze.log(Log.DEBUG, "Combat.processSpellEffectApplication");

		GroupOfPossibilities<SpellEffect> result = new GroupOfPossibilities<SpellEffect>();

		ListIterator<SpellEffect> li = spellEffects.listIterator();
		while (li.hasNext())
		{
			SpellEffect se = li.next();
			int percentage = spell.getEffects().getPercentage(se);

			switch (se.getApplication())
			{
				case AS_PER_SPELL:
					result.add(se, percentage);
					break;

				case APPLY_ONCE_TO_CASTER:
					if (Dice.d100.roll("spell effect: "+se.getName()) <= percentage)
					{
						events.addAll(
							applySpellEffectToWillingTarget(
								spell,
								se,
								caster,
								caster,
								castingLevel));
					}
					break;

				case AS_PER_SPELL_EFFECT:

					// see if we can apply un-targeted spell effects
					if (Dice.d100.roll("spell effect: "+se.getName()) <= percentage)
					{
						switch (se.getTargetType())
						{
							case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
								ActorGroup actorGroupWithoutCaster = getActorGroupWithoutCaster(caster);

								for (UnifiedActor a : actorGroupWithoutCaster.getActors())
								{
									events.addAll(
										applySpellEffectToWillingTarget(
											spell,
											se,
											a,
											caster,
											castingLevel));
								}
								break;
							case MagicSys.SpellTargetType.PARTY:
								ActorGroup actorGroup = combat.getActorGroup(caster);

								for (UnifiedActor a : actorGroup.getActors())
								{
									events.addAll(
										applySpellEffectToWillingTarget(
											spell,
											se,
											a,
											caster,
											castingLevel));
								}
								break;

							case MagicSys.SpellTargetType.ALL_FOES:
							case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
								List<UnifiedActor> allFoesOf = combat.getAllFoesOf(caster);

								for (UnifiedActor a : allFoesOf)
								{
									events.addAll(
										applySpellEffectToUnwillingVictim(
											spell,
											se,
											a,
											caster,
											spell.getLevel(),
											castingLevel));
								}
								break;

							case MagicSys.SpellTargetType.CASTER:
								events.addAll(
									applySpellEffectToWillingTarget(
										spell,
										se,
										caster,
										caster,
										castingLevel));
								break;

							case MagicSys.SpellTargetType.TILE:
								Tile tile = Maze.getInstance().getCurrentTile();
								SpellResult sr = se.getUnsavedResult();
								events.addAll(sr.apply(caster, tile, castingLevel, se));
								break;

							// can't pick a target for these ones
							case MagicSys.SpellTargetType.FOE_GROUP:
							case MagicSys.SpellTargetType.FOE:
							case MagicSys.SpellTargetType.ALLY:
							case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
							case MagicSys.SpellTargetType.ITEM:
							case MagicSys.SpellTargetType.NPC:
							case MagicSys.SpellTargetType.LOCK_OR_TRAP:
								throw new MazeException("SpellEffect target type not compatible with spell: " + se.getTargetType());
						}
						break;

					}
				default:
					throw new MazeException("Invalid application [" + se.getApplication() + "]");
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<MazeEvent> applyCloudSpellToActorGroup(
		Combat combat,
		GroupOfPossibilities<SpellEffect> effects,
		ActorGroup attackedGroup,
		UnifiedActor caster,
		int castingLevel,
		boolean isAttackingAllies)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<SpellEffect> effectsRandom = effects.getRandom();
		for (SpellEffect effect : effectsRandom)
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

				result.add(new CloudSpellEvent(attackedGroup, newCloudSpell));
			}
			else
			{
				// todo: other cloud spell side effects? do they even make sense?
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveTileSpell(
		Spell spell,
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		List<SpellEffect> effects,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		Tile tile = Maze.getInstance().getCurrentTile();

		// special case, for Tile spells add the caster as an animation target
		if (animationContext != null)
		{
			animationContext.addTarget(caster);
		}

		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, effects, result, combat);

		List<SpellEffect> spellEffectsRandom = spellEffects.getRandom();
		for (SpellEffect effect : spellEffectsRandom)
		{
			SpellResult sr = effect.getUnsavedResult();
			result.addAll(sr.apply(caster, tile, castingLevel, effect));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveAllFoesSpell(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		int spellLevel,
		Spell spell,
		CombatAction action,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getPossibilities(), result, combat);

		List<ActorGroup> enemyGroups = new ArrayList<ActorGroup>();

		if (combat != null)
		{
			enemyGroups.addAll(combat.getFoesOf(caster));
		}
		else if (caster instanceof GameSys.DummyCaster)
		{
			enemyGroups.add(Maze.getInstance().getParty());
		}
		else
		{
			// something is up
			throw new MazeException("Unknown caster "+caster);
		}

		for (ActorGroup ag : enemyGroups)
		{
			result.addAll(
				resolveFoeGroupSpell(
					combat,
					caster,
					ag,
					spellLevel,
					castingLevel,
					spell,
					spellEffects,
					action,
					animationContext));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveCasterSpell(
		Spell spell,
		Combat combat,
		UnifiedActor actor,
		int castingLevel,
		List<SpellEffect> effects,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, actor, castingLevel, effects, result, combat);

		result.addAll(
			applySpellToWillingTarget(
				spell,
				spellEffects.getRandom(),
				actor,
				actor,
				castingLevel,
				animationContext));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveFoeGroupSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int spellLevel,
		int castingLevel,
		Spell spell,
		CombatAction action,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getPossibilities(), result, combat);

		result.addAll(
			resolveFoeGroupSpell(
				combat, caster, target, spellLevel, castingLevel, spell, spellEffects, action, animationContext));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<MazeEvent> resolveFoeGroupSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int spellLevel,
		int castingLevel,
		Spell spell,
		GroupOfPossibilities<SpellEffect> spellEffects,
		CombatAction action,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// select the victim
		ActorGroup attackedGroup = (ActorGroup)target;

		if (attackedGroup == null || attackedGroup.numAlive() < 1)
		{
			// all in this group are dead
			result.add(new DefendEvent(caster));
			return result;
		}

		// apply to all in the group
		for (UnifiedActor victim : attackedGroup.getActors())
		{
			List<MazeEvent> events =
				applySpellToUnwillingVictim(
					spell,
					spellEffects.getRandom(),
					victim,
					caster,
					castingLevel,
					spellLevel,
					animationContext);

			result.addAll(events);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolvePartySpell(Combat combat, UnifiedActor caster,
		int castingLevel, Spell spell, AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getPossibilities(), result, combat);

		List<MazeEvent> mazeEvents = new ArrayList<MazeEvent>();

		ActorGroup castingParty = caster.getActorGroup();
		for (UnifiedActor target : castingParty.getActors())
		{
			 mazeEvents.addAll(
				 applySpellToWillingTarget(
					 spell,
					 spellEffects.getRandom(),
					 caster,
					 target,
					 castingLevel,
					 animationContext));
		}

		result.addAll(mazeEvents);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolvePartyButNotCasterSpell(Combat combat, UnifiedActor caster,
		int castingLevel, Spell spell, AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getPossibilities(), result, combat);

		List<MazeEvent> mazeEvents = new ArrayList<MazeEvent>();

		ActorGroup actors = SpellTargetUtils.getActorGroupWithoutCaster(caster);
		for (UnifiedActor target : actors.getActors())
		{
			mazeEvents.addAll(
				applySpellToWillingTarget(
					spell,
					spellEffects.getRandom(),
					caster,
					target,
					castingLevel,
					animationContext));
		}

		result.addAll(mazeEvents);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveAllySpell(Combat combat, UnifiedActor caster,
		SpellTarget target, int castingLevel, Spell spell, AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// determine what spell effects to apply
		List<SpellEffect> spellEffectsToApply = spell.getEffects().getRandom();

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spellEffectsToApply, result, combat);

		List<MazeEvent> mazeEvents =
			applySpellToWillingTarget(
				spell,
				spellEffects.getRandom(),
				caster,
				(UnifiedActor)target,
				castingLevel,
				animationContext);

		result.addAll(mazeEvents);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveFoeSpell(
		Combat combat,
		UnifiedActor caster,
		SpellTarget target,
		int castingLevel,
		int spellLevel,
		Spell spell,
		CombatAction action,
		AnimationContext animationContext)
	{
		Maze.log(Log.DEBUG, "SpellTargetUtils.resolveFoeSpell");

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (target == null)
		{
			// no legal targets or all in this group are dead
			result.add(new DefendEvent(caster));
			return result;
		}

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, spell.getEffects().getPossibilities(), result, combat);

		// spell targeting not affected by Range rules.
		UnifiedActor victim = (UnifiedActor)target;
		if (victim == null)
		{
			// spell is cast but nothing happens
			return result;
		}

		List<MazeEvent> events =
			applySpellToUnwillingVictim(
				spell,
				spellEffects.getRandom(),
				victim,
				caster,
				castingLevel,
				spellLevel,
				animationContext);
		result.addAll(events);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveLockOrTrapSpell(
		Combat combat,
		LockOrTrap lockOrTrap,
		Spell spell,
		PlayerCharacter caster,
		int castingLevel)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<SpellEffect> effects = spell.getEffects().getPossibilities();

		// this will apply "once to caster" effects to the caster.
		// such effects will be skipped later
		GroupOfPossibilities<SpellEffect> spellEffects =
			processSpellEffectApplication(spell, caster, castingLevel, effects, result, combat);

		for (SpellEffect s : spellEffects.getRandom())
		{
			List<MazeEvent> events = s.getUnsavedResult().apply(caster, lockOrTrap, castingLevel, s);
			if (events != null)
			{
				result.addAll(events);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellToWillingTarget(
		Spell spell,
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
			if (effect.getApplication() != SpellEffect.Application.APPLY_ONCE_TO_CASTER)
			{
				result.addAll(
					applySpellEffectToWillingTarget(
						spell, effect, target, caster, castingLevel));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellEffectToWillingTarget(
		Spell spell,
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
			result.addAll(sr.apply(caster, target, castingLevel, effect, spell));
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
		Spell spell,
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
			if (effect.getApplication() != SpellEffect.Application.APPLY_ONCE_TO_CASTER)
			{
				result.addAll(
					applySpellEffectToUnwillingVictim(
						spell,
						effect,
						victim,
						caster,
						spellLevel,
						castingLevel));
			}
		}

		// kick in the Magic Absorption ability
		if (victim.getModifier(Stats.Modifier.MAGIC_ABSORPTION) > 0)
		{
			result.add(new MagicAbsorptionEvent(victim));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> applySpellEffectToUnwillingVictim(
		Spell spell,
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
			result.addAll(sr.apply(caster, victim, castingLevel, effect, spell));
		}
		else
		{
			// no spell effect on victim
			result.add(new ActorUnaffectedEvent(victim));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveItemSpell(
		Combat combat,
		UnifiedActor caster,
		int castingLevel,
		int level,
		Spell spell,
		Item target)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<SpellEffect> effects = spell.getEffects().getRandom();
		for (SpellEffect effect : effects)
		{
			// todo: currently ignoring other spell effects and events produced
			if (effect.getTargetType() == MagicSys.SpellTargetType.ITEM)
			{
				SpellResult sr = effect.getUnsavedResult();
				List<MazeEvent> mazeEvents = sr.apply(caster, target, castingLevel, effect);
				if (mazeEvents != null)
				{
					result.addAll(mazeEvents);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static ActorGroup getActorGroupWithoutCaster(UnifiedActor caster)
	{
		if (caster instanceof Foe)
		{
			FoeGroup ag = (FoeGroup)caster.getActorGroup();
			FoeGroup fg = new FoeGroup();

			for (Foe f : ag.getFoes())
			{
				if (f != caster)
				{
					fg.add(f);
				}
			}

			return fg;
		}
		else
		{
			ArrayList<UnifiedActor> actors = new ArrayList<UnifiedActor>();

			PlayerParty pp = (PlayerParty)caster.getActorGroup();
			for (UnifiedActor a : pp.getActors())
			{
				if (a != caster)
				{
					actors.add(a);
				}
			}

			return new PlayerParty(actors);
		}
	}
}

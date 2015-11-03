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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.ActorsTurnToAct;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ActorActionResolver
{
	/*-------------------------------------------------------------------------*/
	/**
	 * @param action
	 * 	The combat action to resolve
	 * @param combat
	 * 	The current combat, null if there is none
	 * @return
	 * 	An array of events that take place as a result of this combat action
	 */
	public static List<MazeEvent> resolveAction(
		CombatAction action,
		Combat combat)
	{
		UnifiedActor actor = action.actor;

		if (!action.isAttackingAllies)
		{
			// at this point check and see if any conditions screw this actor over
			action.actor = actor;
			action = GameSys.getInstance().checkConditions(action.actor, action);
		}

		AnimationContext animationContext = new AnimationContext(actor);

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		CombatantData combatantData = action.actor.getCombatantData();
		if (combatantData != null)
		{
			// set the action on the combatant data
			combatantData.setCurrentAction(action);
		}

		Combat.AmbushStatus ambushStatus = Combat.AmbushStatus.NONE;
		if (combat != null)
		{
			ambushStatus = combat.getAmbushStatus();
		}

		if (((ambushStatus == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY ||
			ambushStatus == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY) && actor instanceof Foe) ||
			((ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES ||
				ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES) && actor instanceof PlayerCharacter))
		{
			StatModifier sm = GameSys.getInstance().getSurpriseModifiers(actor);
			combatantData.getMiscModifiers().setModifiers(sm);
		}

		//
		// If not in combat, assume actor can always take this action.
		// Otherwise check if the actor is active - might have been gimped during
		// the combat round
		//
		if (combatantData == null || combatantData.isActive())
		{
			if (action instanceof AttackAction)
			{
				if (combat != null)
				{
					// todo: unwind event resolutions in here -> into events resolve() methods
					resolveAttack(
						action.actor,
						(AttackAction)action,
						combat,
						result,
						animationContext);
				}
				else
				{
					// nobody to attack
					result.add(new DefendEvent(actor));
				}
			}
			else if (action instanceof DefendAction)
			{
				// defending doesn't achieve much
				result.add(new DefendEvent(action.actor));
			}
			else if (action instanceof HideAction)
			{
				result.add(new HideAttemptEvent(action.actor, combat));
			}
			else if (action instanceof SpellAction)
			{
				SpellAction spellAction = (SpellAction)action;

				SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
					combat,
					spellAction.getActor(),
					spellAction.getSpell(),
					spellAction.getCastingLevel(),
					spellAction.getTarget());

				if (wildMagic != null)
				{
					spellAction = wildMagic;
				}

				result.addAll(
					resolveSpell(
						combat,
						action.actor,
						spellAction,
						false,
						animationContext));
			}
			else if (action instanceof SpellSilencedAction)
			{
				SpellSilencedAction ssa = (SpellSilencedAction)action;
				result.add(new SpellCastEvent(actor, ssa.getSpell(), ssa.getCastingLevel()));
				result.add(new SpellFizzlesEvent(actor, ssa.getSpell(), ssa.getCastingLevel()));
			}
			else if (action instanceof SpecialAbilityAction)
			{
				SpecialAbilityAction act = (SpecialAbilityAction)action;

				SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
					combat,
					act.getActor(),
					act.getSpell(),
					act.getCastingLevel(),
					act.getTarget());

				if (wildMagic != null)
				{
					UnifiedActor caster = act.actor;
					act = new SpecialAbilityAction(
						act.getDescription(),
						wildMagic.getTarget(),
						wildMagic.getSpell(),
						act.getCastingLevel());
					act.actor = caster;
				}

				resolveSpecialAbility(
					combat,
					action.actor,
					act,
					result,
					animationContext);
			}
			else if (action instanceof UseItemAction)
			{
				resolveUseItem(
					combat,
					action.actor,
					(UseItemAction)action,
					result,
					animationContext);
			}
			else if (action instanceof EquipAction)
			{
				result.add(new EquipEvent(actor));
			}
			else if (action instanceof RunAwayAction)
			{
				result.add(new RunAwayAttemptEvent(actor, combat));
			}
			else if (action == CombatAction.DO_NOTHING)
			{
				// decrease fatigue
				// todo: make sure that this only runs once per turn
				CurMaxSub hp = actor.getHitPoints();
				int stamina = GameSys.getInstance().getFatigueToRegenInCombat(actor);
				hp.incSub(-stamina);
			}
			else if (action instanceof CowerInFearAction)
			{
				result.add(new CowerInFearEvent(action.actor));
			}
			else if (action instanceof FreezeInTerrorAction)
			{
				result.add(new FreezeInTerrorEvent(action.actor));
			}
			else if (action instanceof StumbleBlindlyAction)
			{
				result.add(new StumbleBlindlyEvent(action.actor));
			}
			else if (action instanceof GagsHelplesslyAction)
			{
				result.add(new GagsHelplesslyEvent(action.actor));
			}
			else if (action instanceof RetchesNoisilyAction)
			{
				result.add(new RetchesNoisilyEvent(action.actor));
			}
			else if (action instanceof DancesWildlyAction)
			{
				result.add(new DancesWildlyEvent(action.actor));
			}
			else if (action instanceof LaughsMadlyAction)
			{
				result.add(new LaughsMadlyEvent(action.actor));
			}
			else if (action instanceof ItchesUncontrollablyAction)
			{
				result.add(new ItchesUncontrollablyEvent(action.actor));
			}
			else if (action instanceof AttackAlliesAction)
			{
				resolveAttackAlliesAction(combat, (AttackAlliesAction)action, result);
			}
			else if (action instanceof StrugglesMightilyAction)
			{
				StrugglesMightilyAction sma = (StrugglesMightilyAction)action;
				result.add(new StrugglesMightilyEvent(sma.actor, sma.condition));
			}
			else if (action instanceof BlinkAction)
			{
				if (GameSys.getInstance().isActorBlinkedOut(actor))
				{
					result.add(new BlinkInEvent(actor));
				}
				else
				{
					result.add(new BlinkOutEvent(actor));
				}
			}
			else if (action instanceof ThreatenAction)
			{
				Maze maze = Maze.getInstance();
				ThreatenAction threatenAction = (ThreatenAction)action;

				result.add(new ThreatenEvent(actor, threatenAction.getTarget()));
				result.add(new ActorsTurnToAct(
					maze.getCurrentActorEncounter(),
					maze,
					maze.getUi().getMessageDestination()));
			}
			else if (action instanceof TalkAction)
			{
				TalkAction talkAction = (TalkAction)action;
				Foe npc = (Foe)talkAction.getTarget();
				PlayerCharacter pc = (PlayerCharacter)talkAction.getActor();
				result.addAll(npc.getActionScript().partyWantsToTalk(pc));
			}
			else if (action instanceof GiveAction)
			{
				GiveAction ga = (GiveAction)action;

				Foe npc = (Foe)ga.getTarget();
				PlayerCharacter pc = (PlayerCharacter)ga.getActor();
				result.add(new ChooseItemToGive(npc, pc));
			}
			else if (action instanceof BribeAction)
			{
				BribeAction ga = (BribeAction)action;

				Foe npc = (Foe)ga.getTarget();
				PlayerCharacter pc = (PlayerCharacter)ga.getActor();
				result.add(new ChooseBriberyAmount(npc, pc));
			}
			else if (action instanceof StealAction)
			{
				StealAction sa = (StealAction)action;
				PlayerCharacter pc = (PlayerCharacter)sa.getActor();
				Foe npc = (Foe)sa.getTarget();
				result.add(new PlanTheftEvent(npc, pc));
			}
			else if (action instanceof TradeAction)
			{
				TradeAction a = (TradeAction)action;
				PlayerCharacter pc = (PlayerCharacter)a.getActor();
				Foe npc = (Foe)a.getTarget();
				result.add(new InitiateTradeEvent(npc, pc));
			}
			else if (action instanceof DisarmTrapAction)
			{
				DisarmTrapAction a = (DisarmTrapAction)action;
				PlayerCharacter pc = (PlayerCharacter)a.getActor();
				Chest chest = a.getChest();
				result.add(new DisarmTrapEvent(pc, chest, chest));
			}
			else if (action instanceof OpenChestAction)
			{
				OpenChestAction a = (OpenChestAction)action;
				PlayerCharacter pc = (PlayerCharacter)a.getActor();
				Chest chest = a.getChest();
				result.add(new OpenChestEvent(pc, chest, chest));
			}
			else
			{
				throw new MazeException("Unrecognised combat action: "+action);
			}
		}

		// clear the action
		if (combat != null && combatantData != null)
		{
			combatantData.setCurrentAction(null);
		}

		// set the animation context for any animation events
		for (MazeEvent e : result)
		{
			if (e instanceof AnimationEvent)
			{
				((AnimationEvent)e).setAnimationContext(animationContext);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static void resolveAttackAlliesAction(
		Combat combat,
		AttackAlliesAction action,
		List<MazeEvent> events)
	{
		// get a random target group of allies
		UnifiedActor actor = action.actor;
		int groups = combat.getNrOfAlliedGroups(actor);
		int targetGroup = GameSys.getInstance().nextInt(groups);

		// get an attack action
		ActorActionIntention intention = new AttackIntention(
			combat.getAlliesOf(actor).get(targetGroup),
			combat,
			action.getAttackWith());

		List<CombatAction> actions = getActorCombatActions(combat, actor, intention);

		for (CombatAction act : actions)
		{
			act.isAttackingAllies = true;
			act.actor = actor;

			// recursion alert
			events.addAll(resolveAction(act, combat));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<CombatAction> getActorCombatActions(
		Combat combat,
		UnifiedActor actor,
		ActorActionIntention intention)
	{
		intention = GameSys.getInstance().checkConditions(actor, intention, combat);

		List<CombatAction> actorActions;
		actorActions = actor.getCombatActions(intention);
		return actorActions;
	}

	/*-------------------------------------------------------------------------*/
	private static void resolveUseItem(
		Combat combat,
		UnifiedActor actor,
		UseItemAction useItemAction,
		List<MazeEvent> events,
		AnimationContext animationContext)
	{
		Item item = useItemAction.getItem();
		events.add(new ItemUseEvent(actor, item));

		Spell s = item.getInvokedSpell();
		int castingLevel = item.getInvokedSpellLevel();

		if (s == null)
		{
			// no invoked spell on this item
			events.add(new NoEffectEvent());
			return;
		}

		if (castingLevel == 0)
		{
			// zero casting level implies that the effective level should scale
			// based on the user's skill level

			castingLevel = GameSys.getInstance().getItemUseCastingLevel(actor, item);
		}

		SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
			combat,
			actor,
			s,
			castingLevel,
			useItemAction.getTarget());

		if (wildMagic != null)
		{
			s = wildMagic.getSpell();
			useItemAction.setTarget(wildMagic.getTarget());
		}

		int targetType = s.getTargetType();

		// practise any modifiers
		StatModifier useRequirements = item.getUseRequirements();
		for (String mod : useRequirements.getModifiers().keySet())
		{
			GameSys.getInstance().practice(actor, mod, 1);
		}

		// EARLY EXIT SPECIAL CASE SHIT: spellbooks attempt to teach the spell
		// to the character, failure means that nothing happens
		if (item.getType() == ItemTemplate.Type.SPELLBOOK && actor instanceof PlayerCharacter)
		{
			PlayerCharacter pc = (PlayerCharacter)actor;

			List<Spell> spells = pc.getSpellsThatCanBeLearned();
			Spell spell = item.getInvokedSpell();
			if (spells != null && spell != null && spells.contains(spell))
			{
				events.add(new ActorLearnsSpellEvent(pc, spell));
			}
			else
			{
				events.add(new FailureEvent());
			}

			// early exit
			return;
		}

		int failureChance = GameSys.getInstance().getItemUseFailureChance(
			actor, item);

		int failureRoll = Dice.d100.roll();

		boolean canBackfire = GameSys.getInstance().canBackfire(item);

		if (failureRoll <= failureChance)
		{
			if (failureRoll*4 > failureChance || !canBackfire)
			{
				// spell fizzles
				events.add(new SpellFizzlesEvent(actor, s, castingLevel));
				return;
			}
			else
			{
				// spell backfires
				if (targetType == MagicSys.SpellTargetType.ALLY ||
					targetType == MagicSys.SpellTargetType.CASTER ||
					targetType == MagicSys.SpellTargetType.ITEM ||
					targetType == MagicSys.SpellTargetType.LOCK_OR_TRAP ||
					targetType == MagicSys.SpellTargetType.NPC ||
					targetType == MagicSys.SpellTargetType.TILE ||
					targetType == MagicSys.SpellTargetType.PARTY)
				{
					// these spell target types cannot backfire, simply fizzle
					// todo: may be amusing to provide various backfire effects here
					events.add(new SpellFizzlesEvent(actor, s, castingLevel));
					return;
				}

				events.add(new SpellBackfiresEvent(actor, s, castingLevel));
				useItemAction.isAttackingAllies = true;

				// warp the target
				switch(targetType)
				{
					case MagicSys.SpellTargetType.ALL_FOES:
					case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
						// todo: no appropriate ALL_ALLIES target type?
						targetType = MagicSys.SpellTargetType.PARTY;
						useItemAction.setTarget(combat.getActorGroup(actor));
						break;

					case MagicSys.SpellTargetType.FOE:
						// todo: target actors own group
						targetType = MagicSys.SpellTargetType.ALLY;
						List<UnifiedActor> allies = combat.getAllAlliesOf(actor);
						Dice d = new Dice(1, allies.size(), -1);
						SpellTarget target = allies.get(d.roll());
						useItemAction.setTarget(target);
						break;

					case MagicSys.SpellTargetType.FOE_GROUP:
					case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
						// todo: target actors own group
						targetType = MagicSys.SpellTargetType.PARTY;
						useItemAction.setTarget(combat.getActorGroup(actor));
						break;
				}
			}
		}

		// the casting animation (todo: cast by player allies??)
		if (actor instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				events.addAll(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				events.addAll(s.getCastByFoeScript().getEvents());
			}
		}

		switch (s.getTargetType())
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				events.addAll(
					SpellTargetUtils.resolveFoeSpell(
						combat,
						actor,
						useItemAction.getTarget(),
						castingLevel,
						castingLevel,
						s,
						useItemAction,
						animationContext));
				break;
			case MagicSys.SpellTargetType.ALLY:
				events.addAll(
					SpellTargetUtils.resolveAllySpell(
						combat,
						actor,
						useItemAction.getTarget(),
						castingLevel,
						s,
						animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(
					SpellTargetUtils.resolvePartySpell(
						combat,
						actor,
						castingLevel,
						s,
						animationContext));
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(
					SpellTargetUtils.resolveFoeGroupSpell(
						combat,
						actor,
						useItemAction.getTarget(),
						castingLevel,
						castingLevel,
						s,
						useItemAction,
						animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
			case MagicSys.SpellTargetType.ITEM:
				events.addAll(SpellTargetUtils.resolveLockOrTrapSpellOnChest(
					Maze.getInstance().getCurrentChest(),
					s,
					(PlayerCharacter)actor,
					castingLevel));
				break;
			case MagicSys.SpellTargetType.CASTER:
				events.addAll(
					SpellTargetUtils.resolveCasterSpell(
						combat,
						actor,
						castingLevel,
						s.getEffects().getRandom(),
						animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(
					SpellTargetUtils.resolveAllFoesSpell(
						combat,
						actor,
						castingLevel,
						castingLevel,
						s,
						useItemAction,
						animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(
					SpellTargetUtils.resolveTileSpell(
						combat,
						actor,
						castingLevel,
						s.getEffects().getRandom(),
						animationContext));
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				events.addAll(
					SpellTargetUtils.resolveCloudOneGroupSpell(
						combat,
						actor,
						useItemAction.getTarget(),
						castingLevel,
						castingLevel,
						s,
						useItemAction));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(
					SpellTargetUtils.resolveCloudAllGroupsSpell(
						combat,
						actor,
						castingLevel,
						castingLevel,
						s,
						useItemAction));
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+s.getTargetType());
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void resolveSpecialAbility(
		Combat combat,
		UnifiedActor actor,
		SpecialAbilityAction action,
		List<MazeEvent> events,
		AnimationContext animationContext)
	{
		Spell s = action.getSpell();

		// at this point the spell has been successfully cast
		events.add(new SpecialAbilityUseEvent(actor, s, action.getCastingLevel(), action.getDescription()));

		// the casting animation (todo: cast by player allies?)
		if (actor instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				events.addAll(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				events.addAll(s.getCastByFoeScript().getEvents());
			}
		}

		switch (s.getTargetType())
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				events.addAll(
					SpellTargetUtils.resolveFoeSpell(
						combat,
						actor,
						action.getTarget(),
						action.getCastingLevel(),
						s.getLevel(),
						s,
						action,
						animationContext));
				break;
			case MagicSys.SpellTargetType.ALLY:
				events.addAll(
					SpellTargetUtils.resolveAllySpell(
						combat,
						actor,
						action.getTarget(),
						action.getCastingLevel(),
						s,
						animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(SpellTargetUtils.resolvePartySpell(
					combat,
					actor,
					action.getCastingLevel(),
					s,
					animationContext));
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(SpellTargetUtils.resolveFoeGroupSpell(
					combat,
					actor,
					action.getTarget(),
					s.getLevel(),
					action.getCastingLevel(),
					s,
					action,
					animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				//nothing to do
				break;
			case MagicSys.SpellTargetType.CASTER:
				events.addAll(SpellTargetUtils.resolveCasterSpell(
					combat,
					actor,
					action.getCastingLevel(),
					s.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(SpellTargetUtils.resolveAllFoesSpell(
					combat,
					actor,
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action,
					animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(SpellTargetUtils.resolveTileSpell(
					combat,
					actor,
					action.getCastingLevel(),
					s.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				events.addAll(SpellTargetUtils.resolveCloudOneGroupSpell(
					combat,
					actor,
					action.getTarget(),
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(SpellTargetUtils.resolveCloudAllGroupsSpell(
					combat,
					actor,
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action));
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+s.getTargetType());
		}
	}

	/*-------------------------------------------------------------------------*/
	public static List<MazeEvent> resolveSpell(
		Combat combat,
		final UnifiedActor caster,
		SpellAction spellAction,
		boolean isCloudSpell,
		AnimationContext animationContext)
	{
		Maze.log(Log.DEBUG, "resolving ["+caster.getName()+"] spell action ["+
			spellAction+" ("+isCloudSpell+")]");

		List<MazeEvent> events = new ArrayList<MazeEvent>();

		final Spell s = spellAction.getSpell();

		if (!isCloudSpell)
		{
			// practise modifiers

			GameSys.getInstance().practice(caster, s.getPrimaryModifier(), 1);
			if (Dice.d2.roll() == 2)
			{
				GameSys.getInstance().practice(caster, s.getSecondaryModifier(), 1);
			}
		}

		int castingLevel = spellAction.getCastingLevel();
		int targetType = s.getTargetType();
		if (caster instanceof GameSys.DummyCaster)
		{
			// dodgy hack to allow for traps and such
			events.add(new SpellCastEvent(caster, s, castingLevel)
			{
				public String getText()
				{
					return caster.getName();
				}
			});
		}
		else
		{
			if (isCloudSpell)
			{
				events.add(new SpellCastEvent(caster, s, castingLevel)
				{
					public String getText()
					{
						return s.getDescription();
					}
				});
			}
			else
			{
				events.add(new SpellCastEvent(caster, s, castingLevel));
			}
		}

		if (!isCloudSpell)
		{
			// deduct magic points
			int pointCost = MagicSys.getInstance().getMagicPointCost(
				spellAction.getSpell(), spellAction.getCastingLevel(), caster);
			if (pointCost > caster.getMagicPoints().getCurrent())
			{
				Maze.log(Log.DEBUG, "insufficient magic points: "+pointCost+" > "+caster.getMagicPoints().getCurrent());
				events.add(new SpellFizzlesEvent(caster, s, castingLevel));
				return events;
			}

			if (caster instanceof PlayerCharacter &&
				!((PlayerCharacter)caster).canCast(s))
			{
				events.add(new SpellFizzlesEvent(caster, s, castingLevel));
				return events;
			}
		}

		if (!isCloudSpell)
		{
			int spellFailureChance = GameSys.getInstance().getSpellFailureChance(caster, s, castingLevel);

			int spellFailureRoll = Dice.d100.roll();

			if (spellFailureRoll <= spellFailureChance)
			{
				if (spellFailureRoll*5 > spellFailureChance)
				{
					// spell fizzles
					events.add(new SpellFizzlesEvent(caster, s, castingLevel));
					return events;
				}
				else
				{
					// spell backfires
					if (targetType == MagicSys.SpellTargetType.ITEM ||
						targetType == MagicSys.SpellTargetType.LOCK_OR_TRAP ||
						targetType == MagicSys.SpellTargetType.NPC ||
						targetType == MagicSys.SpellTargetType.TILE ||
						targetType == MagicSys.SpellTargetType.PARTY ||
						combat == null ||
						combat.getNrOfLivingEnemies(caster) == 0)
					{
						// these spell target types cannot backfire (or there are no
						// foes to target), simply fizzle
						// todo: may be amusing to provide various backfire effects here
						events.add(new SpellFizzlesEvent(caster, s, castingLevel));
						return events;
					}

					events.add(new SpellBackfiresEvent(caster, s, castingLevel));

					// warp the target (can assume combat!=null here)
					switch(targetType)
					{
						case MagicSys.SpellTargetType.PARTY:
							targetType = MagicSys.SpellTargetType.FOE_GROUP;
							List<ActorGroup> foesOf = combat.getFoesOf(caster);
							spellAction.setTarget(foesOf.get(GameSys.getInstance().nextInt(foesOf.size())));
							break;

						case MagicSys.SpellTargetType.ALLY:
						case MagicSys.SpellTargetType.CASTER:
							targetType = MagicSys.SpellTargetType.FOE;
							spellAction.setTarget(combat.getRandomFoeOf(caster));
							break;

						case MagicSys.SpellTargetType.ALL_FOES:
						case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
							// todo: no appropriate ALL_ALLIES target type?
							targetType = MagicSys.SpellTargetType.PARTY;
							spellAction.setTarget(combat.getActorGroup(caster));
							break;

						case MagicSys.SpellTargetType.FOE:
							targetType = MagicSys.SpellTargetType.ALLY;
							spellAction.setTarget(combat.getRandomAllyOf(caster));
							break;

						case MagicSys.SpellTargetType.FOE_GROUP:
						case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
							targetType = MagicSys.SpellTargetType.PARTY;
							spellAction.setTarget(combat.getActorGroup(caster));
							break;
					}
				}
			}
		}

		// the casting animation (todo: cast by player allies?)
		if (caster instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				events.addAll(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				events.addAll(s.getCastByFoeScript().getEvents());
			}
		}

		switch (targetType)
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				events.addAll(SpellTargetUtils.resolveFoeSpell(
					combat,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s.getLevel(),
					s,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALLY:
				events.addAll(SpellTargetUtils.resolveAllySpell(
					combat,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s,
					animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(SpellTargetUtils.resolvePartySpell(
					combat,
					caster,
					castingLevel,
					s,
					animationContext));
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(SpellTargetUtils.resolveFoeGroupSpell(
					combat,
					caster,
					spellAction.getTarget(),
					s.getLevel(),
					castingLevel,
					s,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				events.addAll(SpellTargetUtils.resolveLockOrTrapSpellOnChest(
					Maze.getInstance().getCurrentChest(),
					s,
					(PlayerCharacter)caster,
					castingLevel));
				break;
			case MagicSys.SpellTargetType.CASTER:
				events.addAll(SpellTargetUtils.resolveCasterSpell(
					combat,
					caster,
					castingLevel,
					s.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(SpellTargetUtils.resolveAllFoesSpell(
					combat,
					caster,
					castingLevel,
					s.getLevel(),
					s,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(SpellTargetUtils.resolveTileSpell(
					combat,
					caster,
					castingLevel,
					s.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				events.addAll(SpellTargetUtils.resolveCloudOneGroupSpell(
					combat,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s.getLevel(),
					s,
					spellAction));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(SpellTargetUtils.resolveCloudAllGroupsSpell(
					combat,
					caster,
					castingLevel,
					s.getLevel(),
					s,
					spellAction));
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+ targetType);
		}

		return events;
	}

	/*-------------------------------------------------------------------------*/
	private static void resolveAttack(
		UnifiedActor actor,
		AttackAction attackAction,
		Combat combat,
		List<MazeEvent> events,
		AnimationContext animationContext)
	{
		ActorGroup attackedGroup;
		attackedGroup = attackAction.getTargetGroup();

		if (attackedGroup.numAlive() < 1)
		{
			// all in this group are dead do nothing
			return;
		}

		combat.getCombatStatistics().captureAttack(attackAction, combat);

		UnifiedActor defender;
		if (attackAction.isFirstAttack())
		{
			defender =
				getDefender(actor, attackedGroup, attackAction.getAttackWith(), attackAction, combat);
		}
		else
		{
			defender = attackAction.getDefender();
			if (defender.getHitPoints().getCurrent() <= 0)
			{
				return;
			}
		}

		if (defender == null)
		{
			// cannot attack
			return;
		}
		attackAction.setDefender(defender);
		attack(combat, actor, defender, attackAction, events, animationContext);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	null if no valid defender is found
	 */
	private static UnifiedActor getDefender(
		UnifiedActor attacker,
		ActorGroup attackedGroup,
		AttackWith attackWith,
		CombatAction action,
		Combat combat)
	{
		List<UnifiedActor> actors;
		int engagementRange;
		if (action.isAttackingAllies)
		{
			// fudge it
			engagementRange = -1;
			actors = attackedGroup.getActors();
		}
		else
		{
			engagementRange = getEngagementRange(attacker, attackedGroup, combat);
			int minRange = attackWith.getMinRange();
			int maxRange = attackWith.getMaxRange();

			// see who is attackable
			actors = attackedGroup.getActors(
				engagementRange,
				minRange,
				maxRange);
		}

		if (actors == null)
		{
			throw new MazeException("No legal targets found for attack by ["+
				attacker.getName()+"] eng="+engagementRange+", min="+
				attackWith.getMinRange()+", max=" + attackWith.getMinRange());
		}

		return getRandomDefender(attacker, actors);
	}

	/*-------------------------------------------------------------------------*/
	private static UnifiedActor getRandomDefender(UnifiedActor attacker, List<UnifiedActor> actors)
	{
		List<UnifiedActor> temp = new ArrayList<UnifiedActor>();
		List<Double> weights = new ArrayList<Double>();

		//remove dead actors
		for (UnifiedActor actor : actors)
		{
			if (actor.getHitPoints().getCurrent() > 0 &&
				GameSys.getInstance().isActorAttackable(actor) &&
				actor != attacker)
			{
				temp.add(actor);
				double weight = 1.0 / (1.0 + actor.getModifier(Stats.Modifiers.OBFUSCATION));
				weights.add(weight);
			}
		}

		if (temp.isEmpty())
		{
			return null;
		}

		PercentageTable<UnifiedActor> perc = new PercentageTable<UnifiedActor>(
			temp, weights);

		return perc.getRandomItem();
	}

	/*-------------------------------------------------------------------------*/
	public static int getEngagementRange(
		UnifiedActor attacker,
		ActorGroup attackedGroup,
		Combat combat)
	{
		if (isPlayerCharacter(attacker))
		{
			return getPCEngagementRange(attacker, attackedGroup, combat);
		}
		else
		{
			return getFoeEngagementRange(attacker, combat);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isPlayerCharacter(UnifiedActor actor)
	{
		return actor instanceof PlayerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public static int getFoeEngagementRange(
		UnifiedActor attacker,
		Combat combat)
	{
		int foeGroupIndex = combat.getFoes().indexOf(attacker.getCombatantData().getGroup());

		if (foeGroupIndex == -1)
		{
			foeGroupIndex = combat.getPartyAllies().indexOf(attacker.getCombatantData().getGroup());
		}

		if (foeGroupIndex == -1)
		{
			throw new MazeException("Cannot find group for foe ["+attacker+"]");
		}

		switch (foeGroupIndex)
		{
			case 0: return ItemTemplate.WeaponRange.MELEE;
			case 1: return ItemTemplate.WeaponRange.MELEE;
			case 2: return ItemTemplate.WeaponRange.EXTENDED;
			case 3: return ItemTemplate.WeaponRange.THROWN;
			default: return ItemTemplate.WeaponRange.LONG;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static int getFoeGroupIndex(ActorGroup ag, Combat combat)
	{
		if (!combat.getFoes().contains(ag))
		{
			throw new MazeException("not a foe group: "+ag);
		}

		return combat.getFoes().indexOf(ag);
	}

	/*-------------------------------------------------------------------------*/
	public static int getPCEngagementRange(
		UnifiedActor attacker,
		ActorGroup attackedGroup,
		Combat combat)
	{
		int groupIndex = combat.getFoes().indexOf(attackedGroup);
		return getPCEngagementRange(attacker, groupIndex, combat);
	}

	/*-------------------------------------------------------------------------*/
	public static int getPCEngagementRange(
		UnifiedActor attacker,
		int attackedGroupIndex,
		Combat combat)
	{
		boolean isFrontRow = combat.getPlayerParty().isFrontRow(attacker);

		//
		// First 2 foe groups are MELEE, third is EXTENDED, fourth is THROWN, rest are LONG.
		// Back row is +1 to all this
		//

		if (isFrontRow)
		{
			switch (attackedGroupIndex)
			{
				case 0: return ItemTemplate.WeaponRange.MELEE;
				case 1: return ItemTemplate.WeaponRange.MELEE;
				case 2: return ItemTemplate.WeaponRange.EXTENDED;
				case 3: return ItemTemplate.WeaponRange.THROWN;
				default: return ItemTemplate.WeaponRange.LONG;
			}
		}
		else
		{
			switch (attackedGroupIndex)
			{
				case 0: return ItemTemplate.WeaponRange.EXTENDED;
				case 1: return ItemTemplate.WeaponRange.EXTENDED;
				case 2: return ItemTemplate.WeaponRange.THROWN;
				default: return ItemTemplate.WeaponRange.LONG;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void attack(
		Combat combat,
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackAction attackAction,
		List<MazeEvent> events,
		AnimationContext animationContext)
	{
		BodyPart bodyPart = getRandomBodyPart(attacker, defender);

		AttackEvent attackEvent;
		AttackType attackType;
		if (attackAction.getAttackType() == null)
		{
			attackType = GameSys.getInstance().getAttackType(attackAction);

			if (attackType.getDamageType() != MagicSys.SpellEffectType.NONE)
			{
				attackAction.setDamageType(attackType.getDamageType());
			}
		}
		else
		{
			attackType = attackAction.getAttackType();
		}

		if (attackAction.getNrStrikes() == -1)
		{
			attackAction.setNrStrikes(GameSys.getInstance().getNrStrikes(
				attacker,
				defender,
				attackType,
				attackAction.getAttackWith()));
		}

		int stealthCost = getStealthCost(attacker, defender, attackType);
		attackEvent = new AttackEvent(
			attacker,
			defender,
			attackAction.getAttackWith(),
			attackType,
			bodyPart,
			stealthCost,
			attackAction.getNrStrikes());

		if (attackAction.isLightningStrike())
		{
			events.add(new LightningStrikeEvent(attacker));
			int strikes = GameSys.getInstance().getLightningStrikeNrStrikes(attackEvent);
			attackAction.setNrStrikes(attackAction.getNrStrikes() + strikes);
			attackEvent.incStrikes(strikes);
		}

		if (attackAction.isFirstAttack())
		{
			events.add(attackEvent);
		}

		events.addAll(attackAction.getAttackScript().getEvents());
		if (shouldAppendDelayEvent(attackAction.getAttackScript().getEvents()))
		{
			events.add(new DelayEvent(Maze.getInstance().getUserConfig().getCombatDelay()));
		}

		int hitPercent = GameSys.getInstance().calcHitPercent(attackEvent, attackAction);
		if (Dice.d100.roll() <= hitPercent)
		{
			DamagePacket damagePacket = GameSys.getInstance().calcDamage(attackEvent);
			combat.getCombatStatistics().captureAttackHit(attackAction, combat);

			if (GameSys.getInstance().isAttackDodged(attacker, defender, attackAction.getAttackWith()))
			{
				// dodge the attack
				events.add(new AttackDodgeEvent(defender));
			}
			else if (GameSys.getInstance().isAttackDeflected(attacker, defender, attackAction.getAttackWith()))
			{
				// deflected
				events.add(new AttackDeflectedEvent(attacker, defender, bodyPart));
			}
			else if (GameSys.getInstance().isAttackParried(attacker, defender, attackAction.getAttackWith()))
			{
				// parried
				events.add(new AttackParriedEvent(attacker, defender, bodyPart));
			}
			else
			{
				events.add(new AttackHitEvent(
					attacker,
					defender,
					bodyPart));

				events.add(new DamageEvent(
					defender,
					attacker,
					damagePacket,
					attackAction.getDamageType(),
					MagicSys.SpellEffectSubType.NORMAL_DAMAGE,
					attackAction.getAttackWith(),
					combat.getCombatStatistics()));

				// apply any spell effects to the victim
				if (damagePacket.getAmount() > 0)
				{
					List<AttackSpellEffects> effects = GameSys.getInstance().getAttackSpellEffects(attackAction);

					if (effects != null && effects.size() > 0)
					{
						for (AttackSpellEffects ase : effects)
						{
							if (ase.getSpellEffects() != null && ase.getSpellEffects().size() > 0)
							{
								events.addAll(
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
			events.add(new AttackMissEvent(
				attacker, defender));
			combat.getCombatStatistics().captureAttackMiss(attackAction, combat);
		}

		if (attackAction.getNrStrikes() > 1)
		{
			AttackAction aa = new AttackAction(
				attackAction.getTargetGroup(),
				attackAction.getAttackWith(),
				attackAction.getNrStrikes() - 1,
				attackAction.getAttackScript(),
				false,
				false,
				attackAction.getDamageType());
			aa.actor = attacker;
			aa.setDefender(defender);
			aa.isAttackingAllies = attackAction.isAttackingAllies;
			aa.setAttackType(attackType);
			events.add(new AnotherActionEvent(aa, combat));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean shouldAppendDelayEvent(List<MazeEvent> script)
	{
		if (script == null || script.isEmpty())
		{
			return false;
		}

		return !(script.get(script.size()-1) instanceof AnimationEvent);
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
	private static int getStealthCost(UnifiedActor attacker, UnifiedActor defender, AttackType attackType)
	{
		if (attackType == AttackType.NULL_ATTACK_TYPE)
		{
			return -1;
		}

		if (attackType.getName().equals(Stats.Modifiers.BACKSTAB) ||
			attackType.getName().equals(Stats.Modifiers.SNIPE))
		{
			return GameSys.getInstance().getBackstabSnipeCost(attacker, defender);
		}
		else
		{
			return -1;
		}
	}
}

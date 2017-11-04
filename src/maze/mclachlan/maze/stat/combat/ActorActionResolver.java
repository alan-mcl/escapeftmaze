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
import mclachlan.maze.map.script.LockOrTrap;
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
		UnifiedActor actor = action.getActor();

		if (!action.isAttackingAllies())
		{
			// at this point check and see if any conditions screw this actor over
			action.setActor(actor);
			action = GameSys.getInstance().checkConditions(action.getActor(), action);
		}

		AnimationContext animationContext = new AnimationContext(actor);

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		CombatantData combatantData = action.getActor().getCombatantData();
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
					result.addAll(
						resolveAttack(
							action.getActor(),
							(AttackAction)action,
							combat,
							result,
							animationContext));
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
				result.add(new DefendEvent(action.getActor()));
			}
			else if (action instanceof HideAction)
			{
				result.add(new HideAttemptEvent(action.getActor(), combat));
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
						action.getActor(),
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
					UnifiedActor caster = act.getActor();
					act = new SpecialAbilityAction(
						act.getDescription(),
						wildMagic.getTarget(),
						wildMagic.getSpell(),
						act.getCastingLevel());
					act.setActor(caster);
				}

				resolveSpecialAbility(
					combat,
					action.getActor(),
					act,
					result,
					animationContext);
			}
			else if (action instanceof UseItemAction)
			{
				resolveUseItem(
					combat,
					action.getActor(),
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
				result.add(new CowerInFearEvent(action.getActor()));
			}
			else if (action instanceof FreezeInTerrorAction)
			{
				result.add(new FreezeInTerrorEvent(action.getActor()));
			}
			else if (action instanceof StumbleBlindlyAction)
			{
				result.add(new StumbleBlindlyEvent(action.getActor()));
			}
			else if (action instanceof GagsHelplesslyAction)
			{
				result.add(new GagsHelplesslyEvent(action.getActor()));
			}
			else if (action instanceof RetchesNoisilyAction)
			{
				result.add(new RetchesNoisilyEvent(action.getActor()));
			}
			else if (action instanceof DancesWildlyAction)
			{
				result.add(new DancesWildlyEvent(action.getActor()));
			}
			else if (action instanceof LaughsMadlyAction)
			{
				result.add(new LaughsMadlyEvent(action.getActor()));
			}
			else if (action instanceof ItchesUncontrollablyAction)
			{
				result.add(new ItchesUncontrollablyEvent(action.getActor()));
			}
			else if (action instanceof AttackAlliesAction)
			{
				resolveAttackAlliesAction(combat, (AttackAlliesAction)action, result);
			}
			else if (action instanceof StrugglesMightilyAction)
			{
				StrugglesMightilyAction sma = (StrugglesMightilyAction)action;
				result.add(new StrugglesMightilyEvent(sma.getActor(), sma.condition));
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
			else if (action instanceof PickLockAction)
			{
				PickLockAction a = (PickLockAction)action;
				PlayerCharacter pc = (PlayerCharacter)a.getActor();
				LockOrTrap lockOrTrap = a.getPortal();
				result.add(new PickLockEvent(pc, lockOrTrap));
			}
			else if (action instanceof ForceOpenAction)
			{
				ForceOpenAction a = (ForceOpenAction)action;
				PlayerCharacter pc = (PlayerCharacter)a.getActor();
				LockOrTrap lockOrTrap = a.getPortal();
				result.add(new ForceOpenEvent(pc, lockOrTrap));
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
		UnifiedActor actor = action.getActor();
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
			act.setAttackingAllies(true);
			act.setActor(actor);

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

		Spell invokedSpell = item.getInvokedSpell();
		int castingLevel = item.getInvokedSpellLevel();

		if (invokedSpell == null)
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
			invokedSpell,
			castingLevel,
			useItemAction.getTarget());

		if (wildMagic != null)
		{
			invokedSpell = wildMagic.getSpell();
			useItemAction.setTarget(wildMagic.getTarget());
		}

		int targetType = invokedSpell.getTargetType();

		// practise any modifiers
		StatModifier useRequirements = item.getUseRequirements();
		for (Stats.Modifier mod : useRequirements.getModifiers().keySet())
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
				events.add(new SpellFizzlesEvent(actor, invokedSpell, castingLevel));
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
					targetType == MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER ||
					targetType == MagicSys.SpellTargetType.PARTY)
				{
					// these spell target types cannot backfire, simply fizzle
					// todo: may be amusing to provide various backfire effects here
					events.add(new SpellFizzlesEvent(actor, invokedSpell, castingLevel));
					return;
				}

				events.add(new SpellBackfiresEvent(actor, invokedSpell, castingLevel));
				useItemAction.setAttackingAllies(true);

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
						useItemAction.setTarget(actor.getActorGroup());
						break;
				}
			}
		}

		// the casting animation (todo: cast by player allies??)
		if (actor instanceof PlayerCharacter)
		{
			if (invokedSpell.getCastByPlayerScript() != null)
			{
				events.addAll(invokedSpell.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (invokedSpell.getCastByFoeScript() != null)
			{
				events.addAll(invokedSpell.getCastByFoeScript().getEvents());
			}
		}

		switch (invokedSpell.getTargetType())
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
						invokedSpell,
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
						invokedSpell,
						animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(
					SpellTargetUtils.resolvePartySpell(
						combat,
						actor,
						castingLevel,
						invokedSpell,
						animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
				events.addAll(
					SpellTargetUtils.resolvePartyButNotCasterSpell(
						combat,
						actor,
						castingLevel,
						invokedSpell,
						animationContext));
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(
					SpellTargetUtils.resolveFoeGroupSpell(
						combat,
						actor,
						useItemAction.getTarget(),
						castingLevel,
						castingLevel,
						invokedSpell,
						useItemAction,
						animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:

				LockOrTrap lockOrTrap;

				if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
				{
					lockOrTrap = Maze.getInstance().getCurrentChest();
				}
				else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
				{
					lockOrTrap = Maze.getInstance().getCurrentPortal();
				}
				else
				{
					break;
				}

				events.addAll(SpellTargetUtils.resolveLockOrTrapSpell(
					lockOrTrap,
					invokedSpell,
					(PlayerCharacter)actor,
					castingLevel));
				break;

			case MagicSys.SpellTargetType.CASTER:
				events.addAll(
					SpellTargetUtils.resolveCasterSpell(
						invokedSpell,
						combat,
						actor,
						castingLevel,
						invokedSpell.getEffects().getRandom(),
						animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(
					SpellTargetUtils.resolveAllFoesSpell(
						combat,
						actor,
						castingLevel,
						castingLevel,
						invokedSpell,
						useItemAction,
						animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(
					SpellTargetUtils.resolveTileSpell(
						invokedSpell,
						combat,
						actor,
						castingLevel,
						invokedSpell.getEffects().getRandom(),
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
						invokedSpell,
						useItemAction));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(
					SpellTargetUtils.resolveCloudAllGroupsSpell(
						combat,
						actor,
						castingLevel,
						castingLevel,
						invokedSpell,
						useItemAction));
				break;
			case MagicSys.SpellTargetType.ITEM:
				events.addAll(
					SpellTargetUtils.resolveItemSpell(
						combat,
						actor,
						castingLevel,
						castingLevel,
						invokedSpell,
						(Item)useItemAction.getTarget()));
			default:
				throw new MazeException("Unrecognised spell target type: "+invokedSpell.getTargetType());
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
		Spell specialAbilitySpell = action.getSpell();

		if (!GameSys.getInstance().canPaySpellCost(
			action.getSpell(), action.getCastingLevel(), actor))
		{
			events.add(new FailureEvent());
			return;
		}

		// at this point the spell has been successfully cast
		events.add(new SpecialAbilityUseEvent(
			actor, specialAbilitySpell, action.getCastingLevel(), action.getDescription()));

		// the casting animation (todo: cast by player allies?)
		if (actor instanceof PlayerCharacter)
		{
			if (specialAbilitySpell.getCastByPlayerScript() != null)
			{
				events.addAll(specialAbilitySpell.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (specialAbilitySpell.getCastByFoeScript() != null)
			{
				events.addAll(specialAbilitySpell.getCastByFoeScript().getEvents());
			}
		}

		switch (specialAbilitySpell.getTargetType())
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				events.addAll(
					SpellTargetUtils.resolveFoeSpell(
						combat,
						actor,
						action.getTarget(),
						action.getCastingLevel(),
						specialAbilitySpell.getLevel(),
						specialAbilitySpell,
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
						specialAbilitySpell,
						animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(SpellTargetUtils.resolvePartySpell(
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell,
					animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
				events.addAll(SpellTargetUtils.resolvePartyButNotCasterSpell(
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell,
					animationContext));
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(SpellTargetUtils.resolveFoeGroupSpell(
					combat,
					actor,
					action.getTarget(),
					specialAbilitySpell.getLevel(),
					action.getCastingLevel(),
					specialAbilitySpell,
					action,
					animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				events.addAll(SpellTargetUtils.resolveLockOrTrapSpell(
					Maze.getInstance().getCurrentChest(),
					specialAbilitySpell,
					(PlayerCharacter)actor,
					action.getCastingLevel()));
				break;
			case MagicSys.SpellTargetType.CASTER:
				events.addAll(SpellTargetUtils.resolveCasterSpell(
					specialAbilitySpell,
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(SpellTargetUtils.resolveAllFoesSpell(
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell.getLevel(),
					specialAbilitySpell,
					action,
					animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(SpellTargetUtils.resolveTileSpell(
					specialAbilitySpell,
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				events.addAll(SpellTargetUtils.resolveCloudOneGroupSpell(
					combat,
					actor,
					action.getTarget(),
					action.getCastingLevel(),
					specialAbilitySpell.getLevel(),
					specialAbilitySpell,
					action));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(SpellTargetUtils.resolveCloudAllGroupsSpell(
					combat,
					actor,
					action.getCastingLevel(),
					specialAbilitySpell.getLevel(),
					specialAbilitySpell,
					action));
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+specialAbilitySpell.getTargetType());
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

		final Spell spell = spellAction.getSpell();

		if (!isCloudSpell)
		{
			// practise modifiers

			GameSys.getInstance().practice(caster, spell.getPrimaryModifier(), 1);
			if (Dice.d2.roll() == 2)
			{
				GameSys.getInstance().practice(caster, spell.getSecondaryModifier(), 1);
			}
		}

		int castingLevel = spellAction.getCastingLevel();
		int targetType = spell.getTargetType();
		if (caster instanceof GameSys.DummyCaster)
		{
			// dodgy hack to allow for traps and such
			events.add(new SpellCastEvent(caster, spell, castingLevel)
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
				events.add(new SpellCastEvent(caster, spell, castingLevel)
				{
					public String getText()
					{
						return spell.getDescription();
					}
				});
			}
			else
			{
				events.add(new SpellCastEvent(caster, spell, castingLevel));
			}
		}

		if (!isCloudSpell)
		{
			if (caster instanceof PlayerCharacter &&
				!((PlayerCharacter)caster).canCast(spell))
			{
				events.add(new SpellFizzlesEvent(caster, spell, castingLevel));
				return events;
			}

			if (!GameSys.getInstance().canPaySpellCost(
				spellAction.getSpell(), spellAction.getCastingLevel(), caster))
			{
				events.add(new SpellFizzlesEvent(caster, spell, castingLevel));
				return events;
			}
		}

		if (!isCloudSpell)
		{
			int spellFailureChance = GameSys.getInstance().getSpellFailureChance(caster, spell, castingLevel);

			int spellFailureRoll = Dice.d100.roll();

			if (spellFailureRoll <= spellFailureChance)
			{
				if (spellFailureRoll*5 > spellFailureChance)
				{
					// spell fizzles
					events.add(new SpellFizzlesEvent(caster, spell, castingLevel));
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
						targetType == MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER ||
						combat == null ||
						combat.getNrOfLivingEnemies(caster) == 0)
					{
						// these spell target types cannot backfire (or there are no
						// foes to target), simply fizzle
						// todo: may be amusing to provide various backfire effects here
						events.add(new SpellFizzlesEvent(caster, spell, castingLevel));
						return events;
					}

					events.add(new SpellBackfiresEvent(caster, spell, castingLevel));

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
			if (spell.getCastByPlayerScript() != null)
			{
				events.addAll(spell.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (spell.getCastByFoeScript() != null)
			{
				events.addAll(spell.getCastByFoeScript().getEvents());
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
					spell.getLevel(),
					spell,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALLY:
				events.addAll(SpellTargetUtils.resolveAllySpell(
					combat,
					caster,
					spellAction.getTarget(),
					castingLevel,
					spell,
					animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY:
				events.addAll(SpellTargetUtils.resolvePartySpell(
					combat,
					caster,
					castingLevel,
					spell,
					animationContext));
				break;
			case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
				events.addAll(SpellTargetUtils.resolvePartyButNotCasterSpell(
					combat,
					caster,
					castingLevel,
					spell,
					animationContext));
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				events.addAll(SpellTargetUtils.resolveFoeGroupSpell(
					combat,
					caster,
					spellAction.getTarget(),
					spell.getLevel(),
					castingLevel,
					spell,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:

				LockOrTrap lockOrTrap;

				if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
				{
					lockOrTrap = Maze.getInstance().getCurrentChest();
				}
				else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
				{
					lockOrTrap = Maze.getInstance().getCurrentPortal();
				}
				else
				{
					break;
				}

				events.addAll(SpellTargetUtils.resolveLockOrTrapSpell(
					lockOrTrap,
					spell,
					(PlayerCharacter)caster,
					castingLevel));
				break;

			case MagicSys.SpellTargetType.CASTER:
				events.addAll(SpellTargetUtils.resolveCasterSpell(
					spell,
					combat,
					caster,
					castingLevel,
					spell.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				events.addAll(SpellTargetUtils.resolveAllFoesSpell(
					combat,
					caster,
					castingLevel,
					spell.getLevel(),
					spell,
					spellAction,
					animationContext));
				break;
			case MagicSys.SpellTargetType.TILE:
				events.addAll(SpellTargetUtils.resolveTileSpell(
					spell,
					combat,
					caster,
					castingLevel,
					spell.getEffects().getRandom(),
					animationContext));
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				events.addAll(SpellTargetUtils.resolveCloudOneGroupSpell(
					combat,
					caster,
					spellAction.getTarget(),
					castingLevel,
					spell.getLevel(),
					spell,
					spellAction));
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				events.addAll(SpellTargetUtils.resolveCloudAllGroupsSpell(
					combat,
					caster,
					castingLevel,
					spell.getLevel(),
					spell,
					spellAction));
				break;
			case MagicSys.SpellTargetType.ITEM:
				events.addAll(SpellTargetUtils.resolveItemSpell(
					combat,
					caster,
					castingLevel,
					spell.getLevel(),
					spell,
					(Item)spellAction.getTarget()));
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+ targetType);
		}

		return events;
	}

	/*-------------------------------------------------------------------------*/
	private static List<MazeEvent> resolveAttack(
		UnifiedActor actor,
		AttackAction attackAction,
		Combat combat,
		List<MazeEvent> events,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		ActorGroup attackedGroup;
		attackedGroup = attackAction.getTargetGroup();

		if (attackedGroup.numAlive() < 1)
		{
			// all in this group are dead do nothing
			return result;
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
				return result;
			}
		}

		if (defender == null)
		{
			// cannot attack
			return result;
		}

		attackAction.setDefender(defender);

		result.addAll(attack(combat, actor, defender, attackAction, animationContext));

		return result;
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
		if (action.isAttackingAllies())
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
				double weight = 1.0 / (1.0 + actor.getModifier(Stats.Modifier.OBFUSCATION));
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
	public static List<MazeEvent> attack(
		Combat combat,
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackAction attackAction,
		AnimationContext animationContext)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (attackAction.getNrStrikes() == -1)
		{
			attackAction.setNrStrikes(GameSys.getInstance().getNrStrikes(
				attacker,
				defender,
				attackAction.getAttackType(),
				attackAction.getAttackWith()));
		}

		if (attackAction.isLightningStrike())
		{
			result.add(new LightningStrikeEvent(attacker));
			int strikes = GameSys.getInstance().getLightningStrikeNrStrikes();
			attackAction.setNrStrikes(attackAction.getNrStrikes() + strikes);
		}

		AttackEvent attackEvent = new AttackEvent(
			combat,
			attacker,
			defender,
			attackAction.getAttackWith(),
			attackAction.getAttackType(),
			0,
			attackAction.getNrStrikes(),
			attackAction.getAttackScript(),
			attackAction.getDamageType(),
			animationContext,
			attackAction.getModifiers());

		result.addAll(attackAction.getAttackScript().getEvents());
		result.add(attackEvent);

		return result;
	}
}

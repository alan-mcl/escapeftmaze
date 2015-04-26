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
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Combat
{
	/**
	 * The adventurers
	 */ 
	private PlayerParty party;
	/**
	 * Summoned creatures
	 */
	private List<FoeGroup> partyAllies;

	/**
	 * Foes, multiple groups thereof
	 */
	private List<FoeGroup> foes = new ArrayList<FoeGroup>();

	/**
	 * All actors in this combat
	 */
	private List<UnifiedActor> actors = new ArrayList<UnifiedActor>();
	
	private Map<UnifiedActor, ActorActionIntention> combatIntentions =
		new HashMap<UnifiedActor, ActorActionIntention>();
	
	private List<MazeEvent> events;

	private List<Foe> deadFoes = new ArrayList<Foe>();

	private Comparator comparator = new CombatActionComparator();

	private AmbushStatus ambushStatus;

	private AnimationContext animationContext;
	
	private static Dice blinkDice = new Dice(1, 6, 6);

	/**
	 * Statistics collected during the current combat
	 */
	private CombatStatistics combatStats;

	public enum AmbushStatus
	{
		NONE,
		PARTY_AMBUSHES_FOES,
		FOES_AMBUSH_PARTY,
		PARTY_MAY_EVADE_FOES,
		FOES_MAY_EVADE_PARTY
	}
	
	/*-------------------------------------------------------------------------*/
	public Combat(PlayerParty party, List<FoeGroup> foes, boolean testSurprise)
	{
		Maze.getInstance().setCurrentCombat(this);
		this.foes = foes;
		this.party = party;
		// party allies initially empty
		this.partyAllies = new ArrayList<FoeGroup>();

		// begin collecting stats
		this.combatStats = new CombatStatistics(this.toString());
		combatStats.captureCombatStart(party, foes);

		// init the party meta data
		addActors(party);

		// init the foes meta data
		for (FoeGroup group : foes)
		{
			addActors(group);

			for (UnifiedActor actor : group.getActors())
			{
				// NPCs and other UnifiedActor subclasses can end up in here
				if (actor instanceof Foe)
				{
					((Foe)actor).initialEquip();
				}
			}
		}

		if (testSurprise)
		{
			// determine surprise
			int partyValue = GameSys.getInstance().getStealthValue(
				Maze.getInstance().getCurrentTile(), party);
			int foesValue = GameSys.getInstance().getStealthValue(
				Maze.getInstance().getCurrentTile(), foes);

			partyValue += Dice.d10.roll();
			foesValue += Dice.d10.roll();

			if (partyValue > foesValue+20)
			{
				// check and see if there are any "cannot evade" foes
				boolean cannotEvade = false;
				for (FoeGroup fg : foes)
				{
					for(Foe f : fg.getFoes())
					{
						cannotEvade |= f.cannotBeEvaded();
					}
				}
				if (cannotEvade)
				{
					ambushStatus = AmbushStatus.PARTY_AMBUSHES_FOES;
				}
				else
				{
					ambushStatus = AmbushStatus.PARTY_MAY_EVADE_FOES;
				}
			}
			else if (partyValue > foesValue+10)
			{
				ambushStatus = AmbushStatus.PARTY_AMBUSHES_FOES;
			}
			else if (foesValue > partyValue+20)
			{
				ambushStatus = AmbushStatus.FOES_MAY_EVADE_PARTY;
			}
			else if (foesValue > partyValue+10)
			{
				ambushStatus = AmbushStatus.FOES_AMBUSH_PARTY;
			}
			else
			{
				ambushStatus = AmbushStatus.NONE;
			}
		}
		else
		{
			ambushStatus = AmbushStatus.NONE;
		}

		combatStats.captureAmbushStatus(ambushStatus);
	}

	/*-------------------------------------------------------------------------*/
	public void addFoeAllies(List<FoeGroup> allies)
	{
		this.foes.addAll(allies);
		for (FoeGroup fg : allies)
		{
			addActors(fg);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addPartyAllies(List<FoeGroup> allies)
	{
		this.partyAllies.addAll(allies);
		for (FoeGroup fg : allies)
		{
			addActors(fg);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addActors(ActorGroup actors)
	{
		int max = actors.getActors().size();
		for (int i = 0; i < max; i++)
		{
			UnifiedActor pc = actors.getActors().get(i);
			CombatantData metaData = new CombatantData();
			metaData.setCombat(this);
			metaData.setGroup(actors);
			pc.setCombatantData(metaData);
			this.actors.add(pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public AmbushStatus getAmbushStatus()
	{
		return ambushStatus;
	}

	/*-------------------------------------------------------------------------*/
	public AnimationContext getAnimationContext()
	{
		return animationContext;
	}

	/*-------------------------------------------------------------------------*/
	public List<FoeGroup> getFoes()
	{
		return foes;
	}

	/*-------------------------------------------------------------------------*/
	public List<ActorGroup> getFoesOf(UnifiedActor actor)
	{
		List<ActorGroup> result = new ArrayList<ActorGroup>();
		if (actor instanceof PlayerCharacter)
		{
			result.addAll(foes);
			return result;
		}
		else
		{
			result.add(party);
			result.addAll(getPartyAllies());
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<FoeGroup> getPartyAllies()
	{
		return partyAllies;
	}

	/*-------------------------------------------------------------------------*/
	public List<ActorGroup> getAlliesOf(UnifiedActor actor)
	{
		List<ActorGroup> result = new ArrayList<ActorGroup>();
		if (actor instanceof PlayerCharacter)
		{
			result.add(party);
			result.addAll(getPartyAllies());
			return result;
		}
		else
		{
			result.addAll(foes);
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getNrOfLivingEnemies(UnifiedActor actor)
	{
		int result = 0;

		List<ActorGroup> foesOf = getFoesOf(actor);
		for (ActorGroup ag : foesOf)
		{
			result += ag.numAlive();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param partyIntentions
	 * 	Array of {@link ActorActionOption}s corresponding to party members.
	 * @param foeIntentions
	 * 	List of arrays of {@link ActorActionOption}s corresponding to foes
	 * 
	 * @return 
	 * 	An iterator of combat actions
	 */ 
	public Iterator combatRound(
		ActorActionIntention[] partyIntentions,
		List<ActorActionIntention[]> foeIntentions)
	{
		this.startRound();
		this.initCombatActions(partyIntentions, foeIntentions);
		this.calcInitiative();
		List<CombatAction> orderOfPlay = this.getOrderOfPlay();
		
		return orderOfPlay.iterator();
	}

	/*-------------------------------------------------------------------------*/
	private void startRound()
	{
		for (UnifiedActor actor : this.actors)
		{
			CombatantData metaData = actor.getCombatantData();
			metaData.startRound();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endRound()
	{
		this.events.clear();
		for (int i = foes.size()-1; i >=0; i--)
		{
			if (foes.get(i).numAlive() == 0)
			{
				foes.remove(i);
			}
		}

		Maze.log("checking end of round berserk status...");
		// chance of berserking every round
		for (UnifiedActor actor : this.actors)
		{
			if (actor.getModifier(Stats.Modifiers.BERSERKER) > 0)
			{
				if (GameSys.getInstance().actorGoesBeserk(actor))
				{
					appendEvent(new BerserkEvent(actor));
				}
			}
		}

		Maze.log("updating cloud spells...");
		for (int i = 0; i < foes.size(); i++)
		{
			Maze.log("for foes...");
			processCloudSpells(foes.get(i), i);
		}
		for (int i = 0; i < partyAllies.size(); i++)
		{
			Maze.log("for allies...");
			processCloudSpells(partyAllies.get(i), i);
		}
		Maze.log("for party...");
		processCloudSpells(party, 0);

		// any surprise ends here
		this.ambushStatus = AmbushStatus.NONE;

		for (UnifiedActor actor : this.actors)
		{
			CombatantData metaData = actor.getCombatantData();
			metaData.endRound();
		}

		combatStats.incCombatRounds();

		return this.events;
	}

	/*-------------------------------------------------------------------------*/
	private void processCloudSpells(ActorGroup actorGroup, int index)
	{
		List<CloudSpell> list = actorGroup.getCloudSpells();
		ListIterator<CloudSpell> li = list.listIterator();
		while (li.hasNext())
		{
			CloudSpell cloudSpell = li.next();

			SpellAction spellAction = new SpellAction(
				actorGroup, cloudSpell.getSpell(), cloudSpell.getCastingLevel());

			if (cloudSpell.isAttackingAllies())
			{
				spellAction.isAttackingAllies = true;
			}

			resolveSpell(
				cloudSpell.getSource(),
				spellAction,
				true);

			cloudSpell.endOfTurn();

			// Expire conditions
			if (cloudSpell.getDuration() < 0)
			{
				Maze.log("cloud spell expired");
				li.remove();
				cloudSpell.expire();
			}
			else
			{
				Maze.log("duration "+cloudSpell.getDuration());
				Maze.log("strength "+cloudSpell.getStrength());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void endCombat()
	{
		this.combatStats.captureCombatEnd(party.numAlive() > 0);
		for (UnifiedActor actor : this.actors)
		{
			actor.setCombatantData(null);
		}
		Maze.getInstance().setCurrentCombat(null);
		PlayerParty party = Maze.getInstance().getParty();
		if (party != null)
		{
			party.clearCloudSpells();
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	An array of events that take place as a result of this combat action
	 */ 
	public List<MazeEvent> resolveAction(CombatAction action)
	{
		UnifiedActor actor = action.actor;

		if (!action.isAttackingAllies)
		{
			// at this point check and see if any conditions screw this actor over
			action.actor = actor;
			action = GameSys.getInstance().checkConditions(action.actor, action);
		}

		this.animationContext = null;
		this.events = new LinkedList<MazeEvent>();
		
		CombatantData data = action.actor.getCombatantData();

		if (data == null)
		{
			// combat has ended?
			return null;
		}
		
		// set the action
		data.setCurrentAction(action);
		if (((ambushStatus == AmbushStatus.FOES_AMBUSH_PARTY ||
				ambushStatus == AmbushStatus.FOES_MAY_EVADE_PARTY) && actor instanceof Foe) ||
			((ambushStatus == AmbushStatus.PARTY_AMBUSHES_FOES ||
			ambushStatus == AmbushStatus.PARTY_MAY_EVADE_FOES) && actor instanceof PlayerCharacter))
		{
			StatModifier sm = GameSys.getInstance().getSurpriseModifiers(actor);
			data.getMiscModifiers().setModifiers(sm);
		}
		
		if (data.isActive())
		{
			if (action instanceof AttackAction)
			{
				this.resolveAttack(action.actor, (AttackAction)action);
			}
			else if (action instanceof DefendAction)
			{
				// defending doesn't achieve much
				this.appendEvent(new DefendEvent(action.actor));
			}
			else if (action instanceof HideAction)
			{
				this.resolveHide(action.actor);
			}
			else if (action instanceof SpellAction)
			{
				SpellAction spellAction = (SpellAction)action;

				SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
					this,
					spellAction.getActor(),
					spellAction.getSpell(),
					spellAction.getCastingLevel(),
					spellAction.getTarget());

				if (wildMagic != null)
				{
					spellAction = wildMagic;
				}

				this.resolveSpell(action.actor, spellAction, false);
			}
			else if (action instanceof SpellSilencedAction)
			{
				SpellSilencedAction ssa = (SpellSilencedAction)action;
				appendEvent(new SpellCastEvent(actor, ssa.getSpell(), ssa.getCastingLevel()));
				appendEvent(new SpellFizzlesEvent(actor, ssa.getSpell(), ssa.getCastingLevel()));
			}
			else if (action instanceof SpecialAbilityAction)
			{
				SpecialAbilityAction act = (SpecialAbilityAction)action;

				SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
					this,
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

				this.resolveSpecialAbility(action.actor, act);
			}
			else if (action instanceof UseItemAction)
			{
				this.resolveUseItem(action.actor, (UseItemAction)action);
			}
			else if (action instanceof EquipAction)
			{
				this.resolveEquip(action.actor);
			}
			else if (action instanceof RunAwayAction)
			{
				this.resolveRunAway(action.actor);
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
				appendEvent(new CowerInFearEvent(action.actor));
			}
			else if (action instanceof FreezeInTerrorAction)
			{
				appendEvent(new FreezeInTerrorEvent(action.actor));
			}
			else if (action instanceof StumbleBlindlyAction)
			{
				appendEvent(new StumbleBlindlyEvent(action.actor));
			}
			else if (action instanceof GagsHelplesslyAction)
			{
				appendEvent(new GagsHelplesslyEvent(action.actor));
			}
			else if (action instanceof RetchesNoisilyAction)
			{
				appendEvent(new RetchesNoisilyEvent(action.actor));
			}
			else if (action instanceof DancesWildlyAction)
			{
				appendEvent(new DancesWildlyEvent(action.actor));
			}
			else if (action instanceof LaughsMadlyAction)
			{
				appendEvent(new LaughsMadlyEvent(action.actor));
			}
			else if (action instanceof ItchesUncontrollablyAction)
			{
				appendEvent(new ItchesUncontrollablyEvent(action.actor));
			}
			else if (action instanceof AttackAlliesAction)
			{
				this.resolveAttackAlliesAction((AttackAlliesAction)action);
			}
			else if (action instanceof StrugglesMightilyAction)
			{
				StrugglesMightilyAction sma = (StrugglesMightilyAction)action;
				appendEvent(new StrugglesMightilyEvent(sma.actor, sma.condition));
			}
			else if (action instanceof BlinkAction)
			{
				if (GameSys.getInstance().isActorBlinkedOut(actor))
				{
					appendEvent(new BlinkInEvent(actor));
				}
				else
				{
					appendEvent(new BlinkOutEvent(actor));
				}
			}
			else
			{
				throw new MazeException("Unrecognised combat action: "+action);
			}
		}
		
		// clear the action
		data.setCurrentAction(null);

		return this.events;
	}

	/*-------------------------------------------------------------------------*/
	private void resolveAttackAlliesAction(AttackAlliesAction action)
	{
		// get a random target group of allies
		UnifiedActor actor = action.actor;
		int groups = getNrOfAlliedGroups(actor);
		int targetGroup = GameSys.getInstance().nextInt(groups);

		// get an attack action
		ActorActionIntention intention;
		if (actor instanceof Foe)
		{
			Foe foe = (Foe)actor;
			intention = foe.getFoeAttackIntention(
				ItemTemplate.WeaponRange.MELEE,
				new Dice(1, 1, -1), this);
		}
		else
		{
			intention = new AttackIntention(
				getAlliesOf(actor).get(targetGroup),
				this,
				action.getAttackWith());
		}
		List<CombatAction> actions = getActorCombatActions(actor, intention);

		for (CombatAction act : actions)
		{
			act.isAttackingAllies = true;
			act.actor = actor;

			// execute!
			resolveAction(act);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveRunAway(UnifiedActor actor)
	{
		appendEvent(new RunAwayAttemptEvent(actor));

		if (isPlayerCharacter(actor))
		{
			int nrFoes = 0;
			for (FoeGroup fg : foes)
			{
				nrFoes += fg.numAlive();
			}

			boolean success = GameSys.getInstance().attemptToRunAway(actor, nrFoes);

			if (!success)
			{
				appendEvent(new RunAwayFailedEvent(actor));
			}
			else
			{
				appendEvent(new SuccessEvent());
				appendEvent(new RunAwaySuccessEvent(actor));
			}
		}
		else
		{
			int nrFoes = 0;
			nrFoes += party.numAlive();

			for (FoeGroup fg : partyAllies)
			{
				nrFoes += fg.numAlive();
			}

			boolean success = GameSys.getInstance().attemptToRunAway(actor, nrFoes);

			if (!success)
			{
				appendEvent(new RunAwayFailedEvent(actor));
			}
			else
			{
				appendEvent(new SuccessEvent());
				appendEvent(new RunAwaySuccessEvent(actor));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveEquip(UnifiedActor actor)
	{
		appendEvent(new EquipEvent(actor));
	}

	/*-------------------------------------------------------------------------*/
	private void resolveUseItem(UnifiedActor actor, UseItemAction useItemAction)
	{
		Item item = useItemAction.getItem();
		appendEvent(new ItemUseEvent(actor, item));

		Spell s = item.getInvokedSpell();
		int castingLevel = item.getInvokedSpellLevel();

		if (s == null)
		{
			// no invoked spell on this item
			appendEvent(new NoEffectEvent());
			return;
		}

		if (castingLevel == 0)
		{
			// zero casting level implies that the effective level should scale
			// based on the user's skill level

			castingLevel = GameSys.getInstance().getItemUseCastingLevel(actor, item);
		}

		SpellAction wildMagic = GameSys.getInstance().applyWildMagic(
			this,
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
				this.appendEvent(new ActorLearnsSpellEvent(pc, spell));
			}
			else
			{
				this.appendEvent(new FailureEvent());
			}
			
			// early exit
			return;
		}

		// set up the animation context
		animationContext = new AnimationContext(actor);

		int failureChance = GameSys.getInstance().getItemUseFailureChance(
			actor, item);

		int failureRoll = Dice.d100.roll();

		boolean canBackfire = GameSys.getInstance().canBackfire(item);

		if (failureRoll <= failureChance)
		{
			if (failureRoll*4 > failureChance || !canBackfire)
			{
				// spell fizzles
				appendEvent(new SpellFizzlesEvent(actor, s, castingLevel));
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
					appendEvent(new SpellFizzlesEvent(actor, s, castingLevel));
					return;
				}

				appendEvent(new SpellBackfiresEvent(actor, s, castingLevel));
				useItemAction.isAttackingAllies = true;

				// warp the target
				switch(targetType)
				{
					case MagicSys.SpellTargetType.ALL_FOES:
					case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
						// todo: no appropriate ALL_ALLIES target type?
						targetType = MagicSys.SpellTargetType.PARTY;
						useItemAction.setTarget(getActorGroup(actor));
						break;

					case MagicSys.SpellTargetType.FOE:
						// todo: target actors own group
						targetType = MagicSys.SpellTargetType.ALLY;
						List<UnifiedActor> allies = getAllAlliesOf(actor);
						Dice d = new Dice(1, allies.size(), -1);
						SpellTarget target = allies.get(d.roll());
						useItemAction.setTarget(target);
						break;

					case MagicSys.SpellTargetType.FOE_GROUP:
					case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
						// todo: target actors own group
						targetType = MagicSys.SpellTargetType.PARTY;
						useItemAction.setTarget(getActorGroup(actor));
						break;
				}
			}
		}

		// the casting animation (todo: cast by player allies??)
		if (actor instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				appendEvents(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				appendEvents(s.getCastByFoeScript().getEvents());
			}
		}

		switch (s.getTargetType())
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				SpellTargetUtils.resolveFoeSpell(this,
					actor,
					useItemAction.getTarget(),
					castingLevel,
					castingLevel,
					s,
					useItemAction);
				break;
			case MagicSys.SpellTargetType.ALLY:
				SpellTargetUtils.resolveAllySpell(this,
					actor,
					useItemAction.getTarget(),
					castingLevel,
					s);
				break;
			case MagicSys.SpellTargetType.PARTY:
				SpellTargetUtils.resolvePartySpell(this,
					actor,
					castingLevel,
					s);
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				SpellTargetUtils.resolveFoeGroupSpell(this,
					actor,
					useItemAction.getTarget(),
					castingLevel,
					castingLevel,
					s,
					useItemAction);
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
			case MagicSys.SpellTargetType.ITEM:
				//nothing to do
				break;
			case MagicSys.SpellTargetType.CASTER:
				SpellTargetUtils.resolveCasterSpell(this,
					actor,
					castingLevel,
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				SpellTargetUtils.resolveAllFoesSpell(this,
					actor,
					castingLevel,
					castingLevel,
					s,
					useItemAction);
				break;
			case MagicSys.SpellTargetType.TILE:
				SpellTargetUtils.resolveTileSpell(this,
					actor,
					castingLevel,
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				SpellTargetUtils.resolveCloudOneGroupSpell(this,
					actor,
					useItemAction.getTarget(),
					castingLevel,
					castingLevel,
					s,
					useItemAction);
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				SpellTargetUtils.resolveCloudAllGroupsSpell(this,
					actor,
					castingLevel,
					castingLevel,
					s,
					useItemAction);
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+s.getTargetType());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveHide(UnifiedActor actor)
	{
		appendEvent(new HideAttemptEvent(actor));

		int hideChance = GameSys.getInstance().getHideChance(
			actor, getAllFoesOf(actor), getAllAlliesOf(actor));

		if (Dice.d100.roll() <= hideChance)
		{
			appendEvent(new HideSucceedsEvent(actor));
		}
		else
		{
			appendEvent(new HideFailsEvent(actor));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveSpecialAbility(UnifiedActor actor, SpecialAbilityAction action)
	{
		Spell s = action.getSpell();

		// at this point the spell has been successfully cast
		appendEvent(new SpecialAbilityUseEvent(actor, s, action.getCastingLevel(), action.getDescription()));

		// set up the animation context
		animationContext = new AnimationContext(actor);

		// the casting animation (todo: cast by player allies?)
		if (actor instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				appendEvents(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				appendEvents(s.getCastByFoeScript().getEvents());
			}
		}

		switch (s.getTargetType())
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				SpellTargetUtils.resolveFoeSpell(this,
					actor,
					action.getTarget(),
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action);
				break;
			case MagicSys.SpellTargetType.ALLY:
				SpellTargetUtils.resolveAllySpell(this,
					actor,
					action.getTarget(),
					action.getCastingLevel(),
					s);
				break;
			case MagicSys.SpellTargetType.PARTY:
				SpellTargetUtils.resolvePartySpell(this,
					actor,
					action.getCastingLevel(),
					s);
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				SpellTargetUtils.resolveFoeGroupSpell(this,
					actor,
					action.getTarget(),
					s.getLevel(),
					action.getCastingLevel(),
					s,
					action);
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				//nothing to do
				break;
			case MagicSys.SpellTargetType.CASTER:
				SpellTargetUtils.resolveCasterSpell(this,
					actor,
					action.getCastingLevel(),
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				SpellTargetUtils.resolveAllFoesSpell(this,
					actor,
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action);
				break;
			case MagicSys.SpellTargetType.TILE:
				SpellTargetUtils.resolveTileSpell(this,
					actor,
					action.getCastingLevel(),
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				SpellTargetUtils.resolveCloudOneGroupSpell(this,
					actor,
					action.getTarget(),
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action);
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				SpellTargetUtils.resolveCloudAllGroupsSpell(this,
					actor,
					action.getCastingLevel(),
					s.getLevel(),
					s,
					action);
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+s.getTargetType());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveSpell(
		final UnifiedActor caster,
		SpellAction spellAction,
		boolean isCloudSpell)
	{
		Maze.log(Log.DEBUG, "resolving ["+caster.getName()+"] spell action ["+
			spellAction+" ("+isCloudSpell+")]");
		
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
			appendEvent(new SpellCastEvent(caster, s, castingLevel)
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
				appendEvent(new SpellCastEvent(caster, s, castingLevel)
				{
					public String getText()
					{
						return s.getDescription();
					}
				});
			}
			else
			{
				appendEvent(new SpellCastEvent(caster, s, castingLevel));
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
				appendEvent(new SpellFizzlesEvent(caster, s, castingLevel));
				return;
			}

			if (caster instanceof PlayerCharacter &&
				!((PlayerCharacter)caster).canCast(s))
			{
				appendEvent(new SpellFizzlesEvent(caster, s, castingLevel));
				return;
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
					appendEvent(new SpellFizzlesEvent(caster, s, castingLevel));
					return;
				}
				else
				{
					// spell backfires
					if (targetType == MagicSys.SpellTargetType.ITEM ||
						targetType == MagicSys.SpellTargetType.LOCK_OR_TRAP ||
						targetType == MagicSys.SpellTargetType.NPC ||
						targetType == MagicSys.SpellTargetType.TILE ||
						targetType == MagicSys.SpellTargetType.PARTY ||
						getNrOfLivingEnemies(caster) == 0)
					{
						// these spell target types cannot backfire (or there are no
						// foes to target), simply fizzle
						// todo: may be amusing to provide various backfire effects here
						appendEvent(new SpellFizzlesEvent(caster, s, castingLevel));
						return;
					}

					appendEvent(new SpellBackfiresEvent(caster, s, castingLevel));

					// warp the target
					switch(targetType)
					{
						case MagicSys.SpellTargetType.PARTY:
							targetType = MagicSys.SpellTargetType.FOE_GROUP;
							List<ActorGroup> foesOf = getFoesOf(caster);
							spellAction.setTarget(foesOf.get(GameSys.getInstance().nextInt(foesOf.size())));
							break;

						case MagicSys.SpellTargetType.ALLY:
						case MagicSys.SpellTargetType.CASTER:
							targetType = MagicSys.SpellTargetType.FOE;
							spellAction.setTarget(getRandomFoeOf(caster));
							break;

						case MagicSys.SpellTargetType.ALL_FOES:
						case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
							// todo: no appropriate ALL_ALLIES target type?
							targetType = MagicSys.SpellTargetType.PARTY;
							spellAction.setTarget(getActorGroup(caster));
							break;

						case MagicSys.SpellTargetType.FOE:
							targetType = MagicSys.SpellTargetType.ALLY;
							spellAction.setTarget(getRandomAllyOf(caster));
							break;

						case MagicSys.SpellTargetType.FOE_GROUP:
						case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
							targetType = MagicSys.SpellTargetType.PARTY;
							spellAction.setTarget(getActorGroup(caster));
							break;
					}
				}
			}
		}

		// at this point the spell has been successfully cast
		// set up the animation context
		animationContext = new AnimationContext(caster);

		// the casting animation (todo: cast by player allies?)
		if (caster instanceof PlayerCharacter)
		{
			if (s.getCastByPlayerScript() != null)
			{
				appendEvents(s.getCastByPlayerScript().getEvents());
			}
		}
		else
		{
			if (s.getCastByFoeScript() != null)
			{
				appendEvents(s.getCastByFoeScript().getEvents());
			}
		}

		switch (targetType)
		{
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.NPC:
				SpellTargetUtils.resolveFoeSpell(
					this,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s.getLevel(),
					s,
					spellAction);
				break;
			case MagicSys.SpellTargetType.ALLY:
				SpellTargetUtils.resolveAllySpell(
					this,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s);
				break;
			case MagicSys.SpellTargetType.PARTY:
				SpellTargetUtils.resolvePartySpell(
					this,
					caster,
					castingLevel,
					s);
				break;
			case MagicSys.SpellTargetType.FOE_GROUP:
				SpellTargetUtils.resolveFoeGroupSpell(this,
					caster,
					spellAction.getTarget(),
					s.getLevel(),
					castingLevel,
					s,
					spellAction);
				break;
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				//nothing to do
				break;
			case MagicSys.SpellTargetType.CASTER:
				SpellTargetUtils.resolveCasterSpell(this,
					caster,
					castingLevel,
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.ALL_FOES:
				SpellTargetUtils.resolveAllFoesSpell(this,
					caster,
					castingLevel,
					s.getLevel(),
					s,
					spellAction);
				break;
			case MagicSys.SpellTargetType.TILE:
				SpellTargetUtils.resolveTileSpell(this,
					caster,
					castingLevel,
					s.getEffects().getRandom());
				break;
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				SpellTargetUtils.resolveCloudOneGroupSpell(this,
					caster,
					spellAction.getTarget(),
					castingLevel,
					s.getLevel(),
					s,
					spellAction);
				break;
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				SpellTargetUtils.resolveCloudAllGroupsSpell(this,
					caster,
					castingLevel,
					s.getLevel(),
					s,
					spellAction);
				break;
			default:
				throw new MazeException("Unrecognised spell target type: "+ targetType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPlayerCharacter(UnifiedActor actor)
	{
		return actor instanceof PlayerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPlayerAlly(UnifiedActor actor)
	{
		return partyAllies.indexOf(actor.getCombatantData().getGroup()) != -1;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if the two actors are on the same side
	 */
	public boolean isAllies(UnifiedActor actor1, UnifiedActor actor2)
	{
		return getAllAlliesOf(actor1).contains(actor2);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Return all foes of the given actor.
	 */
	public List<UnifiedActor> getAllFoesOf(UnifiedActor actor)
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();

		if (isPlayerCharacter(actor))
		{
			for (FoeGroup fg : foes)
			{
				result.addAll(fg.getActors());
			}
		}
		else
		{
			result.addAll(party.getActors());
			for (FoeGroup fg : partyAllies)
			{
				result.addAll(fg.getActors());
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	all allies of the given actor
	 */
	public List<UnifiedActor> getAllAlliesOf(UnifiedActor actor)
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();

		if (isPlayerCharacter(actor))
		{
			result.addAll(party.getActors());
			for (FoeGroup fg : partyAllies)
			{
				result.addAll(fg.getActors());
			}
		}
		else
		{
			for (FoeGroup fg : foes)
			{
				result.addAll(fg.getActors());
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getRandomAllyOf(UnifiedActor actor)
	{
		List<UnifiedActor> allAlliesOf = getAllAlliesOf(actor);
		return allAlliesOf.get(GameSys.getInstance().nextInt(allAlliesOf.size()));
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getRandomFoeOf(UnifiedActor actor)
	{
		List<UnifiedActor> allFoesOf = getAllFoesOf(actor);
		return allFoesOf.get(GameSys.getInstance().nextInt(allFoesOf.size()));
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	the ActorGroup to which the given actor belongs
	 */
	public ActorGroup getActorGroup(UnifiedActor actor)
	{
		if (isPlayerCharacter(actor))
		{
			return party;
		}
		else if (isPlayerAlly(actor))
		{
			for (ActorGroup ag : getPartyAllies())
			{
				if (ag.getActors().contains(actor))
				{
					return ag;
				}
			}
		}
		else
		{
			for (ActorGroup ag : foes)
			{
				if (ag.getActors().contains(actor))
				{
					return ag;
				}
			}
		}

		throw new MazeException("could not find actor group of ["+actor+"]");
	}

	/*-------------------------------------------------------------------------*/
	public List<UnifiedActor> getAllActors()
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();

		result.addAll(party.getActors());
		for (FoeGroup fg : partyAllies)
		{
			result.addAll(fg.getActors());
		}
		for (FoeGroup fg : foes)
		{
			result.addAll(fg.getActors());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void resolveAttack(UnifiedActor actor, AttackAction attackAction)
	{
		ActorGroup attackedGroup;
		attackedGroup = attackAction.getTargetGroup();

		animationContext = new AnimationContext(actor);

		if (attackedGroup.numAlive() < 1)
		{
			// all in this group are dead do nothing
			return;
		}

		combatStats.captureAttack(attackAction, this);
		
		UnifiedActor defender;
		if (attackAction.isFirstAttack())
		{
			defender = 
				getDefender(actor, attackedGroup, attackAction.getAttackWith(), attackAction);
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
		attack(actor, defender, attackAction);
	}

	/*-------------------------------------------------------------------------*/
	private ActorGroup getAttackedGroup(UnifiedActor actor, int targetGroup, CombatAction action)
	{
		boolean attackAFoe = (isPlayerCharacter(actor) || isPlayerAlly(actor));

		if (action.isAttackingAllies)
		{
			attackAFoe = !attackAFoe;
		}

		if (attackAFoe)
		{
			return this.foes.get(targetGroup);
		}
		else
		{
			//
			// 0 indicates targeting the party, 1..n indicates targeting one
			// of the groups of party allies.
			//

			if (targetGroup == 0)
			{
				return party;
			}
			else
			{
				if (partyAllies.size() < targetGroup)
				{
					return this.partyAllies.get(targetGroup-1);
				}
				else
				{
					// the ally group is gone?
					return party;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	null if no valid defender is found
	 */
	private UnifiedActor getDefender(
		UnifiedActor attacker,
		ActorGroup attackedGroup, 
		AttackWith attackWith,
		CombatAction action)
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
			engagementRange = getEngagementRange(attacker, attackedGroup);
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
	private UnifiedActor getRandomDefender(UnifiedActor attacker, List<UnifiedActor> actors)
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
	public int getEngagementRange(UnifiedActor attacker, ActorGroup attackedGroup)
	{
		if (isPlayerCharacter(attacker))
		{
			return getPCEngagementRange(attacker, attackedGroup);
		}
		else
		{
			return getFoeEngagementRange(attacker);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getFoeEngagementRange(UnifiedActor attacker)
	{
		int foeGroupIndex = foes.indexOf(attacker.getCombatantData().getGroup());

		if (foeGroupIndex == -1)
		{
			foeGroupIndex = partyAllies.indexOf(attacker.getCombatantData().getGroup());
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
	public int getFoeGroupIndex(ActorGroup ag)
	{
		if (!foes.contains(ag))
		{
			throw new MazeException("not a foe group: "+ag);
		}

		return foes.indexOf(ag);
	}

	/*-------------------------------------------------------------------------*/
	public int getPCEngagementRange(UnifiedActor attacker, ActorGroup attackedGroup)
	{
		int groupIndex = foes.indexOf(attackedGroup);
		return getPCEngagementRange(attacker, groupIndex);
	}

	/*-------------------------------------------------------------------------*/
	public int getPCEngagementRange(UnifiedActor attacker, int attackedGroupIndex)
	{
		boolean isFrontRow = party.isFrontRow(attacker);

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
	public int getNrOfEnemyGroups(UnifiedActor attacker)
	{
		if (isPlayerCharacter(attacker) || isPlayerAlly(attacker))
		{
			return foes.size();
		}
		else
		{
			return partyAllies.size() +1;  // +1 for the party
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getNrOfAlliedGroups(UnifiedActor actor)
	{
		if (isPlayerCharacter(actor) || isPlayerAlly(actor))
		{
			return partyAllies.size() +1;  // +1 for the party
		}
		else
		{
			return foes.size();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void attack(UnifiedActor attacker, UnifiedActor defender, AttackAction attackAction)
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
			this.appendEvent(new LightningStrikeEvent(attacker));
			int strikes = GameSys.getInstance().getLightningStrikeNrStrikes(attackEvent);
			attackAction.setNrStrikes(attackAction.getNrStrikes() + strikes);
			attackEvent.incStrikes(strikes);
		}

		if (attackAction.isFirstAttack())
		{
			this.appendEvent(attackEvent);
		}

		this.appendEvents(attackAction.getAttackScript().getEvents());
		if (shouldAppendDelayEvent(attackAction.getAttackScript().getEvents()))
		{
			this.appendEvent(new DelayEvent(Maze.getInstance().getUserConfig().getCombatDelay()));
		}

		int hitPercent = GameSys.getInstance().calcHitPercent(attackEvent, attackAction);
		if (Dice.d100.roll() <= hitPercent)
		{
			DamagePacket damagePacket = GameSys.getInstance().calcDamage(attackEvent);
			combatStats.captureAttackHit(attackAction, this);

			if (GameSys.getInstance().isAttackDodged(attacker, defender, attackAction.getAttackWith()))
			{
				// dodge the attack
				this.appendEvent(new AttackDodgeEvent(defender));
			}
			else if (GameSys.getInstance().isAttackDeflected(attacker, defender, attackAction.getAttackWith()))
			{
				// deflected
				this.appendEvent(new AttackDeflectedEvent(attacker, defender, bodyPart));
			}
			else if (GameSys.getInstance().isAttackParried(attacker, defender, attackAction.getAttackWith()))
			{
				// parried
				this.appendEvent(new AttackParriedEvent(attacker, defender, bodyPart));
			}
			else
			{
				this.appendEvent(new AttackHitEvent(
					attacker,
					defender,
					bodyPart));

				this.appendEvent(new DamageEvent(
					defender,
					attacker,
					damagePacket,
					attackAction.getDamageType(),
					MagicSys.SpellEffectSubType.NORMAL_DAMAGE,
					attackAction.getAttackWith(),
					combatStats));

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
								appendEvents(
									SpellTargetUtils.applySpellToUnwillingVictim(
										ase.getSpellEffects(),
										defender,
										attacker,
										ase.getCastingLevel(),
										ase.getSpellLevel(), this.getAnimationContext()));
							}
						}
					}
				}
			}
		}
		else
		{
			this.appendEvent(new AttackMissEvent(
				attacker, defender));
			combatStats.captureAttackMiss(attackAction, this);
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
			appendEvent(new AnotherActionEvent(aa, this));
		}
	}

	/*-------------------------------------------------------------------------*/
	private BodyPart getRandomBodyPart(UnifiedActor attacker, UnifiedActor defender)
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
	private boolean shouldAppendDelayEvent(List<MazeEvent> script)
	{
		if (script == null || script.isEmpty())
		{
			return false;
		}

		return !(script.get(script.size()-1) instanceof AnimationEvent);
	}

	/*-------------------------------------------------------------------------*/
	private int getStealthCost(UnifiedActor attacker, UnifiedActor defender, AttackType attackType)
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

	/*-------------------------------------------------------------------------*/
	private void initCombatActions(
		ActorActionIntention[] partyIntentions,
		List<ActorActionIntention[]> foeIntentions)
	{
		this.combatIntentions.clear();
		
		// set up party actions
		for (int i = 0; i < partyIntentions.length; i++)
		{
			PlayerCharacter pc = (PlayerCharacter)this.party.getActors().get(i);
			if (pc.getHitPoints().getCurrent() > 0)
			{
				this.combatIntentions.put(pc, partyIntentions[i]);
				partyIntentions[i].setActor(pc);
				pc.getCombatantData().setCurrentIntention(partyIntentions[i]);

				Maze.log("PC "+pc.getName()+" intention is "+partyIntentions[i]);
			}
		}
		// set up party ally intentions
		for (FoeGroup fg : this.partyAllies)
		{
			ActorActionIntention[] intentions = getFoeCombatIntentions(fg);
			for (int i = 0; i < intentions.length; i++)
			{
				Foe ally = (Foe)fg.getActors().get(i);
				this.combatIntentions.put(ally, intentions[i]);
				intentions[i].setActor(ally);
				ally.getCombatantData().setCurrentIntention(intentions[i]);

				Maze.log("PC ally "+ally.getName()+" intention is "+intentions[i]);
			}
		}

		// set up the foe actions
		for (int i = 0; i < foeIntentions.size(); i++)
		{
			ActorActionIntention[] intentions = foeIntentions.get(i);
			for (int j = 0; j < intentions.length; j++)
			{
				ActorGroup group = this.foes.get(i);
				UnifiedActor foe = group.getActors().get(j);
				this.combatIntentions.put(foe, intentions[j]);
				intentions[j].setActor(foe);
				foe.getCombatantData().setCurrentIntention(intentions[j]);

				Maze.log("foe "+foe.getName()+" intention is "+intentions[j]);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention[] getFoeCombatIntentions(FoeGroup foeGroup)
	{
		List<UnifiedActor> foes = foeGroup.getActors();
		ActorActionIntention[] result = new ActorActionIntention[foes.size()];

		for (int i=0; i<foes.size(); i++)
		{
			result[i] = ((Foe)(foes.get(i))).getCombatIntention();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculate the initiative for all combatants this round.  Higher is better.
	 */ 
	private void calcInitiative()
	{
		for (UnifiedActor actor : this.actors)
		{
			CombatantData metaData = actor.getCombatantData();
			int i = GameSys.getInstance().calcInitiative(actor);
			metaData.setIntiative(i);

			combatStats.captureInitiative(i, actor, this);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The actions sorted in descending order of initiative
	 */ 
	private List<CombatAction> getOrderOfPlay()
	{
		// first we need a list of combat actions
		List<CombatAction> result = new ArrayList<CombatAction>();

		for (UnifiedActor actor : this.actors)
		{
			ActorActionIntention actionIntention = this.combatIntentions.get(actor);
			List<CombatAction> actorActions;
			if (actionIntention == ActorActionIntention.INTEND_NOTHING)
			{
				actorActions = new ArrayList<CombatAction>();
				actorActions.add(CombatAction.DO_NOTHING);
			}
			else
			{
				actorActions = getActorCombatActions(actor, actionIntention);
			}

			// insert blink actions as required
			if (actor.getModifier(Stats.Modifiers.BLINK) > 0 && actor.getHitPoints().getCurrent() > 0)
			{
				int blinkInitiative =  blinkDice.roll();
				
				BlinkAction blinkOut = new BlinkAction();
				blinkOut.initiative = blinkInitiative;
				blinkOut.initiativeSet = true;
				
				BlinkAction blinkIn = new BlinkAction();
				blinkIn.initiative = Dice.d6.roll();
				blinkIn.initiativeSet = true;

				actorActions.add(blinkOut);
				actorActions.add(blinkIn);
			}

			for (CombatAction action : actorActions)
			{
				action.actor = actor;
				if (!action.initiativeSet)
				{
					action.initiative = 
						action.getModifier(Stats.Modifiers.INITIATIVE) + 
						actor.getCombatantData().getIntiative();
					action.initiativeSet = true;
				}
			}

			result.addAll(actorActions);
		}

		// shuffle the actions so the actors with equal intiative are randomly 
		// ordered (relies on the sort method being stable, see the javadocs)
		Collections.shuffle(result);
		
		// next we need to sort it based on initiative
		Collections.sort(result, comparator);
		
		// return as an array
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<CombatAction> getActorCombatActions(UnifiedActor actor,
		ActorActionIntention intention)
	{
		intention = GameSys.getInstance().checkConditions(actor, intention, this);

		List<CombatAction> actorActions;
		actorActions = actor.getCombatActions(intention);
		return actorActions;
	}

	/*-------------------------------------------------------------------------*/
	public void appendEvent(MazeEvent event)
	{
		this.events.add(event);
	}

	/*-------------------------------------------------------------------------*/
	public void appendEvents(List<MazeEvent> events)
	{
		if (events == null)
		{
			return;
		}

		for (MazeEvent e : events)
		{
			appendEvent(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addDeadFoe(Foe foe)
	{
		this.deadFoes.add(foe);
	}

	/*-------------------------------------------------------------------------*/
	public void removeFoe(Foe foe)
	{
		FoeGroup foeGroup = foe.getFoeGroup();
		foeGroup.remove(foe);
		actors.remove(foe);
	}

	/*-------------------------------------------------------------------------*/
	public List<Item> getLoot()
	{
		List<Item> result = new ArrayList<Item>();

		for (Foe f : this.deadFoes)
		{
			// todo: this will result in a lot of trash drops
			// Need some way to limit trash drops?
			result.addAll(f.getAllItems());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getTotalExperience()
	{
		int result = 0;

		for (Foe f : this.deadFoes)
		{
			result += f.getExperience();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public CombatStatistics getCombatStatistics()
	{
		return this.combatStats;
	}

	/*-------------------------------------------------------------------------*/
	private class CombatActionComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			if (!(o1 instanceof CombatAction && o2 instanceof CombatAction))
			{
				throw new MazeException("Should be instances of CombatAction: "+
					"["+o1+"] ["+o2+"]");
			}
			
			CombatAction action1 = (CombatAction)o1;
			CombatAction action2 = (CombatAction)o2;
			
			// reverse this because we want descending order
			return action2.initiative - action1.initiative;
		}
	}
}

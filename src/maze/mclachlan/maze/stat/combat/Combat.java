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
import mclachlan.maze.game.event.CheckPartyStatusEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.ActorDiesEvent;
import mclachlan.maze.stat.combat.event.BerserkEvent;
import mclachlan.maze.stat.combat.event.CloudSpellEndOfTurn;
import mclachlan.maze.stat.condition.CloudSpell;
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
	 * Foes, multiple groups thereof. Live ones only.
	 */
	private List<FoeGroup> foes = new ArrayList<>();

	/** Foe groups that have no more live members */
	private List<FoeGroup> deadFoeGroups = new ArrayList<>();

	/**
	 * All actors in this combat
	 */
	private List<UnifiedActor> actors = new ArrayList<>();
	
	private Map<UnifiedActor, ActorActionIntention> combatIntentions =
		new HashMap<UnifiedActor, ActorActionIntention>();
	
	private List<Foe> deadFoes = new ArrayList<>();

	private Comparator comparator = new CombatActionComparator();

	private AmbushStatus ambushStatus;

	private static Dice blinkDice = new Dice(1, 6, 6);

	/**
	 * Statistics collected during the current combat
	 */
	private CombatStatistics combatStats;
	private int roundNr;

	/*-------------------------------------------------------------------------*/
	public enum AmbushStatus
	{
		NONE,
		PARTY_MAY_AMBUSH_FOES,
		FOES_MAY_AMBUSH_PARTY,
		PARTY_MAY_AMBUSH_OR_EVADE_FOES,
		FOES_MAY_AMBUSH_OR_EVADE_PARTY
	}
	
	/*-------------------------------------------------------------------------*/
	public Combat(PlayerParty party, List<FoeGroup> foes, AmbushStatus ambushStatus)
	{
		Maze.getInstance().setCurrentCombat(this);

		this.roundNr = 1;

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
		}

		if (ambushStatus == null)
		{
			this.ambushStatus = AmbushStatus.NONE;
		}
		else
		{
			this.ambushStatus = ambushStatus;
		}

		combatStats.captureAmbushStatus(ambushStatus);
	}

	/*-------------------------------------------------------------------------*/
	public int getRoundNr()
	{
		return roundNr;
	}

	/*-------------------------------------------------------------------------*/
	public int getAverageFoeLevel()
	{
		int result = 0;

		// collect dead foes
		List<Foe> allFoes = new ArrayList<>();
		allFoes.addAll(deadFoes);

		// collect live foes
		for (FoeGroup fg : foes)
		{
			allFoes.addAll(fg.getFoes());
		}

		// average it
		for (Foe f : allFoes)
		{
			result += f.getLevel();
		}

		if (!allFoes.isEmpty())
		{
			result /= allFoes.size();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	the "strongest" foe group in the combat
	 */
	public FoeGroup[] getStrongestAndWeakestFoeGroups()
	{
		FoeGroup strongest = null, weakest = null;
		float max = Integer.MIN_VALUE;
		float min = Integer.MAX_VALUE;

		for (FoeGroup fg : this.foes)
		{
			float fgStr = 0;

			// nr x level
			fgStr += (fg.numActive() * fg.getAverageLevel());

			// weighting to get combat foes at the front

			switch (fg.getFoes().get(0).getFocus())
			{
				case COMBAT:
					fgStr *= 2;
					break;
				case STEALTH:
					fgStr *= 1.5;
					break;
				case MAGIC:
					fgStr *= .5;
					break;
				default:
					throw new MazeException("invalid "+fg.getFoes().get(0).getFocus());
			}

			if (fgStr > max)
			{
				strongest = fg;
				max = fgStr;
			}

			if (fgStr < min)
			{
				weakest = fg;
				min = fgStr;
			}
		}

		if (strongest != null)
		{
			Maze.logDebug("Strongest FG [" + strongest.getDescription() + "] [" + max + "]");
		}
		if (weakest != null)
		{
			Maze.logDebug("Weakest FG [" + weakest.getDescription() + "] [" + min + "]");
		}

		return new FoeGroup[]{strongest, weakest};
	}

	/*-------------------------------------------------------------------------*/
	public void advanceFoeGroup(FoeGroup foeGroup)
	{
		int index = foes.indexOf(foeGroup);
		int newIndex = index -1;

		Collections.swap(foes, index, newIndex);
	}

	/*-------------------------------------------------------------------------*/
	public void retreatFoeGroup(FoeGroup foeGroup)
	{
		int index = foes.indexOf(foeGroup);
		int newIndex = index +1;

		Collections.swap(foes, index, newIndex);
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
	public List<FoeGroup> getFoes()
	{
		return foes;
	}

	/*-------------------------------------------------------------------------*/
	public List<FoeGroup> getDeadFoeGroups()
	{
		return deadFoeGroups;
	}

	/*-------------------------------------------------------------------------*/
	public List<ActorGroup> getFoesOf(UnifiedActor actor)
	{
		List<ActorGroup> result = new ArrayList<ActorGroup>();
		if (actor instanceof PlayerCharacter ||
			(isPlayerAlly(actor)))
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
	public PlayerParty getPlayerParty()
	{
		return party;
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
	public int getLiveFoes()
	{
		int sum = 0;
		for (ActorGroup g : foes)
		{
			sum += g.numAlive();
		}
		return sum;
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
	public Iterator<CombatAction> combatRound(
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
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		Maze.log("checking end of round berserk status...");
		// chance of berserking every round
		for (UnifiedActor actor : this.actors)
		{
			if (actor.getModifier(Stats.Modifier.BERSERKER) > 0)
			{
				if (GameSys.getInstance().actorGoesBeserk(actor))
				{
					result.add(new BerserkEvent(actor));
				}
			}
		}

		Maze.log("updating cloud spells...");
		for (int i = 0; i < foes.size(); i++)
		{
			Maze.log("for foes...");
			result.addAll(processCloudSpells(foes.get(i)));
		}
		for (int i = 0; i < partyAllies.size(); i++)
		{
			Maze.log("for allies...");
			result.addAll(processCloudSpells(partyAllies.get(i)));
		}
		Maze.log("for party...");
		result.addAll(processCloudSpells(party));

		// any surprise ends here
		this.ambushStatus = AmbushStatus.NONE;

		for (UnifiedActor actor : this.actors)
		{
			CombatantData metaData = actor.getCombatantData();
			if (metaData != null)
			{
				metaData.endRound();

				// check for DIE_HARD
				if (metaData.isDieHard())
				{
					result.add(new ActorDiesEvent(actor, null));
				}
			}
		}

		this.roundNr++;
		combatStats.incCombatRounds();

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> processCloudSpells(ActorGroup actorGroup)
	{
		List<MazeEvent> result = new ArrayList<>();

		List<CloudSpell> list = actorGroup.getCloudSpells();
		ListIterator<CloudSpell> li = list.listIterator();

		while (li.hasNext())
		{
			CloudSpell cloudSpell = li.next();

			SpellAction spellAction = new SpellAction(
				actorGroup, cloudSpell.getSpell(), cloudSpell.getCastingLevel());

			if (cloudSpell.isAttackingAllies())
			{
				spellAction.setAttackingAllies(true);
			}

			// todo: animation context goes nowhere?
			AnimationContext animationContext = new AnimationContext(cloudSpell.getSource());

			result.addAll(ActorActionResolver.resolveSpell(
				this,
				cloudSpell.getSource(),
				spellAction,
				true,
				false,
				animationContext));

			result.add(new CheckPartyStatusEvent());
			result.add(new CloudSpellEndOfTurn(cloudSpell, actorGroup));
		}

		return result;
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
		return ActorActionResolver.resolveAction(action, this);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPlayerCharacter(UnifiedActor actor)
	{
		return actor instanceof PlayerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPlayerAlly(UnifiedActor actor)
	{
		return partyAllies.indexOf(getActorGroup(actor)) != -1;
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
	 * 	Return all live foes of the given actor.
	 */
	public List<UnifiedActor> getAllFoesOf(UnifiedActor actor)
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();

		if (isPlayerCharacter(actor))
		{
			for (FoeGroup fg : foes)
			{
				for (UnifiedActor a : fg.getActors())
				{
					if (a.isAlive())
					{
						result.add(a);
					}
				}
			}
		}
		else
		{
			result.addAll(party.getActors());
			for (FoeGroup fg : partyAllies)
			{
				for (UnifiedActor a : fg.getActors())
				{
					if (a.isAlive())
					{
						result.add(a);
					}
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	all live allies of the given actor
	 */
	public List<UnifiedActor> getAllAlliesOf(UnifiedActor actor)
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();

		if (isPlayerCharacter(actor))
		{
			result.addAll(party.getActors());
			for (FoeGroup fg : partyAllies)
			{
				for (UnifiedActor a : fg.getActors())
				{
					if (a.isAlive())
					{
						result.add(a);
					}
				}
			}
		}
		else
		{
			for (FoeGroup fg : foes)
			{
				for (UnifiedActor a : fg.getActors())
				{
					if (a.isAlive())
					{
						result.add(a);
					}
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getRandomAllyOf(UnifiedActor actor)
	{
		List<UnifiedActor> allAlliesOf = getAllAlliesOf(actor);
		return allAlliesOf.get(Dice.nextInt(allAlliesOf.size()));
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getRandomFoeOf(UnifiedActor actor)
	{
		List<UnifiedActor> allFoesOf = getAllFoesOf(actor);

		if (allFoesOf.size() > 0)
		{
			return allFoesOf.get(Dice.nextInt(allFoesOf.size()));
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getRandomFoeWithinRangeOf(UnifiedActor actor)
	{
		// todo

		return getRandomFoeOf(actor);
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

		// check party allies
		for (ActorGroup ag : getPartyAllies())
		{
			if (ag.getActors().contains(actor))
			{
				return ag;
			}
		}

		// check foes
		for (ActorGroup ag : foes)
		{
			if (ag.getActors().contains(actor))
			{
				return ag;
			}
		}

		for (ActorGroup ag : deadFoeGroups)
		{
			if (ag.getActors().contains(actor))
			{
				return ag;
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
			if (actor.isAlive())
			{
				CombatantData metaData = actor.getCombatantData();
				int i = GameSys.getInstance().calcInitiative(actor);
				metaData.setIntiative(i);

				combatStats.captureInitiative(i, actor, this);
			}
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
			if (actor.getModifier(Stats.Modifier.BLINK) > 0 && actor.getHitPoints().getCurrent() > 0)
			{
				int blinkInitiative =  blinkDice.roll("Combat blink dice 1");
				
				BlinkAction blinkOut = new BlinkAction(actor);
				blinkOut.setInitiative(blinkInitiative);

				BlinkAction blinkIn = new BlinkAction(actor);
				blinkIn.setInitiative(blinkInitiative + Dice.d6.roll("Combat blink dice 2"));

				actorActions.add(blinkOut);
				actorActions.add(blinkIn);
			}

			// sort the actor actions, so that we can rank them
			Collections.sort(actorActions, comparator);

			int actorActionIndex = 0;
			for (CombatAction action : actorActions)
			{
				action.setActor(actor);
				action.setActorActionIndex(actorActionIndex++);
				if (!action.isInitiativeSet())
				{
					action.setInitiative(
						action.getModifier(Stats.Modifier.INITIATIVE) +
						actor.getCombatantData().getIntiative());
				}
			}

			result.addAll(actorActions);
		}

		// shuffle the actions so the actors with equal intiative are randomly 
		// ordered (relies on the sort method being stable, see the javadocs)
		Collections.shuffle(result);
		
		// next we need to sort it based on initiative
		Collections.sort(result, comparator);

		// debug logging
		Maze.log(Log.DEBUG, "Order of play: ");
		for (CombatAction action : result)
		{
			Maze.log(Log.DEBUG, action.getActor().getName() + "(" + action.getActor().getStance() + ") : " + action.getInitiative());
		}

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
				throw new MazeException("Should be instances of CombatAction: " +
					"[" + o1 + "] [" + o2 + "]");
			}

			CombatAction action1 = (CombatAction)o1;
			CombatAction action2 = (CombatAction)o2;

			UnifiedActor actor1 = action1.getActor();
			UnifiedActor actor2 = action2.getActor();

			UnifiedActor.Stance stance1 = getStance(actor1, action1);
			UnifiedActor.Stance stance2 = getStance(actor2, action2);

			// default to ACT_EARLY. may be hiding bugs here.
			if (stance1 == null) stance1 = UnifiedActor.Stance.ACT_EARLY;
			if (stance2 == null) stance2 = UnifiedActor.Stance.ACT_EARLY;

			// #2 minus #1 because we want it sorted in descending order
			if (stance1 != stance2)
			{
				return stance2.getPriority() - stance1.getPriority();
			}
			else
			{
				return action2.getInitiative() - action1.getInitiative();
			}
		}

		public UnifiedActor.Stance getStance(UnifiedActor actor, CombatAction action)
		{
			UnifiedActor.Stance stance = actor.getStance();

			// only the actor's first action benefits from SNAKESPEED
			if (stance == UnifiedActor.Stance.SNAKESPEED && action.getActorActionIndex() > 0)
			{
				stance = UnifiedActor.Stance.ACT_EARLY;
			}
			return stance;
		}
	}
}

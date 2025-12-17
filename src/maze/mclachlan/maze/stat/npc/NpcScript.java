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

package mclachlan.maze.stat.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.data.v2.V2Seralisable;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.ActorsTurnToAct;
import mclachlan.maze.game.event.StartCombatEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.ui.diygui.GeneralOptionsCallback;

/**
 * 
 */
public abstract class NpcScript implements GeneralOptionsCallback, V2Seralisable
{
	public static final String ICHIBA_CITY = "Ichiba City";
	public static final String ICHIBA_CROSSROAD= "Ichiba Crossroad";
	public static final String ICHIBA_DOMAIN_NORTH= "Ichiba Domain North";
	public static final String ICHIBA_DOMAIN_SOUTH= "Ichiba Domain South";

	protected Foe npc;

	/*-------------------------------------------------------------------------*/

	/**
	 * Called at the start of each turn.
	 */
	public List<MazeEvent> startOfTurn(long turnNr)
	{
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.
	 */
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed just before the NPC appears, every time he is encountered.
	 * This default implementation does nothing.
	 */
	public List<MazeEvent> preAppearance()
	{
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed the first time the NPC is friendly and greets the party.
	 * This default implementation does nothing.
	 */
	public List<MazeEvent> firstGreeting()
	{
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed subsequent times after the first that the NPC is friendly and
	 * greets the party.  This default implementation does nothing.
	 */
	public List<MazeEvent> friendlyGreeting()
	{
		if (getNpcSpeech().getFriendlyGreeting() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getFriendlyGreeting(), npc));
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is neutral and greets the party.  This default
	 * implementation does nothing.
	 */
	public List<MazeEvent> neutralGreeting()
	{
		if (getNpcSpeech().getNeutralGreeting() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getNeutralGreeting(), npc));
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC decides to attack the party.  This default
	 * implementation just initiates the combat.
	 * @param fAmbushStatus
	 */
	public List<MazeEvent> attacksParty(Combat.AmbushStatus fAmbushStatus)
	{
		Maze maze = Maze.getInstance();
		return getList(
			new StartCombatEvent(
				maze,
				maze.getParty(),
				maze.getCurrentActorEncounter()));
	}

	/*-------------------------------------------------------------------------*/
	protected static List<MazeEvent> getList(MazeEvent... events)
	{
		return new ArrayList<>(Arrays.asList(events));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is attacked by the party.  This default
	 * implementation just begins the combat.
	 */
	public List<MazeEvent> attackedByParty()
	{
		return attacksParty(null);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the party leaves the NPC who is neutral.  This default
	 * implementation simply causes the NPC to leave.
	 */
	public List<MazeEvent> partyLeavesNeutral()
	{
		List<MazeEvent> result = getList(new ActorsLeaveEvent());
		if (getNpcSpeech().getNeutralFarewell() != null)
		{
			result.add(0, new NpcSpeechEvent(getNpcSpeech().getNeutralFarewell(), npc));
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the party leave the NPC who is friendly.  This default
	 * implementation simply causes the NPC to leave.
	 */
	public List<MazeEvent> partyLeavesFriendly()
	{
		List<MazeEvent> result = getList(new ActorsLeaveEvent());
		if (getNpcSpeech().getFriendlyFarewell() != null)
		{
			result.add(0, new NpcSpeechEvent(getNpcSpeech().getFriendlyFarewell(), npc));
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is the target of a successful mindread attempt.
	 * This default implementation simply returns some flavour text.
	 */
	public List<MazeEvent> mindRead(int strength)
	{
		return getList(new FlavourTextEvent(StringUtil.getEventText("msg.default.mindread")));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is the target of a failed mindread attempt.
	 * This default implementation simply returns some flavour text.
	 */
	public List<MazeEvent> mindReadFails(int strength)
	{
		return getList(new FlavourTextEvent(StringUtil.getEventText("event.failed")));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavour text and adds to the
	 * NPC attitude
	 * @param total
	 */
	public List<MazeEvent> successfulThreat(int total)
	{
		total = Math.max(5, total);
		total = Math.min(25, total);

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.thread.succeeds")),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavour text and deducts from
	 * the NPCs attitude.
	 * @param total
	 */
	public List<MazeEvent> failedThreat(int total)
	{
		total = Math.min(-5, total);
		total = Math.max(-25, total);

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("event.failed")),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavour text and adds to
	 * the NPCs attitude
	 * @param total
	 */
	public List<MazeEvent> successfulBribe(int total)
	{
		total = Math.max(5, total);
		total = Math.min(50, total);

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.bribe.succeeds")),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavour text and deducts from
	 * the NPCs attitude
	 * @param total
	 */
	public List<MazeEvent> failedBribe(int total)
	{
		Maze maze = Maze.getInstance();

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("event.failed")),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE),
			new ActorsTurnToAct(
				maze.getCurrentActorEncounter(),
				maze,
				maze.getUi().getMessageDestination()));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players succeed in casting a charm spell on the NPC.
	 * This default implementation does nothing (leaving any attitude adjustments
	 * up to the SpellResult).
	 */
	public List<MazeEvent> successfulCharm()
	{
		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.actor.is.charmed", npc.getDisplayName())),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players fail in casting a charm spell on the NPC.
	 * This default implementation does nothing (leaving any attitude adjustments
	 * up to the SpellResult).
	 */
	public List<MazeEvent> failedCharm()
	{
		return getList(
			new FlavourTextEvent(StringUtil.getEventText("event.failed")),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players succeed in stealing something from the NPC.
	 * This default implementation displays some flavour text, increases the
	 * NPCs theft counter and gives the item to the party
	 */
	public List<MazeEvent> successfulTheft(UnifiedActor source, Item item)
	{
		String itemName = item.getDisplayName();
		boolean inInventory = true;

		if (item instanceof GoldPieces)
		{
			itemName = StringUtil.getUiLabel("common.gp",item.getBaseCost());
			inInventory = false;
		}

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.pc.theft.success",
				source.getName(), itemName)),
			new ChangeNpcTheftCounter(npc, 1),
			new GiveItemToParty(npc, source, item, inInventory));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players attempt to steal fails but is undetected by the
	 * NPC.  This default implementation displays some flavour text and increases
	 * the NPCs theft counter.
	 */
	public List<MazeEvent> failedUndetectedTheft(PlayerCharacter pc, Item item)
	{
		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.pc.theft.fail.undetected",pc.getName())),
			new ChangeNpcTheftCounter(npc, 1));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players attempt to steal fails and is detected by the
	 * NPC.  This default implementation displays some flavour text, adjusts the
	 * NPCs attitude downwards and increases the theft counter
	 */
	public List<MazeEvent> failedDetectedTheft(PlayerCharacter pc, Item item)
	{
		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.pc.theft.fail.caught", pc.getName())),
			new ChangeNpcTheftCounter(npc, 10),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players grab an item and attack the NPC.  This default
	 * implementation displays some flavour text, gives the item to the party and
	 * causes the NPC to attack.
	 */
	public List<MazeEvent> grabAndAttack(PlayerCharacter pc, Item item)
	{
		String itemName = item.getDisplayName();

		boolean inInventory = true;
		if (item instanceof GoldPieces)
		{
			itemName = StringUtil.getUiLabel("common.gp", item.getBaseCost());
			inInventory = false;
		}

		return getList(
			new FlavourTextEvent(StringUtil.getEventText("msg.pc.grab.and.attack",
				pc.getName(), itemName)),
			new GiveItemToParty(npc, pc, item, inInventory),
			new NpcAttacksEvent(npc));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The party is attempting to give an item to the NPC.  In this
	 * case the NPC need not take the item, only react to the attempt to give it.
	 * This default implementation simply returns an NpcTakesItem event, unless
	 * it is a quest item, in which case some speech is returned.
	 */
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		List<MazeEvent> result = new ArrayList<>();

		Maze maze = Maze.getInstance();
		ActorEncounter actorEncounter = maze.getCurrentActorEncounter();
		Foe leader = actorEncounter.getLeader();

		if (item.isQuestItem())
		{
			return doesntWantItem();
		}
		else
		{
			result.add(new NpcTakesItemEvent(owner, item, leader));
		}

		// sure we'll take that thanks
		result.add(
			new FlavourTextEvent(
				StringUtil.getEventText(
					"msg.actor.takes.item",
					leader.getDisplayName(),
					item.getDisplayName())));

		// attitude improvement?
		NpcFaction.AttitudeChange attitudeChange =
			GameSys.getInstance().giveItemToActors(actorEncounter, item);
		switch (attitudeChange)
		{
			case BETTER ->
			{
				result.add(new FlavourTextEvent(
					StringUtil.getEventText("msg.actor.is.pleased", leader.getDisplayName())));
				result.add(new ChangeNpcAttitudeEvent(leader, attitudeChange));
			}
			case NO_CHANGE ->
			{
				result.add(new FlavourTextEvent(
					StringUtil.getEventText("msg.actor.is.not.impressed", leader.getDisplayName())));
				result.add(new ActorsTurnToAct(
					actorEncounter,
					maze,
					maze.getUi().getMessageDestination()));
			}
			case WORSE ->
			{
				result.add(new FlavourTextEvent(
					StringUtil.getEventText("msg.actor.is.angered", leader.getDisplayName())));
				result.add(new ChangeNpcAttitudeEvent(leader, attitudeChange));
				result.add(new ActorsTurnToAct(
					actorEncounter,
					maze,
					maze.getUi().getMessageDestination()));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the given PC wants to talk to this NPC.  This default
	 * implementation simply returns a Wait For Character Speech event.
	 */
	public List<MazeEvent> partyWantsToTalk(PlayerCharacter pc)
	{
		return getList(new WaitForPlayerSpeech(npc, pc));
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Executed when the given PC wants to trade with this NPC.  This default
	 * implementation simply returns an Initiate Trade event.
	 */
	public List<MazeEvent> initiateTrade(PlayerCharacter pc)
	{
		return getList(new InitiateTradeEvent(npc, pc));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the given PC has said something to this NPC. This default
	 * implementation checks the configured Npc dialogue for keywords.
	 */
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		NpcSpeech dialogue = getNpcSpeech();

		if (dialogue != null)
		{
			String response = dialogue.lookupPlayerSentence(speech);

			if (response == null)
			{
				response = doesntKnowAbout(speech);
			}

			List<MazeEvent> result = new ArrayList<>();

			String[] rsps = response.split("\n");

			for (String rsp : rsps)
			{
				result.add(new NpcSpeechEvent(npc, rsp.trim(), MazeEvent.Delay.WAIT_ON_CLICK));
			}

			// check if the player wants to end the conversation
			if (!NpcSpeech.sentenceContainsKeywords(speech, "bye", "goodbye", "farewell"))
			{
				result.add(new WaitForPlayerSpeech(npc, pc));
			}

			return result;
		}
		else
		{
			return getList(new FlavourTextEvent(StringUtil.getEventText("msg.no.response")));
		}
	}

	/*-------------------------------------------------------------------------*/
	private NpcSpeech getNpcSpeech()
	{
		NpcSpeech dialogue = null;
		if (this.npc instanceof Npc)
		{
			Npc npc1 = (Npc)this.npc;
			dialogue = npc1.getTemplate().getDialogue();
		}
		else if (this.npc.getFoeTemplate().getFoeSpeech() != null)
		{
			dialogue = this.npc.getFoeTemplate().getFoeSpeech().getDialog();
		}
		return dialogue;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called when a game is started
	 */
	protected void start()
	{

	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called when a game is loaded.
	 */
	public void initialise()
	{

	}

	/*-------------------------------------------------------------------------*/
	protected PlayerCharacter getPlayerCharacter(int index)
	{
		return Maze.getInstance().getParty().getPlayerCharacter(index);
	}

	/*-------------------------------------------------------------------------*/
	protected NpcFaction getNpcFaction()
	{
		return NpcManager.getInstance().getNpcFaction(npc.getFaction());
	}

	/*-------------------------------------------------------------------------*/
	protected PlayerParty getParty()
	{
		return Maze.getInstance().getParty();
	}

	/*-------------------------------------------------------------------------*/
	protected long getTurnNr()
	{
		return Maze.getInstance().getTurnNr();
	}

	/*-------------------------------------------------------------------------*/
	protected Item createItem(String itemName)
	{
		ItemTemplate it = Database.getInstance().getItemTemplate(itemName);
		return it.create();
	}

	/*-------------------------------------------------------------------------*/
	protected Item createItem(String itemName, int stack)
	{
		ItemTemplate it = Database.getInstance().getItemTemplate(itemName);
		return it.create(stack);
	}

	/*-------------------------------------------------------------------------*/
	protected void checkQuests(List<MazeEvent> result)
	{
		if (npc instanceof Npc)
		{
			Npc npc1 = (Npc)npc;
			QuestManager qm = npc1.getQuestManager();
			List<MazeEvent> events = qm.getNextQuestRelatedStuff();
			if (events != null)
			{
				result.addAll(events);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> optionChosen(String optionChosen)
	{
		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyCantAffordItem(PlayerParty party, Item item)
	{
		if (getNpcSpeech().getPartyCantAffordItem() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getPartyCantAffordItem(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.party.cant.afford.item"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> characterInventoryFull(PlayerParty party, Item item)
	{
		if (getNpcSpeech().getCharacterInventoryFull() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getCharacterInventoryFull(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.party.character.inventory.full"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> notInterestedInBuyingItem(Item item)
	{
		if (getNpcSpeech().getNotInterestedInBuyingItem() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getNotInterestedInBuyingItem(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.not.interested.in.buying.item"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> cantAffordToBuyItem(Item item)
	{
		if (getNpcSpeech().getCantAffordToBuyItem() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getCantAffordToBuyItem(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.cant.afford.to.buy.item"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> npcInventoryFull(Item item)
	{
		if (getNpcSpeech().getNpcInventoryFull() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getNpcInventoryFull(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.npc.inventory.full"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> doesntWantItem()
	{
		if (getNpcSpeech().getDoesntWantItem() != null)
		{
			return getList(new NpcSpeechEvent(getNpcSpeech().getDoesntWantItem(), npc));
		}
		else
		{
			return getList(new NpcSpeechEvent(StringUtil.getEventText("msg.no.thanks"), npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public String doesntKnowAbout(String speech)
	{
		if (getNpcSpeech().getDoesntKnowAbout() != null)
		{
			return String.format(getNpcSpeech().getDoesntKnowAbout(), speech);
		}
		else
		{
			return StringUtil.getEventText("msg.npc.doesnt.know", speech);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Moves the NPC towards the party, if the party is in the same zone
	 * @return null if this NPC is not in the same zone as the party
	 */
	public List<MazeEvent> moveNpcTowardsParty()
	{
		String npcZone = ((Npc)npc).getZone();
		String partyZone = Maze.getInstance().getCurrentZone().getName();

		if (!npcZone.equals(partyZone))
		{
			return null;
		}

		Point partyTile = Maze.getInstance().getPlayerPos();
		Point tile = ((Npc)npc).getTile();

		// halve the distance between the NPC and the party
		int diffX = (partyTile.x - tile.x) / 2;
		int diffY = (partyTile.y - tile.y) / 2;

		int nX;
		int nY;

		if (Math.abs(diffX)<=2 && Math.abs(diffY)<=2)
		{
			// close enough already
			nX = partyTile.x;
			nY = partyTile.y;
		}
		else
		{
			nX = tile.x + diffX/2;
			nY = tile.y + diffY/2;
		}

		return getList(new ChangeNpcLocationEvent(((Npc)npc), new Point(nX, nY), partyZone));
	}
}

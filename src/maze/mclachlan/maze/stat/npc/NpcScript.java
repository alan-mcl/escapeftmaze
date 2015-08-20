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

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.data.Database;

/**
 * 
 */
public abstract class NpcScript
{
	protected Npc npc;

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.  At this point an NPC script is expected
	 * to take any actions required to move the NPC around and so forth.
	 */
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed just before the NPC appears, every time he is encountered.
	 * This default implementation does nothing.
	 */
	public List<MazeEvent> preAppearance()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed the first time the NPC is friendly and greets the party.
	 * This default implementation does nothing.
	 */
	public List<MazeEvent> firstGreeting()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed subsequent times after the first that the NPC is friendly and
	 * greets the party.  This default implementation does nothing.
	 */
	public List<MazeEvent> subsequentGreeting()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is neutral and greets the party.  This default
	 * implementation does nothing.
	 */
	public List<MazeEvent> neutralGreeting()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC decides to attack the party.  This default
	 * implementation just initiates the combat.
	 */
	public List<MazeEvent> attacksParty()
	{
		NpcAttacksEvent event = new NpcAttacksEvent(npc);
		return getList(event);
	}

	/*-------------------------------------------------------------------------*/
	protected List<MazeEvent> getList(MazeEvent... events)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.addAll(Arrays.asList(events));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is attacked by the party.  This default
	 * implementation just begins the combat.
	 */
	public List<MazeEvent> attackedByParty()
	{
		return getList(new NpcAttacksEvent(npc));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the party leaves the NPC who is neutral.  This default
	 * implementation simply causes the NPC to leave.
	 */
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the party leave the NPC who is friendly.  This default
	 * implementation simply causes the NPC to leave.
	 */
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is the target of a successful mindread attempt.
	 * This default implementation simply returns some flavour text.
	 */
	public List<MazeEvent> mindRead(int strength)
	{
		return getList(new FlavourTextEvent(" - mind is blank - "));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the NPC is the target of a failed mindread attempt.
	 * This default implementation simply returns some flavour text.
	 */
	public List<MazeEvent> mindReadFails(int strength)
	{
		return getList(new FlavourTextEvent("FAILED!"));
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
			new FlavourTextEvent("Thread succeeds"),
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
			new FlavourTextEvent("Threat fails"),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavour text and adds to
	 * the NPCs attidude
	 * @param total
	 */
	public List<MazeEvent> successfulBribe(int total)
	{
		total = Math.max(5, total);
		total = Math.min(50, total);

		return getList(
			new FlavourTextEvent("Bribe succeeds"),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation returns some flavout text and deducts from
	 * the NPCs attitude
	 * @param total
	 */
	public List<MazeEvent> failedBribe(int total)
	{
		total = Math.min(-5, total);
		total = Math.max(-50, total);

		return getList(
			new FlavourTextEvent("Bribe fails"),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players succeed in casting a charm spell on the NPC.
	 * This default implementation does nothing (leaving any attitude adjustments
	 * up to the SpellResult).
	 */
	public List<MazeEvent> successfulCharm()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players fail in casting a charm spell on the NPC.
	 * This default implementation does nothing (leaving any attitude adjustments
	 * up to the SpellResult).
	 */
	public List<MazeEvent> failedCharm()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Executed when the players succeed in stealing something from the NPC.
	 * This default implementation displays some flavour text, increases the
	 * NPCs theft counter and gives the item to the party
	 */
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		String itemName = item.getDisplayName();

		if (item instanceof GoldPieces)
		{
			itemName = item.getBaseCost()+"gp";
		}

		return getList(
			new FlavourTextEvent(pc.getName()+" steals: "+itemName+"!"),
			new ChangeNpcTheftCounter(npc, 1),
			new GiveItemToParty(npc, pc, item, true));
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
			new FlavourTextEvent(pc.getName()+" fails but is undetected"),
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
			new FlavourTextEvent(pc.getName()+" is caught!"),
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
		String itemName = item.getName();

		if (item instanceof GoldPieces)
		{
			itemName = item.getBaseCost()+"gp";
		}

		return getList(
			new FlavourTextEvent(pc.getName()+" grabs: "+itemName+"!"),
			new GiveItemToParty(npc, pc, item, true),
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
		if (item.isQuestItem())
		{
			return getList(new NpcSpeechEvent("No thanks."));
		}
		else
		{
			return getList(new NpcTakesItemEvent(owner, item, npc));
		}
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
	 * Executed when the given PC has said something to this NPC. This default
	 * implementation checks the configured Npc dialogue for keywords.
	 */
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		NpcSpeech dialogue = npc.getTemplate().getDialogue();

		String response = dialogue.lookupPlayerSentence(speech);

		if (response == null)
		{
			response = "I don't know anything about '"+speech+"'.";
		}

		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.add(new NpcSpeechEvent(response, 500));

		// check if the player wants to end the conversation
		if (!NpcSpeech.sentenceContainsKeywords(speech, "bye", "goodbye", "farewell"))
		{
			result.add(new WaitForPlayerSpeech(npc, pc));
		}

		return result;
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
		QuestManager qm = npc.getQuestManager();
		List<MazeEvent> events = qm.getNextQuestRelatedStuff();
		if (events != null)
		{
			result.addAll(events);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> optionChosen(String optionChosen)
	{
		return null;
	}
}

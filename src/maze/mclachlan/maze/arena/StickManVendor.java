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

package mclachlan.maze.arena;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.*;

/**
 *
 */
public class StickManVendor extends NpcScript
{
	public static final String PARTY_ASKED_FOR_QUESTS = "arena.stick.man.vendor.party.asked.for.quests";

	List<String> ambiguousResponses = new ArrayList<>();
	Set<String> goodbyes = new HashSet<>();

	private QuestManager questManager;

	{
		ambiguousResponses.add("I see.");
		ambiguousResponses.add("If you say so.");
		ambiguousResponses.add("Whatever you say.");
		ambiguousResponses.add("Well now.");
		ambiguousResponses.add("Hmmm.");
		ambiguousResponses.add("Ok then.");
		ambiguousResponses.add("I suppose so.");
		ambiguousResponses.add("Isn't that strange?");
		ambiguousResponses.add("Why not?");
		ambiguousResponses.add("How does that make you feel?");

		goodbyes.add("bye");
		goodbyes.add("bye bye");
		goodbyes.add("goodbye");
		goodbyes.add("cheerio");
		goodbyes.add("later");
		goodbyes.add("farewell");
		goodbyes.add("see ya");
		goodbyes.add("seeya");
	}

	/*-------------------------------------------------------------------------*/
	protected void start()
	{
		initInternal();
	}

	/*-------------------------------------------------------------------------*/
	public void initialise()
	{
		initInternal();
	}

	/*-------------------------------------------------------------------------*/
	private void initInternal()
	{
		questManager = ((Npc)npc).getQuestManager();
		questManager.addQuest(createQuest1());
		questManager.start();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("Passing through the door, you surprised to " +
			"enter some kind of shop."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attacksParty(Combat.AmbushStatus fAmbushStatus)
	{
		return getList(
			new NpcSpeechEvent("Soft fleshy scum! You will pay for your crimes!", npc),
			new NpcAttacksEvent(npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attackedByParty()
	{
		return getList(
			new NpcSpeechEvent("You will regret attacking me!", npc),
			new NpcAttacksEvent(npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Hello!  I haven't seen you before!  I'm your " +
				"friendly stick man vendor, here to buy and sell shit to you!", npc));

		if (MazeVariables.getBoolean(PARTY_ASKED_FOR_QUESTS))
		{
			checkQuests(result);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Hello again!  What can I help you with!", npc));

		if (MazeVariables.getBoolean(PARTY_ASKED_FOR_QUESTS))
		{
			checkQuests(result);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Don't touch anything.  I don't like the look " +
				"of you lot.", npc));

		if (MazeVariables.getBoolean(PARTY_ASKED_FOR_QUESTS))
		{
			checkQuests(result);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulCharm()
	{
		return getList(
			new NpcSpeechEvent("I guess you aren't so bad after all.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> failedCharm()
	{
		return getList(
			new NpcSpeechEvent("Bah! Don't try that shit on me kids.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Goodbye!  Come again soon!", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Hrmpf!  Good riddance!", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		return getList(
			new NpcSpeechEvent("Gosh thanks!", npc),
			new NpcTakesItemEvent(owner, item, npc),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> failedThreat(int total)
	{
		return getList(
			new NpcSpeechEvent("I ain't scared of you losers!", npc),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulThreat(int total)
	{
		return getList(
			new NpcSpeechEvent("Whoa, hang on, let's talk this over!", npc),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulBribe(int total)
	{
		return getList(
			new NpcSpeechEvent("A pleasure doing business with you!", npc),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.BETTER));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> failedBribe(int total)
	{
		return getList(
			new NpcSpeechEvent("I don't want your stinking small change, chump!", npc),
			new ChangeNpcAttitudeEvent(npc, NpcFaction.AttitudeChange.WORSE));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyWantsToTalk(PlayerCharacter pc)
	{
		return getList(
			new NpcSpeechEvent("Well, speak up", npc),
			new WaitForPlayerSpeech(npc, pc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (goodbyes.contains(speech.toLowerCase()))
		{
			return getList(
				new NpcSpeechEvent("Cheerio then!", npc));
		}
		else if (speech.toLowerCase().contains("quest"))
		{
			MazeVariables.set(PARTY_ASKED_FOR_QUESTS, "true");
			return questManager.getNextQuestRelatedStuff();
		}
		else
		{
			Dice d = new Dice(1, ambiguousResponses.size(), -1);
			return getList(
				new NpcSpeechEvent(npc, ambiguousResponses.get(d.roll("Stick Man Vendor response")), MazeEvent.Delay.WAIT_ON_CLICK),
				new WaitForPlayerSpeech(npc, pc));
		}
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest1()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Quests! blah blah blah blah blah" +
				" blah blah blah blah blah ", npc),
			new NpcSpeechEvent("Your quest is to pull the lever.", npc));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("I am told that you have not completed the task " +
				"that I set you yet.", npc));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Well done, you have pulled the lever.", npc),
			new GrantItemsEvent(createItem("Dagger")),
			new GrantExperienceEvent(100, null));

		return new Quest(
			"Stick Man Vendor And The Test Quest",
			"Just yesterday, a vicious wyrm of some sort came writhing " +
				"through the Gate. It is contained in the chamber below the castle.\n\n" +
				"Anyway, your quest is to pull the lever.",
			"arena.stick.man.vendor.quest.1.completed",
			"arena.stick.man.vendor.quest.1.rewarded",
			intro,
			reward,
			encouragement);
	}
}

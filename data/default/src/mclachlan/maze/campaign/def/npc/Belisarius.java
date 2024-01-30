
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.npc.*;

/**
 * Leonal general, Danaos castle.
 */
public class Belisarius extends NpcScript
{
	public static final String QUEST_1_COMPLETE = "leonal.quest.1.complete";
	public static final String QUEST_1_REWARDED = "leonal.quest.1.rewarded";

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
		QuestManager qm = ((Npc)npc).getQuestManager();

		qm.addQuest(createQuest1());

		qm.start();
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest1()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Your type is usually here asking with " +
				"varying degrees of politeness for access to the lesser Gate " +
				"below the castle.", npc),
			new NpcSpeechEvent("I am seldom happy to grant it. We of the White " +
				"Order work hard to keep the Maze at bay. The more who seek " +
				"to escape it, the more it assails our defences.", npc),
			new NpcSpeechEvent("But you have arrived at an opportune time.\n\n" +
				"Just yesterday, a vicious wyrm of some sort " +
				"came writhing through the Gate. It is contained in the " +
				"chamber below the castle.", npc),
			new NpcSpeechEvent("Instead of risking valuable soldiers on this " +
				"trivial task, I will send you.\n\nIf you triumph, you will find " +
				"the Gate in the same chamber.", npc),
			new SetMazeVariableEvent("danaos.castle.portal.30", "unlocked"));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("I am told that you have not completed the task " +
				"that I set you yet.", npc));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("You have arrived to report your success. Well " +
				"done.", npc),
			new NpcSpeechEvent("You may use the Gate freely, but be on your " +
				"guard. The dark forest of Stygios is the Domain that lies " +
				"beyond - you will find little but danger there.", npc),
			new GrantExperienceEvent(100, null));

		return new Quest(
			QUEST_1_COMPLETE,
			QUEST_1_REWARDED,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The room is clean and spartanly furnished " +
				"with a large wooden desk and several functional looking " +
				"stools. Campaign maps, some of them still glinting with fresh " +
				"ink, cover the walls and desk. Propped in one corner is a " +
				"small camp bed that looks like it's been used in the recent past.\n"),
			new FlavourTextEvent("A solitary leonal straightens from where he " +
				"is perusing the maps on the desk and approaches you..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("So, you are the adventurers. Hrmmmm. Welcome.", npc),
			new NpcSpeechEvent("My name is Belisarius, I am General of the " +
				"White Order.", npc));

		checkQuests(result);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Hrmmmmmh. Welcome back.", npc));

		checkQuests(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Hrmmmmmh. Welcome back.", npc));

		checkQuests(result);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Goodbye.", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell.", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
}
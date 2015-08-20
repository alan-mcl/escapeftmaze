
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Leonal embassy in Ichiba
 */
public class Diomedes extends NpcScript
{
	public static final String DIOMEDES_GRANTED_QUEST = "diomedes.granted.quest";
	public static final String INVITATION_FROM_DIOMEDES = "invitation.from.diomedes";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The knight turns and approaches you, " +
				"gracefully covering the distance in surprisingly few long " +
				"legged strides..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, bold adventurers. I am Diomedes, " +
				"ambassador of the White Order and high priest of the Temple " +
				"of Dana here in Ichiba."),
			new NpcSpeechEvent("I have heard of your exploits. Few attempt " +
				"to escape the First Realm, fewer still survive the attempt."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, friends."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Farewell"),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("The blessing of Dana go with you, friends. " +
				"You will find me here should you need me."),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (NpcSpeech.sentenceContainsKeywords(speech, "quest", "task", "quests") &&
			!MazeVariables.getBoolean(DIOMEDES_GRANTED_QUEST))
		{
			return getList(
				new NpcSpeechEvent("Hmmm, perhaps I do have a task for you."),
				new NpcSpeechEvent("I have unfortunately lost, or had stolen from " +
					"me, some rather rare sticks of camphor incense."),
				new NpcSpeechEvent("If you can find and return them I would be " +
					"most grateful."),
				new SetMazeVariableEvent(DIOMEDES_GRANTED_QUEST, "true"));
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("Camphor Incense"))
		{
			return getList(
				new NpcSpeechEvent("Ah, my incense!"),
				new NpcSpeechEvent("You have my gratitude, friends."),
				new GrantExperienceEvent(100, null),
				new NpcSpeechEvent("I cannot compensate you materially, I'm " +
					"afraid. But perhaps I can open doors in your future."),
				new NpcSpeechEvent("If you ever come to Danaos Castle, the " +
					"stronghold of my Order, tell Stenelaus at the gate that " +
					"Diomedes sent you with his blessing."),
				new SetMazeVariableEvent(INVITATION_FROM_DIOMEDES, "true"));
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
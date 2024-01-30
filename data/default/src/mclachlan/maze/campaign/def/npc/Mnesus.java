
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.SetUserConfigEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 * Gnome king, Aenen city.
 */
public class Mnesus extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The throne room is large and echoing, lined " +
				"with stone pillars. At the far end on an imposing stone " +
				"throne sits a tall and regal gnome, who rises and approaches " +
				"you...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new SetUserConfigEvent("unlock.race.gnome", "true"),
			new NpcSpeechEvent("Greetings. I am Mnesus, King of the gnomes " +
				"of Aenen. Welcome to my hall.", npc),
			new NpcSpeechEvent("I had heard tell that once again some " +
				"brave souls had escaped from the First Realm and were " +
				"wandering these lands. It is a pleasure to meet you in person.", npc),
			new NpcSpeechEvent("This Realm of ours, the Second of many, is " +
				"not free from danger. The Maze is a danger to all. Yet here " +
				"we live as best we can in exile.", npc),
			new NpcSpeechEvent("Ideaus sent word that you came bearing news " +
				"of our brother Antenor?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings friends.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings.", npc));
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
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("Antenor's Ring"))
		{
			return getList(
				new NpcSpeechEvent("That is indeed the seal of Antenor. Tell me, " +
					"how did you find it?", npc),
				new NpcSpeechEvent("...", npc),
				new NpcSpeechEvent("Yes, we had feared that Antenor was lost. " +
					"It is a sad day, but you have our gratitude for returning " +
					"the family seal to us. I will make sure that it is " +
					"converyed to his kin for safekeeping.", npc),
				new NpcTakesItemEvent(owner, item, npc),
				new GrantExperienceEvent(100, null),
				new NpcSpeechEvent("Henceforth you are welcome in Aenen. May " +
					"our halls provide you with safety and rest from the wilds.", npc));
		}
		else if (item.getName().equals("Clockwork Head"))
		{
			ItemTemplate it = Database.getInstance().getItemTemplate("Demilitarised Clockwork Head");
			Item head = it.create();

			return getList(
				new NpcSpeechEvent("Excellent work.", npc),
				new NpcSpeechEvent("I will open the way to the Gate for you. " +
					"Be warned though, beyond you will face sterner foes.", npc),
				new FlavourTextEvent("Mnesus takes the severed clockwork head from you. " +
					"You see him open a small panel on the rear and remove some kind " +
					"of green and silver wafer. He hands the head back to you."),
				new NpcSpeechEvent("You may keep this. Perhaps you can find some" +
					"use for it.", npc),
				new NpcTakesItemEvent(owner, item, npc),
				new GrantExperienceEvent(100, null),
				new GrantItemsEvent(head),
				new SetMazeVariableEvent(ClockworkSentinel.GNOMES_GRANTED_GATE_ACCESS,"true"),
				new SetMazeVariableEvent("aenen.city.portal.36", Portal.State.UNLOCKED));
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (NpcSpeech.sentenceContainsKeywords(speech, "travel to hail", "gate",
			"restricted", "forbidden", "third realm"))
		{
			return getList(
				new NpcSpeechEvent("Yes, as you may already know, here in " +
					"the depths of Aenen, we gnomes guard a minor Gate.", npc),
				new NpcSpeechEvent("It leads to a windswept mountain wasteland, " +
					"a dangerous place to go. None the less, you will have to " +
					"pass that way if you are travelling to Hail, or going beyond " +
					"that to seek the great Gate to the Third Realm.", npc),
				new NpcSpeechEvent("I expected you to approach me about it.", npc),
				new NpcSpeechEvent("You have already earned the freedom of my city, " +
					"but for passage through the Gate I am minded to ask more of " +
					"you - to test your mettle a bit, for mettle you will need " +
					"if you are to venture the wastelands.", npc),
				new NpcSpeechEvent("One of Sarpedon's clockwork servants has " +
					"run amuck. You will find it roaming the woods above our city. " +
					"I require you to destroy it. Bring me proof, and I will open " +
					"the way for you.", npc));
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}
}
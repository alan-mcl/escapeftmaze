
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcLeavesEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import java.util.*;

/**
 * Elf druid in green magic sanctuary, Danaos Village.
 */
public class Lorelei extends NpcScript
{
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		ArrayList<String> ranger = new ArrayList<String>();
		ranger.add("Ranger");

		ArrayList<String> witch = new ArrayList<String>();
		witch.add("Witch");

		ArrayList<String> sorcerer = new ArrayList<String>();
		sorcerer.add("Sorcerer");

		return getList(
			new FlavourTextEvent("You are surrounded by a sense of peace as " +
				"you enter the sanctuary. Pleasant greenery of all sorts fills " +
				"the room, and the air smells sweet and fresh.",
				MazeEvent.Delay.WAIT_ON_CLICK,
				true),
			new FlavourTextEvent("All slender robed woman appears from amongst " +
				"the foliage and gracefully approaches you...",
				MazeEvent.Delay.WAIT_ON_CLICK,
				true),
			new CharacterClassKnowledgeEvent(
				ranger,
				"This is a druidic sanctuary if ever you've seen one, albeit " +
					"a small and secretive one. Your inclination is to treat the " +
					"guardian who approaches with respect and honesty."),
			new CharacterClassKnowledgeEvent(
				witch,
				"An earthy aura of magic infuses the area... you sense rather than " +
					"see a variety of tiny sprites and faeries at play amongst the " +
					"plants... and the figure before you is undoubtedly one of the " +
					"deathless fey, a child of the realm of Faerie..."),
			new CharacterClassKnowledgeEvent(
				sorcerer,
				"The aura of Druidic magic is strong here. No doubt the " +
					"guardian of this sanctuary will be a potent magic user of " +
					"some rustic sort."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, strangers from far off lands. " +
				"I am Lorelei, guardian of this small sanctuary of peace and " +
				"solace, and friend to all those who are friends of the forest."));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings once again. What do you seek?"));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("What do you seek?"));
	}

	/*
	 public List<MazeEvent> attacksParty()
	 {
		 throw new RuntimeException("Unimplemented auto generated method!");
	 }

	 public List<MazeEvent> attackedByParty()
	 {
		 throw new RuntimeException("Unimplemented auto generated method!");
	 }

 */
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Goodbye."),
			new NpcLeavesEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Fare the well on the road, friends. My " +
				"sanctuary is always open to you."),
			new NpcLeavesEvent());
	}

/*
	public List<MazeEvent> mindRead(int strength)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> mindReadFails(int strength)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulThreat(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedThreat(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulBribe(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedBribe(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulCharm()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedCharm()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedUndetectedTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedDetectedTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> grabAndAttack(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> partyWantsToTalk(PlayerCharacter pc)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}*/
}
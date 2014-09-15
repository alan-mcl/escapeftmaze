
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcLeavesEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import java.util.*;

/**
 * Blacksmith vendor, Danaos Village.
 */
public class Stentor extends NpcScript
{
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A wave of dry heat assails you as you " +
				"enter the smithy. Weapons and armour of various sorts are " +
				"stacked around the walls, gleaming in the flickering light " +
				"emanating from a roaring furnace, in front of which a tall " +
				"elderly leonal is bent in labour.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("\nFeeling a breeze from the door, the leonal " +
				"turns and straightens, removes his furnace blackened gloves " +
				"and strides towards you..."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hrrrrmmmh, what have we here? Foreign folks, to " +
				"be sure... Adventurers bold and daring, perhaps... Customers " +
				"even, perchance..."),
			new NpcSpeechEvent("I am Stentor, smith of renown in these parts " +
				"and former officer in the White Order, whose lands you are " +
				"now visiting. How may I serve you?"));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hrrrmmmh, a satisfied customer tends to " +
				"return, methinks. How may Stentor the smith serve you, " +
				"good folk?"));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Ah hrmmmmmgh. You are back."));
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
			new NpcSpeechEvent("Hrmpf. Behave yourselves in this land. The " +
				"White Order tolerates no brigandry or mischief."),
			new NpcLeavesEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell friends. My smithy is always open " +
				"for business."),
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
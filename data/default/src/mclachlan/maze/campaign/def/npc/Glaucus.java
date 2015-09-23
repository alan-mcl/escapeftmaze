
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Item;
import java.util.*;

/**
 * Gnome merchant and embassy in Ichiba
 */
public class Glaucus extends NpcScript
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
			new FlavourTextEvent("You stoop to enter the low-roofed structure. " +
				"Your nose wrinkles at a pungent combination of spices, " +
				"chemicals, grease and burning sticks of incense. Shelves all " +
				"around you are stacked with bottles, packages and " +
				"devices most of which you find impossible to identify."),
			new FlavourTextEvent("A diminutive figure appears from behind a " +
				"counter and approaches you...", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, friends. You must be the newcomers " +
				"from the First Realm. Well done on winning through... Ichiba " +
				"is abuzz with rumours... he he he *ykgh*!", npc),
			new NpcSpeechEvent("I am Glaucus - trader, diplomat, tinker, " +
				"collector and gnome of sundry talents and distinctions. he he " +
				"*ykgh*. How can I help you?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, friends. How may I assist you?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Ugh *ykgh* yes? What do you want?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Ah hmmm. *ykgh*", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Fare thee well friends...", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}

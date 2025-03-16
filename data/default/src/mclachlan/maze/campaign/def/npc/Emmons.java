
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.npc.*;

import static mclachlan.maze.game.MazeEvent.Delay.WAIT_ON_CLICK;
import static mclachlan.maze.map.script.FlavourTextEvent.Alignment.BOTTOM;

/**
 * Lieutenant to Kay in the GSC. Also used as the first NPC that the party
 * encounters in the Gatehouse.
 */
public class Emmons extends NpcScript
{
	public static final String EMMONS_FIRST_ENCOUNTER_COMPLETE = "emmons.first.encounter.complete";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("Rounding the corner you nearly " +
				"collide with a short, scruffy goblin who is furtively picking with a knife" +
				" between the cracks of the northern wall.",
				WAIT_ON_CLICK, true, BOTTOM));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("In a flash the stranger confronts you!",
							WAIT_ON_CLICK, true, BOTTOM),
			new NpcSpeechEvent("Oh aye! What have we here?", npc),
			new NpcSpeechEvent("Could it be ... ye're newcomers to this here Second Realm?", npc),
			new NpcSpeechEvent("Ha! Newly arrived through the Great Gate eh! Been a while, quite a while, since Emmons has seen the likes of ye!", npc),
			new NpcSpeechEvent("Where are me manners?\n\nGreetings travellers, from Emmons, called by some The Hand. On account of me skill at juggling.", npc),
			new NpcSpeechEvent("Now mind you I have little time for prattle. Got business to take care of.\n\nNo doubt ye'll be wanting to move on quickly to Ichiba ...", npc));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hello again!", npc));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Not you again.", npc));
	}

	public List<MazeEvent> partyLeavesNeutral()
	{
		if (!MazeVariables.getBoolean(EMMONS_FIRST_ENCOUNTER_COMPLETE))
		{
			return partyLeavesFriendly();
		}
		else
		{
			return getList(
				new NpcSpeechEvent("Don't let the door hit ye on the way out mate.", npc),
				new ActorsLeaveEvent());
		}
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		if (!MazeVariables.getBoolean(EMMONS_FIRST_ENCOUNTER_COMPLETE))
		{
			return getList(
				new NpcSpeechEvent("So listen mateys.\n\nEmmons know these halls. There are three Gates outta here.", npc),
				new NpcSpeechEvent("All will take ye to places in the crater.\n\nIf I were ye I'd take the center and make for the city. It's where all the action is.", npc),
				new NpcSpeechEvent("Emmons has his own ways out!\n\nFarewell, mayhap we'll meet again in Ichiba City!", npc),
				new FlavourTextEvent("With a casual but surprisingly fast saunter, the goblin steps around the corner and out of sight.", WAIT_ON_CLICK, true, BOTTOM),
				new ChangeNpcLocationEvent((Npc)npc, new Point(1,1), SirKay.ICHIBA_CITY), // todo: place within Ichiba city
				new SetMazeVariableEvent(EMMONS_FIRST_ENCOUNTER_COMPLETE, "true"),
				new ActorsLeaveEvent());
		}
		else
		{
			return getList(
				new NpcSpeechEvent("Safe travels to ye!", npc),
				new ActorsLeaveEvent());
		}
	}
}

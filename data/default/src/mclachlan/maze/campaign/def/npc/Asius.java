
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import java.util.*;

/**
 * Vendor at Aenen outskirts.
 */
public class Asius extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The dingy interior is littered with wooden " +
				"tables and stools, and the walls are lined with cluttered " +
				"shelves. This appears to be some sort of cross between a " +
				"watering hole and a general store...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Well hello there, he he... Welcome to " +
				"Asius' place.", npc),
			new NpcSpeechEvent("You look like you need something... he he he " +
				"he. How can I help?  he he.", npc));
	}

	public List<MazeEvent> friendlyGreeting()
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
		return getList(
			new NpcSpeechEvent("Don't bother coming back.", npc),
			new ActorsLeaveEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Go well, he he, see you soon!", npc),
			new ActorsLeaveEvent());
	}
}

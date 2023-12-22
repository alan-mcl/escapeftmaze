
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Vendor at Aenen outskirts.
 */
public class Emmons extends NpcScript
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
			new FlavourTextEvent("...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Ah.", npc));
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
		return getList(
			new NpcSpeechEvent("Don't bother coming back.", npc),
			new ActorsLeaveEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Go well!", npc),
			new ActorsLeaveEvent());
	}
}

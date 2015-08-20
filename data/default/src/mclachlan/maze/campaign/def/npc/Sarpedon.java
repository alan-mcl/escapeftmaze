
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import java.util.*;

/**
 * Guildmaster at Aenen city
 */
public class Sarpedon extends NpcScript
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
			new FlavourTextEvent("Wooden benches line the walls of this " +
				"building. A notice board on the wall bears a variety of notices " +
				"and rosters. From behind a battered counter, a muscular gnome " +
				"scowls at you and approaches...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Sarpedon, guildmaster of Aenen. Foreigners " +
				"like yerselves sometimes come here; I'm the one to talk to " +
				"for introductions or contracts."));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("You're back. How were yer travels?"));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("What?"));
	}

	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Good riddance. Hmpf."),
			new ActorsLeaveEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Goodbye."),
			new ActorsLeaveEvent());
	}
}
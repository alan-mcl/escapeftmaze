
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Guards access to the gate in Aenen City
 */
public class ClockworkSentinel extends NpcScript
{
	public static final String GNOMES_GRANTED_GATE_ACCESS = "gnomes.granted.gate.access";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A giant brass machine blocks the corridor " +
				"here. As you approach, it whirs and clanks to life..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("bzzzzzz * click * bzzzzzzmmmmmmmmm", npc),
			new NpcSpeechEvent("THIS. WAY. IS. RESTRICTED.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return subsequentGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		if (MazeVariables.get(GNOMES_GRANTED_GATE_ACCESS) != null)
		{
			// party is authorized to enter
			return getList(
				new NpcSpeechEvent("click * click * ALLOWED. TO. PASS.", npc),
				new ActorsLeaveEvent());
		}
		else
		{
			// party is not authorized
			return getList(
				new NpcSpeechEvent("bzzzzzmmmmm * click * NOT. ALLOWED. TO. PASS.", npc),
				new MovePartyEvent(new Point(21, 20), CrusaderEngine.Facing.WEST),
				new ActorsLeaveEvent());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return partyLeavesNeutral();
	}
}
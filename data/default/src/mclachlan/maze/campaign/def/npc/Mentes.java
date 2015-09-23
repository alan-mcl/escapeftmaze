
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import java.util.*;

/**
 * Vendor at Aenen city.
 */
public class Mentes extends NpcScript
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
			new FlavourTextEvent("Neat racks of weapons and armour line the " +
				"walls. Behind a low counter, the beaming gnome vendor of this " +
				"establishment puts down the sword that he was polishing and " +
				"bids you welcome...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, customers. Welcome to Mentes' fine " +
				"establishment. For buying and selling, there's no better " +
				"place in the fine city of Aenen. What are you looking for?", npc));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings once again! What have you for sale?", npc));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Yes?", npc));
	}

	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Hrpmf. Bye.", npc),
			new ActorsLeaveEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell, come again soon!", npc),
			new ActorsLeaveEvent());
	}
}
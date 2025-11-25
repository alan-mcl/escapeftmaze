
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Goblin merchant in the Crater Bazaar in Ichiba
 */
public class Mazza extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The stall is manned by a greasy looking goblin " +
				"with a bulbous nose and beady eyes, who hovers over several large smoking pots and pans.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The air is thick with the " +
				"smell of spice and boiling oil.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Welcome to Mazza's FRIED ANYTHING! ", npc),
			new NpcSpeechEvent("If it fits in a pan, Mazza can FRY IT!", npc),
			new NpcSpeechEvent("If it doesn't fit, Mazza CUTS it till it does!", npc),
			new NpcSpeechEvent("Don't mind the smoke. That's the smell of flavor NOT ESCAPING! Gotta trap it in batter, see?", npc),
			new NpcSpeechEvent("Now what can I get you TODAY?", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("You look HUNGRY! Or tired. Or both. Either way, fried bits solve it.", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("CHEAP? No no, boss, I call it competitively EDIBLE!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Take it easy out there! Mazza will keep the OIL HOT for ya!", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyCantAffordItem(PlayerParty party, Item item)
	{
		return getList(new NpcSpeechEvent("Sorry but NO CREDIT! Mazza takes only cash!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> characterInventoryFull(PlayerParty party, Item item)
	{
		return getList(new NpcSpeechEvent("You got no hands for that boss! Lighten the load, THEN WE FEED!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> notInterestedInBuyingItem(Item item)
	{
		return getList(new NpcSpeechEvent("What? Mazza can't fry THAT!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> cantAffordToBuyItem(Item item)
	{
		return getList(new NpcSpeechEvent("If only! Mazza's purse is EMPTY!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> npcInventoryFull(Item item)
	{
		return getList(new NpcSpeechEvent("Sorry boss, Mazza's got NO ROOM!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> doesntWantItem(Foe leader)
	{
		return getList(new NpcSpeechEvent("What? Mazza can't fry THAT!", npc));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Come back anytime and Mazza gives you the friend price... which is STILL A BARGAIN!", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String doesntKnowAbout(String speech)
	{
		return "No idea what '%s' is, boss. Sounds UNFRYABLE.";
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}

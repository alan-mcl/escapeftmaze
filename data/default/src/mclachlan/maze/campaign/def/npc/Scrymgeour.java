
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Guildmaster and merchant in Ichiba
 */
public class Scrymgeour extends NpcScript
{
	private static final String BEER = "Scrymgeour's Bitter";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
				new FlavourTextEvent("The interior of the building is dark and " +
					"smoky, with wooden tables and stools arranged around a " +
					"stained and scratched bar. Against the walls, various items " +
					"of weapons and armour are stacked, most clearly well used."),
				new FlavourTextEvent("The proprietor approaches you...",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Ah, the bold adventurers who fought through " +
				"to the Second Realm! Pleased to meet you, you've come to the " +
				"right place.", npc),
			new NpcSpeechEvent("I am Scrymgeour. Many of your sort end up at " +
				"my establishment after arriving in Ichiba... I provide a hot " +
				"meal, a warm bed, and have a limited inventory " +
				"of weapons and armour to trade.", npc),
			new NpcSpeechEvent("Some who come here choose to stay for a " +
				"while, and some that are staying here may wish to join your " +
				"party. Either way, I can arrange the matter.", npc));

		checkCocQuest2(result);
		checkFreeBeer(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void checkFreeBeer(List<MazeEvent> result)
	{
		if (MazeVariables.getBoolean(WePickett.FREE_BOOZE_AT_THE_INN))
		{
			List<Item> items = new ArrayList<Item>();
			for (int i=0; i<getParty().size(); i++)
			{
				items.add(createItem(BEER, 1));
			}

			result.add(new NpcSpeechEvent("Hey! This round's on old man Pickett, " +
				"over at the Chamber Of Commerce.", npc));
			result.add(new GrantItemsEvent(items));
			result.add(new SetMazeVariableEvent(WePickett.FREE_BOOZE_AT_THE_INN, "false"));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void checkCocQuest2(List<MazeEvent> result)
	{
		if (MazeVariables.getBoolean(WePickett.QUEST_2_STARTED) &&
			!MazeVariables.getBoolean(WePickett.QUEST_2_COMPLETED))
		{
			result.add(new NpcSpeechEvent("Um, unfortunately we have no " +
				"brandy in stock at the moment. A group of " +
				"goblins drank all my stock the other night!", npc));
			result.add(new NpcSpeechEvent("A particularly rowdy and well " +
				"armed group, I might add. Luckily they headed out to the " +
				"north side of town without causing any trouble here.", npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		List<MazeEvent> result = getList(new NpcSpeechEvent("Welcome back! How can I help?", npc));

		checkCocQuest2(result);
		checkFreeBeer(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
				new NpcSpeechEvent("Yes?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
				new NpcSpeechEvent("Goodbye", npc),
				new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
				new NpcSpeechEvent("Farewell, return any time!", npc),
				new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
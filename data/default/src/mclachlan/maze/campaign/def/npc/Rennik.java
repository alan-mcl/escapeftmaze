
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Cobbler in the Crater Bazaar in Ichiba
 */
public class Rennik extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("This market stall is laden with footwear of all " +
				"shapes and sizes. Sandals, shoes and boots of all descriptions are " +
				"very neatly arranged on shelves and racks.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The proprietor approaches you...",
				MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("Rennik is a middle-aged human with stooped shoulders and forearms like coiled rope.", MazeEvent.Delay.WAIT_ON_CLICK, true, FlavourTextEvent.Alignment.BOTTOM),
			new FlavourTextEvent("He wears thick, mismatched wire-framed lenses and clothes that are plain and well-kept, aside from his boots which are immaculate.", MazeEvent.Delay.WAIT_ON_CLICK, true, FlavourTextEvent.Alignment.BOTTOM),
			new FlavourTextEvent("Without meeting your eyes he studies your feet one by one...", MazeEvent.Delay.WAIT_ON_CLICK, true, FlavourTextEvent.Alignment.BOTTOM),
			new NpcSpeechEvent("Hm. Dust, likely from dungeoneering...\nJungle grit still wet...", npc),
			new FlavourTextEvent("His accent is heavy and difficult to place, rolling consonants and clipped vowels, as if learned late in life and never fully settled.", MazeEvent.Delay.WAIT_ON_CLICK, true, FlavourTextEvent.Alignment.BOTTOM),
			new NpcSpeechEvent("And stains from even further afield... Methinks thee have walked further than most.", npc),
			new NpcSpeechEvent("Well, welcome to Ichiba and to my stall.", npc),
			new NpcSpeechEvent("How can I help thee?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}

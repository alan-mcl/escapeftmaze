
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
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}


package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * merchant in the Crater Bazaar in Ichiba
 */
public class GegnusScrimshaw extends NpcScript
{
	/*-------------------------------------------------------------------------*/
/*	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The stall is manned by a greasy looking goblin " +
				"with a bulbous nose and beady eyes, who hovers over several large smoking pots and pans.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The air is thick with the " +
				"smell of spice and boiling oil.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}*/

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("I'm Gegnus ", npc),
			new NpcSpeechEvent("Hi!", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}


package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Deadly spirit of the Plain Of Pillars
 */
public class BelIes extends NpcScript
{
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You espy a small, damp looking hut in this " +
				"gloomy swamp clearing. Before you can examine the surroundings " +
				"in greater detail, the door of the hut flies open with a clatter!",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("A scrawny man emerges at pace and staggers " +
				"towards you with a stilted gate, arms flailing. Tiny marsh " +
				"insects rise in alarmed swarms in his path. Naked hatred contorts " +
				"his face into a snarl, and mad eyes are locked unblinkingly " +
				"on you...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("YYYEEEEEEAAAAAAAARRRRRRRGGGGHHHHHHHHHHH!!!!!!!\n\n" +
				"UD NIN GIR!!!! SU KE URU NI SE IGI ZID IM SI BAR RAAAAA!!!", npc),
			new NpcSpeechEvent("AAAAAAARRRRRRGGGGGHHHHH!!!!!!!!!\n\n" +
				"LU NINNU TA IM AB E EAAAAAAAA!!!!!!", npc),
			new NpcSpeechEvent("NA GAL!!!! GAL BI LAGAB!!!\n\nGAL BI LAGAAAAAB!!!", npc),
			new NpcSpeechEvent("YYYYAAAAAAAEEEEEEEEEEEEEEEEEEE!!!!!!!!!", npc),
			new CharacterClassKnowledgeEvent("The language bears some " +
				"resemblance to ancient Eridu. You don't know enough to " +
				"translate, but you recall that 'lagab' meant 'death'.\n\n" +
				"Regardless of linguistic preferences, the individual is " +
				"obviously a powerful magic user.", "Sorcerer"),
			new NpcAttacksEvent(npc));
	}

	public List<MazeEvent> friendlyGreeting()
	{
		return firstGreeting();
	}

	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}
}
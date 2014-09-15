
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
public class OldIgor extends NpcScript
{
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A bestial stench clogs your nostrils, acrid " +
				"and hot. Your eyes water and some sort of primordial fear " +
				"grips at your bowels.\n\n", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("You have not long to wait or think, with a " +
				"hiss of scales the beast emerges from the darkness...",
				MazeEvent.Delay.WAIT_ON_CLICK, false));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("HARRRRRRRRGGGGGHHHHHHHHHHHHH!"),
			new NpcSpeechEvent("SOLDIERS COME TO ROB ME."),
			new NpcSpeechEvent("SCUM. PREPARE TO DIE."),
			new CharacterClassKnowledgeEvent("It's a dragon, to be sure. But it " +
				"looks to you like an old one, and small at that. Still, not to " +
				"be trifled with.",
				"Hero"),
			new NpcAttacksEvent(npc));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return firstGreeting();
	}

	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}
}
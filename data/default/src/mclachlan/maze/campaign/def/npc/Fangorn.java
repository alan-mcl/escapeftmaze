
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import java.util.*;

/**
 * Vampire, master of Castle Fangorn
 */
public class Fangorn extends NpcScript
{
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("At first you think that the dark chamber is " +
				"empty. Your breath forms white clouds in the unbelievable " +
				"cold.\n\n"),
			new FlavourTextEvent("You are about to turn away, " +
				"when suddenly you realise that a dark figure is standing " +
				"motionless before you, watching...."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("Fear tingles up your spine as you wonder " +
				"where he came from and how he evaded your detection.\n\n" +
				"Seeing that you have noticed him, the tall figure speaks...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new NpcSpeechEvent("Well. Adventurers, come to my god-forsaken " +
				"lair, my lonely cheerless fortress. Who knows what baneful " +
				"deity directed your unfortunate footsteps this way?", npc),
			new NpcSpeechEvent("Silence, fools. I care not for your " +
				"stories, nor your names. Your writhings will momentarily " +
				"relieve my boredom, then your blood will warm my lips. " +
				"There is nothing more for you.", npc),
			new NpcSpeechEvent("Defend yourselves.", npc),
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
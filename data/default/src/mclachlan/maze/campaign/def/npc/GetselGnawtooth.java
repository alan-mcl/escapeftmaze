
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import java.util.*;

/**
 * Deadly spirit of the Caves Of Ilast
 */
public class GetselGnawtooth extends NpcScript
{
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		return getList(
				new FlavourTextEvent("If anything, the stench in this chamber is " +
					"worse than the last one. Heaps of garbage line the walls, yet " +
					"it's obvious that someone or something lives here..."),
				new FlavourTextEvent("\nYour inspection is suddenly " +
					"cut short as a mad eyed hag drops from the cavernous " +
					"ceiling where she has been hiding and hurls herself at you!"));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
				new NpcSpeechEvent("Yeeeeeeegh! Ave ye wasters been muckin wit " +
					"me wee sis Miriam? Av ye eh?"),
				new NpcSpeechEvent("If any arms come to er ye'll regret it, ye " +
					"fookin will, ere me say it ye wasters."),
				new NpcSpeechEvent("Ah'll chew off yer wee arms, ah'll claw out " +
					"ye lilly livers an after that while ye're still livin an " +
					"breathin ah'll rip out ye beating hearts an roast em before " +
					"ye very eyes! Yeeeeeeeeeeeeeeee!"),
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
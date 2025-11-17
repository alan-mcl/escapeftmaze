package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 *
 */
public class Gurney extends NpcScript
{
	@Override
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("A sturdy dwarf slouches at the table, tankard close at hand, eyes fixed somewhere out the window."),
			new FlavourTextEvent("As you approach he rises to great you..."),
			new NpcSpeechEvent("'Twas upon a dark and stormy night the strangers entered Scrymgeours renowned tavern!", npc),
			new NpcSpeechEvent("Merely seeking refuge they were, yet still heavy with fate were the footsteps that brought them to that low door!", npc),
			new NpcSpeechEvent("Hah! Or at least must one hope that such is how the bards recount events later...", npc),
			new NpcSpeechEvent("Well met adventurers. Ye have the look of eagles about yer, as they say.", npc),
			new NpcSpeechEvent("I am Gurney, at yer service.", npc)
			);
	}
}
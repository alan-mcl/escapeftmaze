
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
public class MiriamMarrowsucker extends NpcScript
{
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		return getList(
				new FlavourTextEvent("The dingy cavern is littered with broken " +
					"furniture and half eaten food. Charred bones poke from a " +
					"blackened fire pit to one side, and the stench of filth and " +
					"decay makes you flinch."),
				new FlavourTextEvent("\nThe largest pile of trash shakes and shudders " +
					"and suddenly from under it emerges a bent old crone, hideously " +
					"ugly, clad in little more than tattered rags. Tottering " +
					"towards you, she grins a toothy smile and flexes yellow " +
					"fingers that bear alarmingly sharp nails..."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
				new NpcSpeechEvent("Eeeee heee heee, wot av we ere ehhhh?", npc),
				new NpcSpeechEvent("Wot bisniss ye have comin ere, to me home, " +
					"me wee nest, all struttin an sneering n all? Well, me " +
					"pretties ye've made a wee mistake, a wee accident ye ave made, " +
					"comin ere, ye ave.", npc),
				new NpcSpeechEvent("Aye, cause when Miriam is done wit ye, when " +
					"ahm done twisting off ye wee heads an sucking out ye soft " +
					"wet eyes an crackin open ye green juicy bones, then ye won't " +
					"be turnin ye noses up at wee Miriam now, will ye?", npc),
				new NpcSpeechEvent("Eeeee hee hee! Come to ye mammie me pretties!", npc),
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
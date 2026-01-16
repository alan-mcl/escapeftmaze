
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
 * Ichiba Librarian
 */
public class Elsibet extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The library smells of old paper and even older wood.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Shelves lean inward, heavy with mismatched volumes. Titles that you can read at first glance promise heroism, romance, and impossible victories.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The space feels small and overlooked, yet stubbornly alive - a pocket of quiet order holding its ground in a noisy town.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("A soft intake of breath comes from between the shelves, followed by hurried footsteps, and a moment later the librarian steps into view, clearly unaccustomed to being interrupted.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Oh. Oh! Visitors. Actual visitors.\n\nUm. Welcome to the Ichiba Library!", npc),
			new NpcSpeechEvent("Hello. I did not expect people today. Or this week. Or ever, really.\nPlease excuse the mess, the quiet, and me.", npc),
			new NpcSpeechEvent("I am Elsibet Quon, Junior Librarian.\n\nWell, the only Librarian, I suppose. For quite a while now.", npc),
			new NpcSpeechEvent("Unfortunately we have a deliquency problem and I can't let you take any books out.\nBut you are welcome to take a seat and read here!", npc),
			new NpcSpeechEvent("Please do not touch the shelves -unless- you intend to read the book!", npc),
			new NpcSpeechEvent("Fiction section is right here, non-fiction and reference works are in the next chamber.", npc),
			new NpcSpeechEvent("If you need a book, I know where it is.\nIf you need silence, I can enforce it.", npc),
			new NpcSpeechEvent("If you need adventure, well... those are shelved under Fiction. Obviously.", npc),
			new NpcSpeechEvent("And yes, that -is- the very popular Red Greave serials over on the east wall. The complete set, so far.", npc),
			new FlavourTextEvent("She pauses, slight breathless. After a moment it's apparent that she has nothing else to say right away.", FlavourTextEvent.Alignment.BOTTOM)
			);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> friendlyGreeting()
	{
		return getList(
			new NpcSpeechEvent("Oh! You are back. Good. I mean - welcome back. The library has been very quiet since you left.", npc),
			new NpcSpeechEvent("Nothing has moved, exploded, or been stolen. Which is an excellent day, by library standards.", npc)
			);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}

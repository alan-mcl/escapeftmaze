
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Vampire, secret leader of the C.O.C.
 */
public class Rhys extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You emerge in a dark room that is decorated " +
				"with blood red drapes and thick red carpets. The temperature is " +
				"icy cold.\n\n"),
			new FlavourTextEvent("A hunched figure stands in the center of the " +
				"room. As it turns to face you, it becomes apparent that you " +
				"are dealing with something horrifying..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("Your hair stands on end as the grim figure " +
				"moves slowly towards you, speaking as it comes...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new NpcSpeechEvent("Well well well. Have you come for a drink? " +
				"Ha ha ha ha haaaaaa ssssssssss sssss.", npc),
			new NpcSpeechEvent("Ahhhhhh, I am discovered. It was only ever " +
				"a matter of time. Perhaps I have become complacent, feeding off " +
				"this stinking city of fools.", npc),
			new NpcSpeechEvent("Before I kill you, I want you to know - I am " +
				"not a creature of the Maze! I write my own story, blaze my own " +
				"path, just like you. I too strive against all the odds " +
				"to tear a better destiny for myself from the bitter hands of " +
				"fate!", npc),
			new NpcSpeechEvent("The difference, friends, is that your story " +
				"ends here!", npc),
			new CharacterClassKnowledgeEvent("You recognise this foe as a " +
				"vampire, one of the deadliest of the undead.",
				"Priest"),
			new NpcAttacksEvent(npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attacksParty(Combat.AmbushStatus fAmbushStatus)
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}
}
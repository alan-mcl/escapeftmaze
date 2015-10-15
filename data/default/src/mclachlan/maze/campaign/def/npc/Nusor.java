
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.*;

/**
 * Minion of the demon Usark, waylays the party in the Stygios Forest and
 * warns them off proceeding.
 */
public class Nusor extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("Suddenly, a blinding flash of light stabs " +
				"through the gloomy forest, searing your unready eyes.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("When you recover your vision, the vegetation all " +
				"around you is scorched and charred, and a terrifying " +
				"creature stands before you wreathed in crackling flames... ",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new CharacterClassKnowledgeEvent("You recognise this being as a demon " +
				"from some fiery nether plane.",
				"Priest", "Sorcerer", "Witch"),
			new FlavourTextEvent("You feel the heat of the fire on your faces. " +
				"The creature's empty eyes regard you silently for a moment, then " +
				"a chilling voice issues from it's featureless face...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new NpcSpeechEvent("MORTALS, I BRING A MESSAGE FROM MY MASTER USARK " +
				"THE PITILESS, MOST GLORIOUS SERVANT OF THE MAZE.", npc),
			new NpcSpeechEvent("USARK SENDS A WARNING: GO NO FURTHER. DO NOT " +
				"TRY TO ESCAPE FROM THE MAZE. ONLY YOUR DEATHS WILL RESULT. THE " +
				"SENTENCE IS NOT YET SERVED.", npc),
			new NpcSpeechEvent("ALL SEEING IS THE EYE OF USARK. YOUR EVERY MOVE " +
				"IS WATCHED.", npc),
			new NpcSpeechEvent("HEAVY IS THE HAND OF USARK WHEN HE CHOOSES TO ACT. " +
				"YOU WOULD NOT SURVIVE HIS WRATH.", npc),
			new NpcSpeechEvent("BOUNDLESS ARE THE LEGIONS OF USARK. FIVE ARE MINE " +
				"TO COMMAND, AMONGST THE LEAST OF HIS THIRTY GENERALS.", npc),
			new NpcSpeechEvent("GO BACK. SPEND YOUR SHORT LIVES IN THE SAFETY " +
				"OF THE DOMAINS WHERE THE HAND AND EYE OF USARK WILL NOT PURSUE " +
				"YOU, ELSE YOU WILL SURELY BE LOST.", npc),
			new NpcSpeechEvent("I AM NUSOR THE TORTURER, AND YOU HAVE HEARD MY " +
				"WARNING.", npc),
			new FlavourTextEvent("The demons eyes flash with fire, and it " +
				"vanishes in an acrid cloud of smoke...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new ActorsLeaveEvent(),
			new ChangeNpcLocationEvent((Npc)npc, new Point(1,1), "Stygios Forest"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attacksParty(Combat.AmbushStatus fAmbushStatus)
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}
}
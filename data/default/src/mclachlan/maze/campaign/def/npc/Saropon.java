
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.*;

/**
 * Minion of the demon Usark, waylays the party in the Tornado Mountain and
 * warns them off proceeding.
 */
public class Saropon extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("With starling suddenness, the howling wind " +
				"stops clutching at your clothing, and silence fills your ears.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Gazing around, you see a bird frozen in " +
				"flight, wheeling above your head. The meagre vegetation around you " +
				"is still, bent and twisted in the midst of it's struggle against " +
				"the pervasive winds.\n\nTime around you is standing still.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Without warning, the still air before you bends " +
				"and warps, shimmering in some kind of force field. A bizarre " +
				"apparition appears before you...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new CharacterClassKnowledgeEvent("You recognise this being as a " +
				"powerful demon of some sort.",
				"Priest", "Sorcerer", "Witch"),
			new FlavourTextEvent("Your skin crawls as the creature regards you " +
				"from behind it's bizarre mask. Suddenly it speaks, with a voice " +
				"that is oiled with charm and menace...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new NpcSpeechEvent("Greetings, worthy mortal adventurers. I am " +
				"Saropon the Vile, humble servant of the demon Usark.", npc),
			new NpcSpeechEvent("As you no doubt already know, my glorious " +
				"master Usark is one of the seven great Servants of the Maze, " +
				"that which is both your prison and your jailer.", npc),
			new NpcSpeechEvent("Many and wide are the lands of the First Realm, " +
				"and few are the dangers. You could have lived happily there, many " +
				"have done so before you. And yet you chose to leave.", npc),
			new NpcSpeechEvent("Less wide, perhaps, are the lands and towns of this Second " +
				"Realm. But yet you might have chosen to live a full life among " +
				"the peoples here, the proud leonals or the cunning gnomes or the " +
				"others in Ichiba Craterlake.", npc),
			new NpcSpeechEvent("And yet, here you are. I can see a fire in your " +
				"eyes, a fire that in some cannot be easily extinguished. This " +
				"fire has driven you onward, and brought news of you to Usark " +
				"himself, whose eye sees all.", npc),
			new NpcSpeechEvent("I am here to deliver a warning from my master. " +
				"Thus far, you have not been tested. The battles you have fought " +
				"and the pain you have suffered are as nothing. Turn back to " +
				"safety now, or Usark will move against you. You are condemned by " +
				"the actions of your ancestors, and the sentence is not yet served.", npc),
			new NpcSpeechEvent("When he moves, it will be swift and deadly, and " +
				"you will not survive. The resources of Usark are limitless. My " +
				"own six legions of demonic warriors are the least of his " +
				"tools, and I single handed could crush you here and now.", npc),
			new NpcSpeechEvent("I see the fire in your eyes and I doubt that you " +
				"will heed my warning. Such is mortal life, always struggling.", npc),
			new NpcSpeechEvent("I am Saropon mortals, remember my warning. " +
				"Remember too that there are fates worse than merely dying. " +
				"Turn back!", npc),
			new FlavourTextEvent("With a ripple of light-bending force fields, the " +
				"being vanishes. Time resumes it's normal course and " +
				"you are nearly knocked down by a sudden gust of wind. Above your " +
				"head the bird utters a harsh cry and wheels away, heedless.",
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
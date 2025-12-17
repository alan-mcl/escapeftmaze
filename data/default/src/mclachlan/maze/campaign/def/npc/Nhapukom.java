
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Minion of the demon Usark, fights the party in the Temple Of The Gate
 */
public class Nhapukom extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You enter a large, domed chamber. Torches " +
				"flicker in alcoves, and the vaulted ceiling is lost in the gloom " +
				"above. On the far side, a great magical portal flickers and " +
				"shimmers, emitting a tangible hum that makes your hair stand on " +
				"end. Surely, after all this time, you have found the second Great " +
				"Gate!",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Standing between you and the Great Gate waits a " +
				"tall horned demon, it's red skin glowing and smoking gently. You " +
				"have the impression that it has been waiting for you, frozen " +
				"still with a deadly immortal patience...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("The demon speaks!",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new NpcSpeechEvent("HEED THE WORDS OF USARK THE PROUD.", npc),
			new NpcSpeechEvent("YOU HAVE RECEIVED THE REQUIRED WARNINGS. " +
				"YOU HEARD BUT DISOBEYED. NOW YOU MUST BE PUNISHED. NONE MAY " +
				"ESCAPE THE MAZE. THE SINS OF YOUR ANCESTORS ARE STILL UPON YOU. " +
				"THE SENTENCE HAS NOT YET BEEN SERVED.", npc),
			new NpcSpeechEvent("FURTHER WORDS ARE FUTILE.", npc),
			new NpcSpeechEvent("I AM NHAPUKOM BANESOUL AND I AM YOUR DEATH.", npc),
			new FlavourTextEvent("The demons eyes flash with fire, and it " +
				"moves to attack you!",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
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
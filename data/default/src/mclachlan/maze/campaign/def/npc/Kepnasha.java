
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.ChangeNpcLocationEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Minion of the demon Usark, takes out Imogen and lectures the party.
 */
public class Kepnasha extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You climb to the top of the tower and enter " +
				"Imogens leafy inner chamber, hopeful for more loot."),
			new FlavourTextEvent("Suddenly, the stench of sulphur assails you, " +
				"and you see standing within a tall red-skinned humanoid.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The creature fixes its burning eyes on you " +
				"and you involuntatily freeze in fear.\n\n" +
				"It speaks in a voice like a clash of cymbals and a thunder of " +
				"wild horses...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new CharacterClassKnowledgeEvent("This is a demon. A powerful one. ",
				"Priest", "Sorcerer", "Witch"),
			new NpcSpeechEvent("MORTALS\n\nI AM KEPNASHA, SERVANT " +
				"OF THE MIGHTY USARK."),
			new NpcSpeechEvent("IMOGEN SOUGHT IMMORTALITY THOUGH SORCERY. SHE " +
				"SOUGHT THUS TO ESCAPE FROM THE MAZE."),
			new NpcSpeechEvent("NONE ESCAPE THE MAZE! HER TORMENTS WILL LAST " +
				"FOREVER. THUS USARK HAS GRANTED WHAT SHE SOUGHT.\n\n" +
				"YOUR PART IN THIS IS KNOWN, YOU HAVE BEEN WATCHED. THE MAZE " +
				"KNOWS ALL."),
			new NpcSpeechEvent("DO NOT SEEK TO ESCAPE. THE DOOM OF YOUR ANCESTORS " +
				"BINDS YOU TO THE MAZE, THE SENTENCE IS NOT YET SERVED."),
			new NpcSpeechEvent("YOU STAND BEFORE ME AND YOUR FEAR BETRAYS YOU. " +
				"YOU KNOW THAT YOUR LIFE IS IN MY HANDS."),
			new NpcSpeechEvent("THIRTY OTHERS SUCH AS I SERVE USARK THE " +
				"DIRE. OF USARKS FELL HOSTS, SEVEN DO I COMMAND. EACH ONE ALONE " +
				"WOULD LAY WASTE TO THIS SECOND REALM IN WHICH WE STAND."),
			new NpcSpeechEvent("USARK SERVES THE MAZE, AND WITH HIM SIX OTHERS " +
				"NO LESS IN MIGHT AND SAVAGERY.\n\n" +
				"YOU CANNOT HOPE TO STAND AGAINST THE FORCES ARRAYED AGAINST YOU."),
			new NpcSpeechEvent("DO NOT SEEK TO ESCAPE THE MAZE. DO NOT SEEK TO " +
				"ENTER FURTHER REALMS THAN THIS. ONLY DEATH AWAITS YOU.\n\n" +
				"MORTALS\n\n"+
				"TODAY YOU HAVE HEARD THE WORDS OF KEPNASHA DEATHEATER."),
			new FlavourTextEvent("The demons eyes flash with fire, and it " +
				"vanishes in an acrid cloud of smoke...",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new ActorsLeaveEvent(),
			new ChangeNpcLocationEvent(npc, new Point(0,0), "Ichiba City"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attacksParty()
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

package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.npc.*;

/**
 * Keeper of Dalen
 */
public class RenSan extends NpcScript
{
	private static final String REN_SAN_LOCATION_VAR = "dalen.npc.ren.san.location";

	public List<MazeEvent> preAppearance()
	{
		String var = MazeVariables.get(REN_SAN_LOCATION_VAR);
		if (var == null)
		{
			return getList(
				new FlavourTextEvent("Suddenly, a slight figure drops from " +
					"the moss covered boughs above, and lands with feline " +
					"grace before you..."));
		}
		else
		{
			return getList(
				new FlavourTextEvent("Waiting for you in the clearing, " +
					"sword drawn, is a familiar figure..."));
		}
	}

	public List<MazeEvent> firstGreeting()
	{
		return subsequentGreeting();
	}

	public List<MazeEvent> subsequentGreeting()
	{
		String var = MazeVariables.get(REN_SAN_LOCATION_VAR);
		if (var == null)
		{
			return getList(
				new FlavourTextEvent("The cat-warrior crouches low for a " +
					"moment, fingers lightly touching the hilt of his sword. " +
					"Then he straightens and addresses you..."),
				new NpcSpeechEvent("Strangers. Go no further. This domain is " +
					"not for mortals like yourselves. The forest keeps it's " +
					"own secrets, you are not welcome. Leave, or you will come " +
					"to grief."),
				new NpcSpeechEvent("I am Ren San. You have been warned."),
				new ChangeNpcLocationEvent(npc, new Point(14,5), Maze.getInstance().getCurrentZone().getName()),
				new SetMazeVariableEvent(REN_SAN_LOCATION_VAR, "2"),
				new ActorsLeaveEvent());
		}
		else
		{
			return getList(
				new NpcSpeechEvent("Unwelcome strangers. Twice you have " +
					"been warned. Twice you have pressed on, heedless."),
				new NpcSpeechEvent("There are no more words to be spoken."),
				new NpcSpeechEvent("Come, brother, let us rid the forest of " +
					"these invaders."),
				new NpcAttacksEvent(npc));
		}
	}

	public List<MazeEvent> neutralGreeting()
	{
		return subsequentGreeting();
	}
}

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
public class Tao extends NpcScript
{
	private static final String TAO_LOCATION_VAR = "dalen.npc.tao.location";

	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		String var = MazeVariables.get(TAO_LOCATION_VAR);
		if (var == null)
		{
			return getList(
				new FlavourTextEvent("Without warning, a slender figure " +
					"saunters from behind a gnarled trunk, and " +
					"blocks your path..."));
		}
		else
		{
			return getList(
				new FlavourTextEvent("A familiar figure is waiting for you..."));
		}
	}

	public List<MazeEvent> firstGreeting()
	{
		return subsequentGreeting();
	}

	public List<MazeEvent> subsequentGreeting()
	{
		String var = MazeVariables.get(TAO_LOCATION_VAR);
		if (var == null)
		{
			return getList(
				new FlavourTextEvent("It is another cat-warrior, similar to " +
					"the first. His whiskers twitch as he examines you, then " +
					"he speaks..."),
				new NpcSpeechEvent("I can tell that you're brave. Skilled, too.", npc),
				new NpcSpeechEvent("Turn back. This place is forbidden to you, " +
					"I can see in your eyes that you know this to be truth. " +
					"Bravery and skill will not help you if you go further.", npc),
				new NpcSpeechEvent("My brother has warned you once. I, Tao, " +
					"deliver the second warning. Leave this domain or you " +
					"will be slain.", npc),
				new ChangeNpcLocationEvent((Npc)npc, new Point(0,0), Maze.getInstance().getCurrentZone().getName()),
				new SetMazeVariableEvent(TAO_LOCATION_VAR, "2"),
				new ActorsLeaveEvent());
		}
		else
		{
			return getList(
				new FlavourTextEvent("Without a word, Tao draws his sword and " +
					"attacks you."),
				new NpcAttacksEvent(npc));
		}
	}

	public List<MazeEvent> neutralGreeting()
	{
		return subsequentGreeting();
	}}
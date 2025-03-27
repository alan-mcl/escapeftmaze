package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class TempleOfTheGateRagDoll extends TileScript
{
	public static final String RAG_DOLL_USED = "temple.of.the.gate.rag.doll.used";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, UnifiedActor user)
	{
		if (item.getName().equals("Miriam's Rag Doll") &&
			!MazeVariables.getBoolean(RAG_DOLL_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("You toss the limp raggety doll that you " +
					"found amongst the possessions of the hag Miriam " +
					"Marrowsucker into the gaping shaft.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent("\n\nIt tumbles rather pathetically into " +
					"the darkness and is lost from view.",
					MazeEvent.Delay.WAIT_ON_CLICK, false));
			result.add(new SetMazeVariableEvent(RAG_DOLL_USED, "true"));

			user.removeItem(item, true);

			return result;
		}

		return null;
	}

}
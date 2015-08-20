package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.campaign.def.npc.WePickett;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class GoblinMagsmenEncounter extends TileScript
{
	private static final String GOBLIN_MAGSMEN_ENCOUNTER = "goblin.magsmen.encounter";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(WePickett.QUEST_2_STARTED) &&
			!MazeVariables.getBoolean(WePickett.QUEST_2_COMPLETED))
		{
			return getList(
				new FlavourTextEvent("Pushing through the door, you surprise a " +
					"large group of goblins who are lounging around the dingy " +
					"residence. Empty bottles are scattered around and the smell " +
					"of stale alchohol is overpowering."),
				new FlavourTextEvent("Strangely, the floor is covered with a " +
					"large amount of blue and white china shards, as if a someones " +
					"grandmothers best tea set has been smashed up here.",
					MazeEvent.Delay.WAIT_ON_CLICK,
					true),
				new FlavourTextEvent("You have little time to take any more in, " +
					"as the goblins spring to their feet, draw wicked daggers and " +
					"attack!",
					MazeEvent.Delay.WAIT_ON_CLICK,
					true),
				new EncounterActorsEvent(GOBLIN_MAGSMEN_ENCOUNTER, GOBLIN_MAGSMEN_ENCOUNTER));
		}
		else
		{
			return null;
		}
	}
}
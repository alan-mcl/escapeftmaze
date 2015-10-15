package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class EkirthsTombMedallionOfBelEncounter extends TileScript
{
	public static final String ENCOUNTER_DONE =
		"ekirths.tomb.medallion.of.bel.encounter.done";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		if (!MazeVariables.getBoolean(ENCOUNTER_DONE) && 
			MazeVariables.getBoolean(EkirthsTombMedallionOfBel.MEDALLION_OF_BEL_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("Pouring down the passage comes a " +
					"swarm of giant black beetles, pincers clacking and gnashing!",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new EncounterActorsEvent(ENCOUNTER_DONE, "scarabs.of.bel.swarm", null, null));

			return result;
		}

		return null;
	}
}
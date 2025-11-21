package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.campaign.def.npc.Imogen;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;

/**
 *
 */
public class IchibaGnollEncounters extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(Imogen.QUEST_3_COMPLETE))
		{
			return getEncounter();
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getEncounter()
	{
		return getList(
			new EncounterActorsEvent(null, "gnoll.village.allies.on.call", null, null, null, null, null, null, false));
	}
}
package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import java.util.*;

/**
 *
 */
public class RuinsOfHailDeadPillarTouch extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		String var = MazeVariables.get(RuinsOfHailDeadPillarTile.BLACK_BLOOD_MAZE_VAR);
		if (var == null)
		{
			return getList(
				new FlavourTextEvent("The pillar is cold to your touch..."));
		}
		else
		{
			return Database.getInstance().getScript(
				"Ruins Of Hail to Castle Fangorn").getEvents();
		}
	}
}
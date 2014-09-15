package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import java.util.*;

/**
 *
 */
public class RuinsOfHailDeadPillarTile extends TileScript
{
	public static final String BLACK_BLOOD_MAZE_VAR = "ruins.of.hail.black.blood.used";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Black Blood"))
		{
			return Database.getInstance().getScript(
				"Ruins Of Hail use black blood").getEvents();
		}

		return null;
	}
}
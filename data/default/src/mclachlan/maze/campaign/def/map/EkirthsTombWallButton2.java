package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;

/**
 *
 */
public class EkirthsTombWallButton2 extends TileScript
{
	public static final String MAZE_VAR = "ekirths.tomb.wall.button.2";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(
		Maze maze,
		Point tile,
		Point previousTile,
		int facing)
	{
		if (!MazeVariables.getBoolean(MAZE_VAR))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new SoundEffectEvent("14096_adcbicycle_9_2"));
			result.add(new SetMazeVariableEvent(MAZE_VAR, "true"));

			return result;
		}

		return null;
	}
}
package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class StopMusicEvent extends MazeEvent
{
	@Override
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getUi().getMusic().stop();
		return null;
	}
}

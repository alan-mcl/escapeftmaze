package mclachlan.maze.campaign.def.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class IchibaLibraryFictionSection extends TileScript
{
	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		return getList(
			new FlavourTextEvent("You examine the bookshelves..."),
			new FlavourTextEvent("todo")
		);
	}
}

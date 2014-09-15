package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.util.MazeException;
import java.util.*;

/**
 *
 */
public class RandomRotator extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		int newFacing;

		int roll = Dice.d4.roll();
		switch (roll)
		{
			case 1: newFacing = CrusaderEngine.Facing.NORTH; break;
			case 2: newFacing = CrusaderEngine.Facing.SOUTH; break;
			case 3: newFacing = CrusaderEngine.Facing.EAST; break;
			case 4: newFacing = CrusaderEngine.Facing.WEST; break;
			default: throw new MazeException("invalid facing "+roll);
		}

		return getList(
			new MovePartyEvent(tile, newFacing));
	}
}
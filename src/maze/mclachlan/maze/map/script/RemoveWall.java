/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;

/**
 *
 */
public class RemoveWall extends TileScript
{
	private String mazeVariable;
	private int wallIndex;
	private boolean horizontalWall;
	
	public static final String FOUND = "found";
	
	/*-------------------------------------------------------------------------*/
	public RemoveWall(String mazeVariable, int wallIndex, boolean horizontalWall)
	{
		this.mazeVariable = mazeVariable;
		this.wallIndex = wallIndex;
		this.horizontalWall = horizontalWall;
	}
	
	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (MazeVariables.get(mazeVariable) != null)
		{
			new RemoveWallEvent(mazeVariable, horizontalWall, wallIndex).resolve();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		MazeScript script = Database.getInstance().getMazeScript("_SECRET_PASSAGE_");
		
		List<MazeEvent> events = script.getEvents();
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.addAll(events);
		result.add(new RemoveWallEvent(mazeVariable, horizontalWall, wallIndex));
		
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public boolean isHorizontalWall()
	{
		return horizontalWall;
	}

	public int getWallIndex()
	{
		return wallIndex;
	}
}

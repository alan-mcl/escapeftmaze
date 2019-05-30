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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.Maze;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import java.util.*;

/**
 *
 */
public class RemoveWallEvent extends MazeEvent
{
	private String mazeVariable;
	private boolean horizontalWall;
	private int wallIndex;

	/*-------------------------------------------------------------------------*/
	public RemoveWallEvent(String mazeVariable, boolean horizontalWall, int wallIndex)
	{
		this.mazeVariable = mazeVariable;
		this.horizontalWall = horizontalWall;
		this.wallIndex = wallIndex;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		removeWall(Maze.getInstance().getCurrentZone().getMap(), horizontalWall, wallIndex);
		
		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	void removeWall(Map map, boolean horizontal, int index)
	{
		map.setWall(horizontal, new Wall(Map.NO_WALL, null, false, false,null, null), index);
		MazeVariables.set(this.mazeVariable, "1");
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isHorizontalWall()
	{
		return horizontalWall;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public int getWallIndex()
	{
		return wallIndex;
	}
}

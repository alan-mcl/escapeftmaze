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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;

/**
 *
 */
public class SetMazeVariable extends TileScript
{
	private String mazeVariable, value;

	public SetMazeVariable()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SetMazeVariable(String mazeVariable, String value)
	{
		this.mazeVariable = mazeVariable;
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.add(new SetMazeVariableEvent(mazeVariable, value));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getMazeVariable()
	{
		return mazeVariable;
	}

	/*-------------------------------------------------------------------------*/
	public String getValue()
	{
		return value;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		SetMazeVariable that = (SetMazeVariable)o;

		if (getMazeVariable() != null ? !getMazeVariable().equals(that.getMazeVariable()) : that.getMazeVariable() != null)
		{
			return false;
		}
		return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getMazeVariable() != null ? getMazeVariable().hashCode() : 0);
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		return result;
	}
}

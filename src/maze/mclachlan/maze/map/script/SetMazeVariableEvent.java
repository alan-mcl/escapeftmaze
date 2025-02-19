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
import java.util.*;

/**
 *
 */
public class SetMazeVariableEvent extends MazeEvent
{
	private String mazeVariable, value;

	public SetMazeVariableEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SetMazeVariableEvent(String mazeVariable, String value)
	{
		this.mazeVariable = mazeVariable;
		this.value = value;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		MazeVariables.set(mazeVariable, value);
		return null;
	}

	/*-------------------------------------------------------------------------*/

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public String getValue()
	{
		return value;
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

		SetMazeVariableEvent that = (SetMazeVariableEvent)o;

		if (getMazeVariable() != null ? !getMazeVariable().equals(that.getMazeVariable()) : that.getMazeVariable() != null)
		{
			return false;
		}
		return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getMazeVariable() != null ? getMazeVariable().hashCode() : 0;
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		return result;
	}
}

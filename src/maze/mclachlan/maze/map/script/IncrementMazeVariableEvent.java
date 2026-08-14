/*
 * Copyright (c) 2026 Alan McLachlan
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

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;

/**
 * Adds {@link #amount} to an integer maze variable (missing values start at 0).
 */
public class IncrementMazeVariableEvent extends MazeEvent
{
	private String mazeVariable;
	private int amount;

	public IncrementMazeVariableEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public IncrementMazeVariableEvent(String mazeVariable, int amount)
	{
		this.mazeVariable = mazeVariable;
		this.amount = amount;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		String current = MazeVariables.get(mazeVariable);
		int base = 0;
		if (current != null && !current.isEmpty())
		{
			base = Integer.parseInt(current);
		}
		MazeVariables.set(mazeVariable, Integer.toString(base + amount));
		return null;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
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

		IncrementMazeVariableEvent that = (IncrementMazeVariableEvent)o;

		if (getAmount() != that.getAmount())
		{
			return false;
		}
		return getMazeVariable() != null
			? getMazeVariable().equals(that.getMazeVariable())
			: that.getMazeVariable() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getMazeVariable() != null ? getMazeVariable().hashCode() : 0;
		result = 31 * result + getAmount();
		return result;
	}
}

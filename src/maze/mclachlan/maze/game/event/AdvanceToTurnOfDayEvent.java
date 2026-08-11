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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;

/**
 * Advances the global turn clock forward to the next occurrence of a target
 * turn-of-day without rewinding time. Does not run end-of-turn logic for the
 * skipped turns.
 */
public class AdvanceToTurnOfDayEvent extends MazeEvent
{
	private int turnOfDay;
	private String onceMazeVariable;

	public AdvanceToTurnOfDayEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public AdvanceToTurnOfDayEvent(int turnOfDay, String onceMazeVariable)
	{
		this.turnOfDay = turnOfDay;
		this.onceMazeVariable = onceMazeVariable;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		if (onceMazeVariable != null && !onceMazeVariable.isEmpty()
			&& MazeVariables.getBoolean(onceMazeVariable))
		{
			return null;
		}

		long current = GameTime.getTurnNr();
		GameTime.setTurnNr(GameTime.computeForwardAdvanceToTurnOfDay(current, turnOfDay));

		if (onceMazeVariable != null && !onceMazeVariable.isEmpty())
		{
			MazeVariables.set(onceMazeVariable, "true");
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getTurnOfDay()
	{
		return turnOfDay;
	}

	public void setTurnOfDay(int turnOfDay)
	{
		this.turnOfDay = turnOfDay;
	}

	public String getOnceMazeVariable()
	{
		return onceMazeVariable;
	}

	public void setOnceMazeVariable(String onceMazeVariable)
	{
		this.onceMazeVariable = onceMazeVariable;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AdvanceToTurnOfDayEvent))
		{
			return false;
		}

		AdvanceToTurnOfDayEvent that = (AdvanceToTurnOfDayEvent)o;

		if (getTurnOfDay() != that.getTurnOfDay())
		{
			return false;
		}
		return Objects.equals(getOnceMazeVariable(), that.getOnceMazeVariable());
	}

	@Override
	public int hashCode()
	{
		int result = getTurnOfDay();
		result = 31 * result + (getOnceMazeVariable() != null ? getOnceMazeVariable().hashCode() : 0);
		return result;
	}
}

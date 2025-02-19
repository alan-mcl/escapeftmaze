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

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.diygui.FlavourTextDialog;


/**
 *
 */
public class FlavourTextEvent extends MazeEvent
{
	private String flavourText;
	private int delay;
	private boolean shouldClearText;

	public FlavourTextEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText)
	{
		this(flavourText, Delay.WAIT_ON_CLICK, false);
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText, int delay, boolean shouldClearText)
	{
		this.flavourText = flavourText;
		this.delay = delay;
		this.shouldClearText = shouldClearText;
	}

	/*-------------------------------------------------------------------------*/
	public String getFlavourText()
	{
		return flavourText;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().journalInContext(flavourText);
		Maze.getInstance().getUi().showDialog(new FlavourTextDialog(null, flavourText));
		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getDelay()
	{
		return delay;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean shouldClearText()
	{
		return shouldClearText;
	}

	public void setFlavourText(String flavourText)
	{
		this.flavourText = flavourText;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	public boolean isShouldClearText()
	{
		return shouldClearText;
	}

	public void setShouldClearText(boolean shouldClearText)
	{
		this.shouldClearText = shouldClearText;
	}

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

		FlavourTextEvent that = (FlavourTextEvent)o;

		if (getDelay() != that.getDelay())
		{
			return false;
		}
		if (isShouldClearText() != that.isShouldClearText())
		{
			return false;
		}
		return getFlavourText() != null ? getFlavourText().equals(that.getFlavourText()) : that.getFlavourText() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getFlavourText() != null ? getFlavourText().hashCode() : 0;
		result = 31 * result + getDelay();
		result = 31 * result + (isShouldClearText() ? 1 : 0);
		return result;
	}
}

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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.diygui.FlavourTextDialog;


/**
 *
 */
public class FlavourTextEvent extends MazeEvent
{
	private String flavourText;
	private String coldStringKey;
	private int delay;
	private boolean shouldClearText;
	private Alignment alignment = Alignment.CENTER;

	public enum Alignment
	{
		CENTER, TOP, BOTTOM
	}

	public FlavourTextEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText)
	{
		this(flavourText, Delay.WAIT_ON_CLICK, false, Alignment.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText, Alignment alignment)
	{
		this(flavourText, Delay.WAIT_ON_CLICK, false, alignment);
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText, int delay, boolean shouldClearText)
	{
		this(flavourText, delay, shouldClearText, Alignment.CENTER);
	}
	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String flavourText, int delay, boolean shouldClearText, Alignment alignment)
	{
		this.flavourText = flavourText;
		this.delay = delay;
		this.shouldClearText = shouldClearText;
		this.alignment = alignment;
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
		String text = resolveText();
		Maze.getInstance().journalInContext(text);
		Maze.getInstance().getUi().showDialog(new FlavourTextDialog(null, text, alignment));
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private String resolveText()
	{
		if (coldStringKey != null && coldStringKey.length() > 0)
		{
			return StringUtil.getColdString(coldStringKey);
		}
		return flavourText;
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

	public String getColdStringKey()
	{
		return coldStringKey;
	}

	public void setColdStringKey(String coldStringKey)
	{
		this.coldStringKey = coldStringKey;
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

	public Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(
		Alignment alignment)
	{
		this.alignment = alignment;
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
		if (getColdStringKey() != null ? !getColdStringKey().equals(that.getColdStringKey()) : that.getColdStringKey() != null)
		{
			return false;
		}
		return getFlavourText() != null ? getFlavourText().equals(that.getFlavourText()) : that.getFlavourText() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getFlavourText() != null ? getFlavourText().hashCode() : 0;
		result = 31 * result + (getColdStringKey() != null ? getColdStringKey().hashCode() : 0);
		result = 31 * result + getDelay();
		result = 31 * result + (isShouldClearText() ? 1 : 0);
		return result;
	}
}

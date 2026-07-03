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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;

/**
 *
 */
public class FlavourText extends TileScript
{
	private String text;
	private String coldStringKey;
	private FlavourTextEvent.Alignment alignment;

	public FlavourText()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FlavourText(String text, FlavourTextEvent.Alignment alignment)
	{
		this.text = text;
		this.alignment = alignment;
	}

	/*-------------------------------------------------------------------------*/
	protected FlavourText(FlavourText copy)
	{
		super(copy);
		text = copy.text;
		coldStringKey = copy.coldStringKey;
		alignment = copy.alignment;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public TileScript copyScript()
	{
		return new FlavourText(this);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();
		String resolved = resolveText();
		result.add(new FlavourTextEvent(resolved, MazeEvent.Delay.WAIT_ON_CLICK, true, alignment));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private String resolveText()
	{
		if (coldStringKey != null && coldStringKey.length() > 0)
		{
			return StringUtil.getColdString(coldStringKey);
		}
		return text;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getColdStringKey()
	{
		return coldStringKey;
	}

	public void setColdStringKey(String coldStringKey)
	{
		this.coldStringKey = coldStringKey;
	}

	public FlavourTextEvent.Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(
		FlavourTextEvent.Alignment alignment)
	{
		this.alignment = alignment;
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

		FlavourText that = (FlavourText)o;

		if (getColdStringKey() != null ? !getColdStringKey().equals(that.getColdStringKey()) : that.getColdStringKey() != null)
		{
			return false;
		}
		return getText() != null ? getText().equals(that.getText()) : that.getText() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getText() != null ? getText().hashCode() : 0);
		result = 31 * result + (getColdStringKey() != null ? getColdStringKey().hashCode() : 0);
		return result;
	}
}

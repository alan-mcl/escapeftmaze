/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.ui.diygui;

import java.awt.Color;
import java.awt.Dimension;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class FilledBarWidget extends Widget implements ProgressListener
{
	private int current;
	private int max;
	private int sub = -1;

	private Orientation orientation = Orientation.HORIZONTAL;

	private Color barColour = Color.GRAY, subBarColour = Color.LIGHT_GRAY;

	private InnerTextType textType = InnerTextType.NONE;
	private String customText;

	private ProgressListenerCallback callback;

	/*-------------------------------------------------------------------------*/
	public enum InnerTextType
	{
		NONE, CURRENT, CURRENT_AND_MAX, PERCENT, CUSTOM;
	}

	public enum Orientation
	{
		HORIZONTAL, VERTICAL
	}

	/*-------------------------------------------------------------------------*/
	public FilledBarWidget(int current, int max)
	{
		super(0, 0, 0, 0);
		this.current = current;
		this.max = max;
	}

	/*-------------------------------------------------------------------------*/
	protected FilledBarWidget(int x, int y, int width, int height, int current, int max)
	{
		super(x, y, width, height);

		this.current = current;
		this.max = max;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void incProgress(int amount)
	{
		this.current += amount;
		callback.callback(this.current);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void message(String msg)
	{
		callback.message(msg);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setCallback(ProgressListenerCallback callback)
	{
		this.callback = callback;
	}

	/*-------------------------------------------------------------------------*/
	public void setFromCurMax(CurMax cm)
	{
		this.setMax(cm.getMaximum());
		this.setCurrent(cm.getCurrent());
	}

	/*-------------------------------------------------------------------------*/
	public void setFromCurMaxSub(CurMaxSub cm)
	{
		this.setMax(cm.getMaximum());
		this.setCurrent(cm.getCurrent());
		this.setSub(cm.getSub());
	}

	/*-------------------------------------------------------------------------*/
	public void setPercent(int percent)
	{
		this.setMax(100);
		this.setCurrent(percent);
		this.setSub(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void set(int cur, int max)
	{
		this.setCurrent(cur);
		this.setMax(max);
		this.setSub(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void set(int cur, int max, int sub)
	{
		this.setCurrent(cur);
		this.setMax(max);
		this.setSub(sub);
	}

	/*-------------------------------------------------------------------------*/
	public int getCurrent()
	{
		return current;
	}

	public void setCurrent(int current)
	{
		this.current = current;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public int getSub()
	{
		return sub;
	}

	public void setSub(int sub)
	{
		this.sub = sub;
	}

	public InnerTextType getTextType()
	{
		return textType;
	}

	public void setTextType(InnerTextType textType)
	{
		this.textType = textType;
	}

	public Orientation getOrientation()
	{
		return orientation;
	}

	public void setOrientation(
		Orientation orientation)
	{
		this.orientation = orientation;
	}

	public Color getBarColour()
	{
		return barColour;
	}

	public void setBarColour(Color barColour)
	{
		this.barColour = barColour;
	}

	public Color getSubBarColour()
	{
		return subBarColour;
	}

	public void setSubBarColour(Color subBarColour)
	{
		this.subBarColour = subBarColour;
	}

	public ProgressListenerCallback getCallback()
	{
		return callback;
	}

	public String getCustomText()
	{
		return customText;
	}

	public void setCustomText(String customText)
	{
		this.customText = customText;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.FILLED_BAR_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}

}

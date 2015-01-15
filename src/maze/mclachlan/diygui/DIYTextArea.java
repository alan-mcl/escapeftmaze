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

package mclachlan.diygui;

import java.awt.*;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;

/**
 *
 */
public class DIYTextArea extends ContainerWidget
{
	private DIYToolkit.Align alignment;
	private String text;
	private boolean transparent;
	private Font font;

	/*-------------------------------------------------------------------------*/
	public DIYTextArea(String text)
	{
		super(0, 0, 1, 1);
		this.text = text;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isTransparent()
	{
		return transparent;
	}

	/*-------------------------------------------------------------------------*/
	public void setTransparent(boolean transparent)
	{
		this.transparent = transparent;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.TEXT_AREA;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		return DIYToolkit.getDimension(text);
	}

	/*-------------------------------------------------------------------------*/
	public DIYToolkit.Align getAlignment()
	{
		return alignment;
	}

	/*-------------------------------------------------------------------------*/
	public void setAlignment(DIYToolkit.Align alignment)
	{
		this.alignment = alignment;
	}

	/*-------------------------------------------------------------------------*/
	public Font getFont()
	{
		return font;
	}

	/*-------------------------------------------------------------------------*/
	public void setFont(Font font)
	{
		this.font = font;
	}

	public void addText(String s)
	{
		text = text+s;
	}
}

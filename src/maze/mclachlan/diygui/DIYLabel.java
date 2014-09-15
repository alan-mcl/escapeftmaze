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

import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import java.awt.*;

/**
 *
 */
public class DIYLabel extends Widget
{
	private String text;
	private Font font;
	private Image icon;
	private DIYToolkit.Align align = DIYToolkit.Align.CENTER;

	/*-------------------------------------------------------------------------*/
	public DIYLabel(String text)
	{
		super(0, 0, 1, 1);
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public DIYLabel(Image icon)
	{
		super(0, 0, 1, 1);
		this.icon = icon;
	}

	/*-------------------------------------------------------------------------*/
	public DIYLabel()
	{
		this("");
	}

	/*-------------------------------------------------------------------------*/
	public DIYLabel(String text, DIYToolkit.Align align)
	{
		super(0, 0, 1, 1);
		this.text = text;
		this.align = align;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setAlignment(DIYToolkit.Align align)
	{
		this.align = align;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.LABEL;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public DIYToolkit.Align getAlignment()
	{
		return align;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension d = DIYToolkit.getDimension(this.text, this.font);
		return new Dimension(d.width+8, d.height);
	}

	/*-------------------------------------------------------------------------*/
	public void setFont(Font f)
	{
		this.font = f;
	}

	/*-------------------------------------------------------------------------*/
	public Font getFont()
	{
		return font;
	}

	/*-------------------------------------------------------------------------*/
	public DIYToolkit.Align getAlign()
	{
		return align;
	}

	/*-------------------------------------------------------------------------*/
	public Image getIcon()
	{
		return icon;
	}

	/*-------------------------------------------------------------------------*/
	public void setIcon(Image icon)
	{
		this.icon = icon;
	}
}

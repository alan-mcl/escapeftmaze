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

import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.LayoutManager;
import java.awt.*;

/**
 *
 */
public class DIYPanel extends ContainerWidget
{
	/**
	 * Hints for the renderer.
	 */
	public enum Style
	{
		TRANSPARENT,
		PANEL_HEAVY,
		PANEL_MED,
		PANEL_LIGHT,
		IMAGE_BACK,
		DIALOG
	}

	private Style style = Style.TRANSPARENT;
	private Image background;
	
	/*-------------------------------------------------------------------------*/
	public DIYPanel()
	{
		super(0, 0, 1, 1);
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
	}

	/*-------------------------------------------------------------------------*/
	public DIYPanel(LayoutManager layout)
	{
		super(0,0,1,1);
		super.setLayoutManager(layout);
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYPanel(Rectangle bounds)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}
	
	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		if (this.background != null)
		{
			Component obsrvr = DIYToolkit.getInstance().getComponent();
			int width = background.getWidth(obsrvr);
			int height = background.getHeight(obsrvr);
			
			return new Dimension(width, height);
		}
		else
		{
			return super.getPreferredSize();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setBackgroundImage(Image background)
	{
		this.style = Style.IMAGE_BACK;
		this.background = background;
	}

	public Image getBackgroundImage()
	{
		return background;
	}

	public Style getStyle()
	{
		return style;
	}

	public void setStyle(Style style)
	{
		this.style = style;
	}
}

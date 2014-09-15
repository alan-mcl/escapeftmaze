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

package mclachlan.maze.ui.diygui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import mclachlan.maze.util.MazeException;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;

/**
 * Manages a bunch of other widgets
 */
public class CardLayoutWidget extends ContainerWidget
{
	private ContainerWidget currentWidget;
	ArrayList<ContainerWidget> widgets;
	
	/*-------------------------------------------------------------------------*/
	public CardLayoutWidget(Rectangle bounds, ArrayList<ContainerWidget> widgets)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);
		this.widgets = widgets;
		this.currentWidget = this.widgets.get(0);
		for (Widget w : widgets)
		{
			add(w);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget getCurrentWidget()
	{
		return currentWidget;
	}

	/*-------------------------------------------------------------------------*/
	public void show(ContainerWidget w)
	{
		if (!this.widgets.contains(w))
		{
			throw new MazeException("Not a child widget: "+w);
		}

		
		this.currentWidget = w;
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		this.currentWidget.draw(g);
	}
	
	/*-------------------------------------------------------------------------*/
	public void doLayout()
	{
		for (ContainerWidget w : widgets)
		{
			w.doLayout();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Widget getHoverComponent(int x, int y)
	{
		return this.currentWidget.getHoverComponent(x, y);
	}

	/*-------------------------------------------------------------------------*/
	public Widget getChild(int x, int y)
	{
		return this.currentWidget.getChild(x, y);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		this.currentWidget.processMouseClicked(e); 
	}

	public void processMouseEntered(MouseEvent e)
	{
		this.currentWidget.processMouseEntered(e);
	}

	public void processMouseExited(MouseEvent e)
	{
		this.currentWidget.processMouseExited(e);
	}

	public void processMousePressed(MouseEvent e)
	{
		this.currentWidget.processMousePressed(e);
	}

	public void processMouseReleased(MouseEvent e)
	{
		this.currentWidget.processMouseReleased(e);
	}

	/*-------------------------------------------------------------------------*/
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);

		if (widgets != null)
		{
			for (ContainerWidget w : widgets)
			{
				w.setBounds(x, y, width, height);
			}
		}
	}
}

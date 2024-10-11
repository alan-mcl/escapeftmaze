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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.util.MazeException;

/**
 * Manages a bunch of other widgets
 */
public class CardLayoutWidget extends ContainerWidget
{
	private ContainerWidget currentWidget;
	private final Map<Object, ContainerWidget> widgets;
	
	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a card layout widget without any keys. Show method must be
	 * called with the actual widget to show.
	 */
	public CardLayoutWidget(Rectangle bounds, ArrayList<ContainerWidget> widgets)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);
		this.currentWidget = widgets.get(0);

		this.widgets = new HashMap<>();
		for (ContainerWidget w : widgets)
		{
			this.widgets.put(System.identityHashCode(w), w);
		}

		for (Widget w : widgets)
		{
			add(w);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a card layout widget where cards are referenced by keys. Show
	 * method can be called with either key or actual widget.
	 */
	public CardLayoutWidget(Rectangle bounds,
		Map<Object, ContainerWidget> widgets)
	{
		super(bounds);
		this.widgets = widgets;
		this.currentWidget = widgets.values().iterator().next();

		for (Widget w : widgets.values())
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
		if (!this.widgets.containsValue(w))
		{
			throw new MazeException("Not a child widget: "+w);
		}

		this.currentWidget = w;
	}

	/*-------------------------------------------------------------------------*/
	public void show(Object key)
	{
		if (!this.widgets.containsKey(key))
		{
			throw new MazeException("Not a child widget: "+key);
		}

		this.currentWidget = this.widgets.get(key);
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		this.currentWidget.draw(g);
	}
	
	/*-------------------------------------------------------------------------*/
	public void doLayout()
	{
		for (ContainerWidget w : widgets.values())
		{
			w.doLayout();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Widget getHoverComponent(int x, int y)
	{
		if (this.currentWidget != null)
		{
			return this.currentWidget.getHoverComponent(x, y);
		}
		else
		{
			return null;
		}
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
			for (ContainerWidget w : widgets.values())
			{
				w.setBounds(x, y, width, height);
			}
		}
	}
}

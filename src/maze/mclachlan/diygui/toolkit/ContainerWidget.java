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

package mclachlan.diygui.toolkit;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Contains other widgets.
 */
public abstract class ContainerWidget extends Widget
{
	private final Object childMutex = new Object();
	/**
	 * Children of this widget, sorted in ascending z-order
	 */
	List<Widget> children = new ArrayList<>();
	LayoutManager layoutManager;
	Insets insets = new Insets(0,0,0,0);
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget(Rectangle bounds)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height);
	}
	
	/*-------------------------------------------------------------------------*/
	public void setLayoutManager(LayoutManager layoutManager)
	{
		this.layoutManager = layoutManager;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setInsets(Insets insets)
	{
		this.insets = insets;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		synchronized (childMutex)
		{
			int width = this.width;
			int height = this.height;

			for (Widget w : children)
			{
				Dimension size = w.getPreferredSize();
				if (size.width > width)
				{
					width = size.width;
				}
				if (size.height > height)
				{
					height = size.height;
				}
			}

			return new Dimension(width, height);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds the given widget to this container.
	 */ 
	public void add(Widget w)
	{
		this.add(w, null);
	}
	
	/*-------------------------------------------------------------------------*/
	public void add(Widget w, Object constraints)
	{
		synchronized (childMutex)
		{
			this.children.add(w);
		}
		w.parent = this;
		if (layoutManager != null)
		{
			this.layoutManager.addWidgetToLayout(w, constraints);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void remove(Widget w)
	{
		synchronized (childMutex)
		{
			this.children.remove(w);
		}
		if (layoutManager != null)
		{
			this.layoutManager.removeWidgetFromLayout(w);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		super.draw(g);
		synchronized (childMutex)
		{
			int max = children.size();
			for (int i=0; i<max; i++)
			{
				Widget w = children.get(i);
				w.draw(g);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void doLayout()
	{
		if (this.layoutManager != null)
		{
			this.layoutManager.layoutContainer(this);
		}
		
		for (Widget w : children)
		{
			if (w instanceof ContainerWidget)
			{
				((ContainerWidget)w).doLayout();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Widget getHoverComponent(int x, int y)
	{
		ContainerWidget d = DIYToolkit.getInstance().getDialog();
		if (d != null && d != this && !this.isChildOfDialog(d))
		{
			// there is a modal dialog visible
			Rectangle bounds = new Rectangle(d.x, d.y, d.width, d.height);
			if (bounds.contains(x, y))
			{
				return d.getHoverComponent(x, y);
			}
			else
			{
				return null;
			}
		}

		return getChild(x, y);
	}

	/*-------------------------------------------------------------------------*/
	public Widget getChild(int x, int y)
	{
		List<Widget> kids = new ArrayList<>(children);
		Collections.reverse(kids);

		for (Widget w : kids)
		{
			Rectangle bounds = new Rectangle(w.x,  w.y,  w.width, w.height);
			if (bounds.contains(x, y))
			{
				if (w instanceof ContainerWidget)
				{
					return ((ContainerWidget)w).getChild(x, y);
				}
				else if (w.isVisible())
				{
					return w;
				}
			}
		}

		return this;
	}

	/*-------------------------------------------------------------------------*/
	private boolean isChildOfDialog(ContainerWidget dialog)
	{
		Widget w = this.parent;

		while (w != null)
		{
			if (w == dialog)
			{
				return true;
			}
			w = w.parent;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public List<Widget> getChildren()
	{
		return children;
	}

	/*-------------------------------------------------------------------------*/
	public void removeAllChildren()
	{
		List<Widget> kids = new ArrayList<>(children);

		for (Widget w : kids)
		{
			remove(w);
		}
	}
}

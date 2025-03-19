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

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class DIYListBox extends ContainerWidget
{
	private Object selected;
	private List items;
	private Object lastClicked;

	/*-------------------------------------------------------------------------*/
	public DIYListBox(List items)
	{
		super(0, 0, 1, 1);
		this.items = items;

		setItems(items);
	}

	/*-------------------------------------------------------------------------*/
	public DIYListBox(List items, Rectangle bounds)
	{
		super(bounds);
		setItems(items);
	}

	/*-------------------------------------------------------------------------*/
	public void setItems(List items)
	{
		this.items = items;
		
		List<Widget> children = new ArrayList<>(this.getChildren());
		for (Widget w : children)
		{
			this.remove(w);
		}

		int itemHeight;
		Dimension dimension = DIYToolkit.getDimension("|");
		if (dimension != null)
		{
			itemHeight = dimension.height +2;
		}
		else
		{
			itemHeight = 18;
		}

		int rows = Math.max(height/itemHeight, items.size());
		this.setLayoutManager(new DIYGridLayout(1, rows, 0, 0));

		for (Object item : items)
		{
			if (item instanceof ListItemWithIcon)
			{
				this.add(new ListItem(((ListItemWithIcon)item).getItem(), ((ListItemWithIcon)item).getIcon()));
			}
			else
			{
				this.add(new ListItem(item));
			}
		}

		doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		// dodgy hack: we render the list items instead.  This is necessitated by 
		// being a ContainerWidget to make click handling easier.
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if (DIYToolkit.debug)
		{
			g.setColor(Color.MAGENTA);
			g.drawRect(x, y, width, height);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		int estWidth = 0;
		int estHeight = 0;

		for (Object item : items)
		{
			String s = item.toString();
			Dimension d = DIYToolkit.getDimension(s);

			if (estWidth < d.width)
			{
				estWidth = d.width;
			}
			if (estHeight < d.height)
			{
				estHeight = d.height;
			}
		}

		// to accomodate list boxes that start empty and get things added.
		int minWidth = (this.width == 0) ? 50 : this.width;
		int minHeight = (this.height == 0) ? 200 : this.height;

		return new Dimension(
			Math.max(minWidth, estWidth),
			Math.max(minHeight, (estHeight+2)*items.size()));
	}
	
	/*-------------------------------------------------------------------------*/
	public List getItems()
	{
		return items;
	}
	
	/*-------------------------------------------------------------------------*/
	public Object getSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(Object selected)
	{
		this.selected = selected;
	}

	/*-------------------------------------------------------------------------*/
	public void setEnabled(Object item, boolean enabled)
	{
		for (Widget w : getChildren())
		{
			ListItem listItem = (ListItem)w;
			if (listItem.item.equals(item))
			{
				listItem.setEnabled(enabled);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEnabled(Object item)
	{
		for (Widget w : getChildren())
		{
			ListItem listItem = (ListItem)w;
			if (listItem.item.equals(item))
			{
				return listItem.isEnabled();
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public Object getLastClicked()
	{
		return this.lastClicked;
	}

	/*-------------------------------------------------------------------------*/
	public void moveSelectionUp()
	{
		if (items.size() == 0)
		{
			return;
		}

		int index;
		if (selected == null)
		{
			index = items.size()-1;
		}
		else
		{
			index = items.indexOf(selected);
		}

		if (index > 0)
		{
			do
			{
				index--;
			}
			while (index > 0 && !isEnabled(items.get(index)));
		}

		if (isEnabled(items.get(index)))
		{
			selected = items.get(index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void moveSelectionDown()
	{
		if (items.size() == 0)
		{
			return;
		}

		int index;
		if (selected == null)
		{
			index = 0;
		}
		else
		{
			index = items.indexOf(selected);
		}

		if (index < items.size()-1)
		{
			do
			{
				index++;
			}
			while(index < items.size()-1 && !isEnabled(items.get(index)));
		}

		if (isEnabled(items.get(index)))
		{
			selected = items.get(index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP ->
			{
				e.consume();
				moveSelectionUp();
				this.notifyListeners(e);
			}
			case KeyEvent.VK_DOWN ->
			{
				e.consume();
				moveSelectionDown();
				this.notifyListeners(e);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public interface ListItemWithIcon
	{
		Object getItem();
		BufferedImage getIcon();
	}

	/*-------------------------------------------------------------------------*/
	public class ListItem extends Widget
	{
		private boolean hover;
		private final Image icon;
		private final Object item;

		/*----------------------------------------------------------------------*/

		public ListItem(Object item)
		{
			this(item, null);
		}

		/*----------------------------------------------------------------------*/
		public ListItem(Object item, Image icon)
		{
			super(0,0,1,1);
			this.item = item;
			this.icon = icon;
		}

		/*----------------------------------------------------------------------*/
		public Dimension getPreferredSize()
		{
			Dimension dimension = DIYToolkit.getDimension(this.item.toString());
			dimension.setSize(dimension.getWidth(), dimension.getHeight());
			return dimension;
		}

		/*----------------------------------------------------------------------*/
		public String getWidgetName()
		{
			// the least of the hacks going on here
			return DIYToolkit.LIST_BOX_ITEM;
		}
		
		/*----------------------------------------------------------------------*/
		public void processMouseClicked(MouseEvent e)
		{
			DIYListBox.this.lastClicked = this.item;
			if (isEnabled())
			{
				DIYListBox.this.setSelected(this.item);
			}

			// dodgy hack to ensure that the parent ActionListeners see this
			DIYListBox.this.processMouseClicked(e);
		}

		@Override
		public void processKeyPressed(KeyEvent e)
		{
			parent.processKeyPressed(e);
		}

		/*----------------------------------------------------------------------*/
		public DIYListBox getParent()
		{
			return DIYListBox.this;
		}
		
		/*----------------------------------------------------------------------*/
		public Object getItem()
		{
			return item;
		}

		/*----------------------------------------------------------------------*/

		public Image getIcon()
		{
			return icon;
		}

		/*----------------------------------------------------------------------*/

		public boolean isHover()
		{
			return hover;
		}

		/*----------------------------------------------------------------------*/
		public String toString()
		{
			return item.toString();
		}

		/*----------------------------------------------------------------------*/

		@Override
		public void processMouseEntered(MouseEvent e)
		{
			this.hover = true;
		}

		@Override
		public void processMouseExited(MouseEvent e)
		{
			this.hover = false;
		}
	}
}

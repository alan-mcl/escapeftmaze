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

/**
 *
 */
public class DIYBorderLayout extends LayoutManager
{
	private int vgap;
	private int hgap;
	
	Widget north, south, east, west, center;

	public static enum Constraint
	{
		NORTH, SOUTH, EAST, WEST, CENTER
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYBorderLayout()
	{
		this(0,0);
	}

	/*-------------------------------------------------------------------------*/
	public DIYBorderLayout(int hgap, int vgap)
	{
		this.hgap = hgap;
		this.vgap = vgap;
	}
	
	/*-------------------------------------------------------------------------*/
	public void addWidgetToLayout(Widget w, Object constraints)
	{
		if (constraints == null)
		{
			this.center = w;
		}
		else if(constraints == Constraint.NORTH)
		{
			this.north = w;
		}
		else if (constraints == Constraint.SOUTH)
		{
			this.south = w;
		}
		else if (constraints == Constraint.EAST)
		{
			this.east = w;
		}
		else if (constraints == Constraint.WEST)
		{
			this.west = w;
		}
		else if (constraints == Constraint.CENTER)
		{
			this.center = w;
		}
		else
		{
			throw new DIYException("Invalid layout constraints ["+constraints+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	public void layoutContainer(ContainerWidget parent)
	{
		Insets insets = parent.insets;
		int top = parent.y + insets.top;
		int bottom = parent.y + parent.height - insets.bottom;
		int left = parent.x + insets.left;
		int right = parent.x + parent.width - insets.right;

		Widget c = null;

		if ((c = getChild(Constraint.NORTH)) != null)
		{
//			c.setSize(right - left, c.getHeight());
			Dimension d = c.getPreferredSize();
			c.setBounds(left, top, right - left, d.height);
			top += d.height + vgap;
		}
		if ((c = getChild(Constraint.SOUTH)) != null)
		{
//			c.setSize(right - left, c.getHeight());
			Dimension d = c.getPreferredSize();
			c.setBounds(left, bottom - d.height, right - left, d.height);
			bottom -= d.height + vgap;
		}
		if ((c = getChild(Constraint.EAST)) != null)
		{
//			c.setSize(c.getWidth(), bottom - top);
			Dimension d = c.getPreferredSize();
			c.setBounds(right - d.width, top, d.width, bottom - top);
			right -= d.width + hgap;
		}
		if ((c = getChild(Constraint.WEST)) != null)
		{
//			c.setSize(c.getWidth(), bottom - top);
			Dimension d = c.getPreferredSize();
			c.setBounds(left, top, d.width, bottom - top);
			left += d.width + hgap;
		}
		if ((c = getChild(Constraint.CENTER)) != null)
		{
			c.setBounds(left, top, right - left, bottom - top);
		}
	}

	/*-------------------------------------------------------------------------*/
	private Widget getChild(Constraint key)
	{
		if (key == Constraint.NORTH)
		{
			return north;
		}
		else if (key == Constraint.SOUTH)
		{
			return south;
		}
		else if (key == Constraint.WEST)
		{
			return west;
		}
		else if (key == Constraint.EAST)
		{
			return east;
		}
		else if (key == Constraint.CENTER)
		{
			return center;
		}
		else
		{
			return null;
		}
	}
}

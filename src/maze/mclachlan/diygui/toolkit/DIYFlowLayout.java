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

import java.awt.Dimension;
import java.awt.Insets;

/**
 *
 */
public class DIYFlowLayout extends LayoutManager
{
	private int hgap;
	private int vgap;
	private DIYToolkit.Align alignment;

	/*-------------------------------------------------------------------------*/
	public DIYFlowLayout()
	{
		this(0,0, DIYToolkit.Align.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public DIYFlowLayout(int hgap, int vgap, DIYToolkit.Align alignment)
	{
		this.hgap = hgap;
		this.vgap = vgap;
		this.alignment = alignment;
	}

	/*-------------------------------------------------------------------------*/
	public void layoutContainer(ContainerWidget parent)
	{
		Insets insets = parent.insets;
		if (insets == null)
		{
			insets = new Insets(0,0,0,0);
		}

		int max = parent.children.size();
		int sumWidth = 0;

		for (int i = 0; i < max; i++)
		{
			Widget w = parent.children.get(i);
			Dimension d = w.getPreferredSize();
			w.width = Math.min(d.width, parent.width);
			w.height = Math.min(d.height, parent.height);
			sumWidth += w.width;
		}

		sumWidth += hgap*(max-1);
		int startY = parent.y + insets.top + vgap;
		int startX;
		switch (alignment)
		{
			case LEFT:
				startX = parent.x + insets.left;
				break;
			case CENTER:
				startX = parent.x + parent.width/2 - sumWidth/2;
				break;
			case RIGHT:
				startX = parent.x + parent.width - insets.right - sumWidth;
				break;
			default:
				throw new RuntimeException("Invalid Alignment: "+alignment);
		}

		for (int i=0; i<max; i++)
		{
			Widget w = parent.getChildren().get(i);
			w.setBounds(startX, startY, w.width, w.height);
			startX += w.width + hgap;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addWidgetToLayout(Widget w, Object constraints)
	{
		// no op
	}
}

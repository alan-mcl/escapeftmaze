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
public class DIYGridLayout extends LayoutManager
{
	private int rows;
	private int columns;
	private int hgap;
	private int vgap;

	/*-------------------------------------------------------------------------*/
	public DIYGridLayout(int cols, int rows, int hgap, int vgap)
	{
		this.columns = cols;
		this.rows = rows;
		this.hgap = hgap;
		this.vgap = vgap;
	}
	
	/*-------------------------------------------------------------------------*/
	public void layoutContainer(ContainerWidget parent)
	{
		Insets insets = parent.insets;
		if (insets == null)
		{
			insets = new Insets(0,0,0,0);
		}
		int ncomponents = parent.children.size();
		int nrows = rows;
		int ncols = columns;
		
		if (ncomponents == 0)
		{
			return;
		}
//		if (nrows > 0)
//		{
//			ncols = (ncomponents + nrows - 1) / nrows;
//		}
//		else
//		{
//			nrows = (ncomponents + ncols - 1) / ncols;
//		}
		
		int w = parent.width - (insets.left + insets.right);
		int h = parent.height - (insets.top + insets.bottom);
		w = (w - (ncols - 1) * hgap) / ncols;
		h = (h - (nrows - 1) * vgap) / nrows;
		
		for (int r = 0, y = insets.top; r < nrows; r++, y += h + vgap)
		{
			for (int c = 0, x = insets.left; c < ncols; c++, x += w + hgap)
			{
				int i = r * ncols + c;
				if (i < ncomponents)
				{
					Widget widget = parent.children.get(i);
					int xx = parent.x + x;
					int yy = parent.y + y;
					widget.setBounds(xx, yy, w, h);
				}
			}
		}
	}	
	
	/*-------------------------------------------------------------------------*/
	public void addWidgetToLayout(Widget w, Object contraints)
	{
		
	}
}

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

import mclachlan.diygui.toolkit.*;
import java.awt.*;

/**
 *
 */
public class DIYPane extends ContainerWidget
{
	/*-------------------------------------------------------------------------*/
	public DIYPane()
	{
		super(0,0,1,1);
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYPane(mclachlan.diygui.toolkit.LayoutManager layout)
	{
		super(0,0,1,1);
		super.setLayoutManager(layout);
	}

	/*-------------------------------------------------------------------------*/
	public DIYPane(int x, int y, int width, int height)
	{
		super(x, y, width, height);
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYPane(Rectangle bounds)
	{
		super(bounds);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}
}

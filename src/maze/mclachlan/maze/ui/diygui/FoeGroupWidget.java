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

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;

/**
 * A status bar of a group of foes.
 */
public class FoeGroupWidget extends ContainerWidget
{
	private FoeGroup group;
	private Rectangle bounds;

	/*-------------------------------------------------------------------------*/
	public FoeGroupWidget(FoeGroup group, Rectangle bounds)
	{
		super(bounds);
		this.group = group;
		this.bounds = bounds;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setFoeGroup(FoeGroup group)
	{
		this.group = group;
	}

	/*-------------------------------------------------------------------------*/
	public FoeGroup getFoeGroup()
	{
		return group;
	}

	/*-------------------------------------------------------------------------*/
	public boolean processMouseClicked(MouseEvent e)
	{
		return parent.processMouseClicked(e);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return MazeRendererFactory.FOE_GROUP_WIDGET;
	}
}

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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class CompassWidget extends ContainerWidget
{
	private final DIYLabel compass;
	
	/*-------------------------------------------------------------------------*/
	public CompassWidget()
	{
		this(0, 0, 1, 1);
	}

	/*-------------------------------------------------------------------------*/
	protected CompassWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		setLayoutManager(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));

		compass = new DIYLabel("", DIYToolkit.Align.CENTER);
		compass.setIcon(Database.getInstance().getImage("screen/compass_frame"));
		compass.setIconAlign(DIYToolkit.Align.TOP);

		add(compass);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}
	
	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		int facing = Maze.getInstance().getFacing();
		
		if (facing != -1)
		{
			switch (facing)
			{
				case CrusaderEngine.Facing.NORTH -> compass.setIcon(Database.getInstance().getImage("screen/compass_north"));
				case CrusaderEngine.Facing.SOUTH -> compass.setIcon(Database.getInstance().getImage("screen/compass_south"));
				case CrusaderEngine.Facing.EAST -> compass.setIcon(Database.getInstance().getImage("screen/compass_east"));
				case CrusaderEngine.Facing.WEST -> compass.setIcon(Database.getInstance().getImage("screen/compass_west"));
				default -> throw new MazeException("Invalid facing :" + facing);
			}
		}
		
		super.draw(g);		
	}
}

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

import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import java.awt.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;
import mclachlan.crusader.CrusaderEngine;

/**
 *
 */
public class Compass extends ContainerWidget
{
	private final DIYLabel direction;
	
	/*-------------------------------------------------------------------------*/
	public Compass()
	{
		this(0, 0, 1, 1);
	}

	/*-------------------------------------------------------------------------*/
	protected Compass(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		setLayoutManager(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
		direction = new DIYLabel("", DIYToolkit.Align.CENTER);
		add(direction);
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
				case CrusaderEngine.Facing.NORTH -> direction.setText("< N >");
				case CrusaderEngine.Facing.SOUTH -> direction.setText("< S >");
				case CrusaderEngine.Facing.EAST -> direction.setText("< E >");
				case CrusaderEngine.Facing.WEST -> direction.setText("< W >");
				case CrusaderEngine.Facing.NORTH_EAST -> direction.setText("< NE >");
				case CrusaderEngine.Facing.NORTH_WEST -> direction.setText("< NW >");
				case CrusaderEngine.Facing.SOUTH_EAST -> direction.setText("< SE >");
				case CrusaderEngine.Facing.SOUTH_WEST -> direction.setText("< SW >");
				default -> throw new MazeException("Invalid facing :" + facing);
			}
		}
		
		super.draw(g);		
	}
}

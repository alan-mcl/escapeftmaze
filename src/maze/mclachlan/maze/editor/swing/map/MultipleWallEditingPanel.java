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

package mclachlan.maze.editor.swing.map;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class MultipleWallEditingPanel extends JPanel
{
	private final WallDetailsPanel details;

	/*-------------------------------------------------------------------------*/
	public MultipleWallEditingPanel(Zone zone)
	{
		setLayout(new BorderLayout());
		add(new JLabel(" --- Edit Multiple Walls --- "), BorderLayout.NORTH);
		details = new WallDetailsPanel(true, zone);
		add(details, BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void setWalls(List<Object> selected)
	{
		List<Wall> walls = new ArrayList<>();
		for (Object obj : selected)
		{
			walls.add((Wall)obj);
		}
		
		details.refresh(new MultipleWallProxy(walls), -1, false);
	}
}

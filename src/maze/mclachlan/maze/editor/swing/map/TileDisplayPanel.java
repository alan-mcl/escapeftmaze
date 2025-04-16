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
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;
import mclachlan.crusader.Tile;
import java.awt.*;

/**
 *
 */
public class TileDisplayPanel extends JPanel
{
	private Zone zone;
	private TileDetailsPanel details;

	/*-------------------------------------------------------------------------*/
	public TileDisplayPanel(Zone zone, MapEditor editor)
	{
		this.zone = zone;
		setLayout(new BorderLayout());
		add(new JLabel(" --- Tile Details --- "), BorderLayout.NORTH);
		details = new TileDetailsPanel(zone, editor, false);
		add(details, BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Tile tile)
	{
		mclachlan.maze.map.Tile mazeTile = null;
		int index = -1, x=-1, y=-1;
		
		for (int i=0; i<zone.getMap().getTiles().length; i++)
		{
			if (tile == zone.getMap().getTiles()[i])
			{
				index = i;
				x = i % zone.getWidth();
				y = i / zone.getWidth();
				mazeTile = zone.getTiles()[x][y];
				break;
			}
		}
		
		if (mazeTile == null)
		{
			throw new MazeException("Cannot match crusader tile "+tile+" to a maze tile");
		}
		
		details.refresh(new SingleTileProxy(tile, mazeTile), index, x, y);
	}
}

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

import mclachlan.crusader.Tile;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class MultipleTileEditingPanel extends JPanel
{
	private MapEditor editor;
	private TileDetailsPanel details;

	/*-------------------------------------------------------------------------*/
	public MultipleTileEditingPanel(Zone zone, MapEditor editor)
	{
		this.editor = editor;
		setLayout(new BorderLayout());
		add(new JLabel(" --- Edit Multiple Tiles --- "), BorderLayout.NORTH);
		details = new TileDetailsPanel(zone, editor, true);
		add(details, BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void setTiles(List<Object> selected)
	{
		List<mclachlan.crusader.Tile> crusaderTiles = new ArrayList<>();
		List<mclachlan.maze.map.Tile> mazeTiles = new ArrayList<>();
		
		for (Object obj : selected)
		{
			crusaderTiles.add((Tile)obj);
			mazeTiles.add(editor.getMazeTile((Tile)obj));
		}
		
		details.refresh(new MultipleTileProxy(crusaderTiles, mazeTiles), -1,-1,-1);
	}
}

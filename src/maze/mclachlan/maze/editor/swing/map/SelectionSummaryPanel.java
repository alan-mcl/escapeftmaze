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

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SelectionSummaryPanel extends JPanel implements ActionListener
{
	SelectionLayer layer;
	Zone zone;
	MapEditor editor;
	JLabel tileCount;
	JLabel horizWallCount;
	JLabel vertWallCount;
	
	JButton addPortal;

	/*-------------------------------------------------------------------------*/
	public SelectionSummaryPanel(MapDisplay display, MapEditor editor, Zone zone)
	{
		this.editor = editor;
		this.layer = display.selectionLayer;
		this.zone = zone;
		setLayout(new GridLayout(15,1));
		tileCount = new JLabel();
		horizWallCount = new JLabel();
		vertWallCount = new JLabel();
		
		add(tileCount);
		add(horizWallCount);
		add(vertWallCount);
		
		addPortal = new JButton("Add Portal");
		addPortal.addActionListener(this);
		add(addPortal);
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		int tiles, horizWalls, vertWalls;
		tiles = horizWalls = vertWalls = 0;
		
		for (Object obj : layer.selected)
		{
			if (obj instanceof Tile)
			{
				tiles++;
			}
			else if (obj instanceof Wall)
			{
				boolean found=false;
				for (Wall w : zone.getMap().getHorizontalWalls())
				{
					if (w == obj)
					{
						horizWalls++;
						found=true;
						break;
					}
				}
				if (!found)
				{
					vertWalls++;
				}
			}
		}
		
		tileCount.setText(tiles+" tiles");
		horizWallCount.setText(horizWalls+" horizontal walls");
		vertWallCount.setText(vertWalls+" vertical walls");
		
		addPortal.setEnabled(tiles == 2 && horizWalls == 0 && vertWalls == 0);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == addPortal)
		{
			if (layer.selected.size() != 2 || 
				!(layer.selected.get(0) instanceof Tile) ||
				!(layer.selected.get(1) instanceof Tile))
			{
				return;
			}
			
			Tile fromTile = (Tile)layer.selected.get(0);
			Tile toTile = (Tile)layer.selected.get(1);
			
			Point from = getCoords(fromTile);
			Point to = getCoords(toTile);

			String mazeScript = "generic door creak";

			try
			{
				// hackish attempt to set a default door script. because i keep
				// forgetting this
				Database.getInstance().getMazeScript(mazeScript);
			}
			catch (Exception x)
			{
				mazeScript = null;
			}

			zone.addPortal(new Portal(
				"",
				Portal.State.UNLOCKED,
				from,
				CrusaderEngine.Facing.NORTH,
				to,
				CrusaderEngine.Facing.NORTH,
				true,
				true,
				true,
				true,
				5,
				0,
				new int[8],
				new BitSet(),
				null, 
				true,
				mazeScript,
				null));
			
			editor.refreshSelectionSummary();
		}
	}

	/*-------------------------------------------------------------------------*/
	private Point getCoords(Tile tile)
	{
		for (int i=0; i<zone.getMap().getTiles().length; i++)
		{
			if (zone.getMap().getTiles()[i] == tile)
			{
				return new Point(i%zone.getWidth(), i/zone.getWidth());
			}
		}
		
		throw new MazeException("Could not find tile "+tile);
	}
}

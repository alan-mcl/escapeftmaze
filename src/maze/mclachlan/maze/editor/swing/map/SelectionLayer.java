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

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SelectionLayer extends Layer
{
	List<Object> selected = new ArrayList<>();
	Rectangle activeSelection;

	private MapDisplay display;
	Map map;
	
	/*-------------------------------------------------------------------------*/
	public SelectionLayer(MapDisplay display, Map map)
	{
		this.display = display;
		this.map = map;
	}

	/*-------------------------------------------------------------------------*/
	public void setActiveSelection(Rectangle activeSelection)
	{
		this.activeSelection = activeSelection;
	}

	/*-------------------------------------------------------------------------*/
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g.create();
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
		g2d.setColor(Color.BLUE);

		if (activeSelection != null)
		{
			g2d.fillRect(activeSelection.x, activeSelection.y, activeSelection.width, activeSelection.height);
		}
		
		for (int i=0; i<map.getTiles().length; i++)
		{
			if (selected.contains(map.getTiles()[i]))
			{
				Rectangle bounds = display.getTileBounds(i);
				bounds.grow(2,2);
				g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
		
		for (int i=0; i<map.getHorizontalWalls().length; i++)
		{
			if (selected.contains(map.getHorizontalWalls()[i]))
			{
				Rectangle bounds = display.getHorizontalWallBounds(i);
				bounds.grow(2,2);
				g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
		
		for (int i=0; i<map.getVerticalWalls().length; i++)
		{
			if (selected.contains(map.getVerticalWalls()[i]))
			{
				Rectangle bounds = display.getVerticalWallBounds(i);
				bounds.grow(2,2);
				g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}

		for (EngineObject obj : map.getExpandedObjects())
		{
			if (selected.contains(obj))
			{
				Rectangle bounds = display.getObjectBounds(obj.getXPos(), obj.getYPos());
				bounds.grow(2,2);
				g2d.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
	}
}

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

import java.awt.*;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class PortalLayer extends Layer
{
	Zone zone;
	MapDisplay display;

	/*-------------------------------------------------------------------------*/
	public PortalLayer(Zone zone, MapDisplay display)
	{
		this.zone = zone;
		this.display = display;
	}
	
	/*-------------------------------------------------------------------------*/
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g.create();

		Portal[] portals = zone.getPortals();

		if (display.displayFeatures.get(MapDisplay.Display.PORTALS))
		{
			for (Portal portal : portals)
			{
				int fromIndex = calcIndex(portal.getFrom());
				int toIndex = calcIndex(portal.getTo());

				Rectangle fromBounds = display.getTileBounds(fromIndex);
				Rectangle toBounds = display.getTileBounds(toIndex);
				int tileSize = display.tileSize*display.zoomLevel;
				int offset = tileSize/4;
				int diameter = tileSize/2;
				
				g2d.setColor(Color.WHITE);
				g2d.drawLine(fromBounds.x+tileSize/2+1, fromBounds.y+tileSize/2+1, toBounds.x+tileSize/2+1, toBounds.y+tileSize/2+1);
				g2d.setColor(Color.BLACK);
				g2d.drawLine(fromBounds.x+tileSize/2, fromBounds.y+tileSize/2, toBounds.x+tileSize/2, toBounds.y+tileSize/2);

				g2d.setColor(Color.WHITE);
				g2d.drawOval(fromBounds.x+offset+1, fromBounds.y+offset+1, diameter, diameter);
				g2d.setColor(Color.BLACK);
				g2d.drawOval(fromBounds.x+offset, fromBounds.y+offset, diameter, diameter);
				
				if (portal.isTwoWay())
				{
					g2d.setColor(Color.WHITE);
					g2d.drawOval(toBounds.x+ offset +1, toBounds.y+ offset +1, diameter, diameter);
					g2d.setColor(Color.BLACK);
					g2d.drawOval(toBounds.x+ offset, toBounds.y+ offset, diameter, diameter);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private int calcIndex(Point p)
	{
		int width = zone.getWidth();
		return p.y*width + p.x%width;
	}
}

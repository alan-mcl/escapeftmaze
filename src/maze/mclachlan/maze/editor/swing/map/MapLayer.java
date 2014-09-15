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

import java.util.List;
import java.awt.*;
import mclachlan.maze.map.Zone;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.crusader.EngineObject;

/**
 *
 */
public class MapLayer extends Layer
{
	Zone zone;
	MapDisplay display;

	/*-------------------------------------------------------------------------*/
	public MapLayer(Zone zone, MapDisplay display)
	{
		this.zone = zone;
		this.display = display;
	}
	
	/*-------------------------------------------------------------------------*/
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setColor(Color.DARK_GRAY);
		
		int width = zone.getWidth();
		
		int tileSize = display.tileSize*display.zoomLevel;
		int wallSize = display.wallSize+display.zoomLevel;

		Map map = zone.getMap();
		
		if (display.displayFeatures.get(MapDisplay.Display.TILES))
		{
			Tile[] tiles = map.getTiles();
			for (int i = 0; i < tiles.length; i++)
			{
				int column = i%width;
				int row=i/width;
				int x1 = (wallSize*(column+1))+(column*tileSize);
				int y1 = (wallSize*(row+1))+(row*tileSize);
			
				g2d.drawImage(display.getFloorScaledImage(tiles[i].getFloorTexture().getImages()[0]), x1, y1, display);
				if (tiles[i].getFloorMaskTexture() != null)
				{
					g2d.drawImage(display.getFloorScaledImage(tiles[i].getFloorMaskTexture().getImages()[0]), x1, y1, display);
				}
			}
		}

		if (display.displayFeatures.get(MapDisplay.Display.HORIZ_WALLS))
		{
			Wall[] horizontalWalls = map.getHorizontalWalls();
			for (int i = 0; i < horizontalWalls.length; i++)
			{
				if (horizontalWalls[i].isVisible())
				{
					int column = i%width;
					int row = i/width;
					int x1 = (wallSize*(column+1))+(column*tileSize);
					int y1 = wallSize*row+tileSize*row;
	
					if (horizontalWalls[i].isVisible())
					{
						g2d.drawImage(display.getHorizScaledImage(horizontalWalls[i].getTexture().getImages()[0]), x1, y1, display);
						if (display.displayFeatures.get(MapDisplay.Display.GRID))
						{
							g2d.drawRect(x1,y1,tileSize,wallSize);
						}
					}
				}
			}
		}
		
		if (display.displayFeatures.get(MapDisplay.Display.VERT_WALLS))
		{
			Wall[] verticalWalls = map.getVerticalWalls();
			for (int i = 0; i < verticalWalls.length; i++)
			{
				if (verticalWalls[i].isVisible())
				{
					int column = i%(width+1);
					int row = i/(width+1);
					int x1 = column*(wallSize+tileSize);
					int y1 = (wallSize*(row+1))+(row*tileSize);
				
					if (verticalWalls[i].isVisible())
					{
						g2d.drawImage(display.getVertScaledImage(verticalWalls[i].getTexture().getImages()[0]), x1, y1, display);
						if (display.displayFeatures.get(MapDisplay.Display.GRID))
						{
							g2d.drawRect(x1,y1,wallSize,tileSize);
						}
					}
				}
			}
		}
		
		if (display.displayFeatures.get(MapDisplay.Display.OBJECTS))
		{
			List<EngineObject> objects = map.getOriginalObjects();
			for (EngineObject object : objects)
			{
				Rectangle r = display.getTileBounds(object.getTileIndex());
				g2d.setColor(Color.GREEN);
				g2d.fillOval(r.x+2, r.y+2, r.width-4, r.height-4);
				g2d.setColor(Color.BLACK);
				g2d.drawOval(r.x+2, r.y+2, r.width-4, r.height-4);
			}
		}
	}
}

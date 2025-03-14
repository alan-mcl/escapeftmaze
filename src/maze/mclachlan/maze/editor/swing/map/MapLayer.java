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
import java.util.*;
import java.util.List;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

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
		
		if (display.displayFeatures.get(MapDisplay.Display.TILES) || display.displayFeatures.get(MapDisplay.Display.TILE_MASK_TEXTURES))
		{
			Tile[] tiles = map.getTiles();
			for (int i = 0; i < tiles.length; i++)
			{
				int column = i%width;
				int row=i/width;
				int x1 = (wallSize*(column+1))+(column*tileSize);
				int y1 = (wallSize*(row+1))+(row*tileSize);

				if (display.displayFeatures.get(MapDisplay.Display.TILES))
				{
					g2d.drawImage(display.getFloorScaledImage(tiles[i].getFloorTexture().getImages()[0], true), x1, y1, display);
				}
				if (display.displayFeatures.get(MapDisplay.Display.TILE_MASK_TEXTURES))
				{
					if (tiles[i].getFloorMaskTexture() != null)
					{
						g2d.drawImage(display.getFloorScaledImage(tiles[i].getFloorMaskTexture().getImages()[0], false), x1, y1, display);
					}
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
						g2d.drawImage(display.getHorizScaledImage(horizontalWalls[i].getTexture(0).getImages()[0]), x1, y1, display);
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
						g2d.drawImage(display.getVertScaledImage(verticalWalls[i].getTexture(0).getImages()[0]), x1, y1, display);
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

				BitSet placementMask = object.getPlacementMask();

				if (placementMask == null)
				{
					placementMask = new BitSet();
					placementMask.set(EngineObject.Placement.CENTER);
				}
				for (int j=0; j<=9; j++)
				{
					if (placementMask.get(j))
					{
						Point p = getCoords(r, j);

						int radius = r.width / 5;
						drawObject(g2d, p.x, p.y, radius, radius);
					}
				}
			}
		}
	}

	private void drawObject(Graphics2D g2d, int x, int y, int w, int h)
	{
		g2d.setColor(Color.GREEN);
		g2d.fillOval(x, y, w, h);
		g2d.setColor(Color.BLACK);
		g2d.drawOval(x, y, w, h);
	}

	Point getCoords(Rectangle r, int placement)
	{
		int radius = r.width/5;

		int midX = r.x +r.width/2 -radius/2;
		int midY = r.y +r.height/2 -radius/2;

		int xOffset;
		int yOffset;

		switch (placement)
		{
			case EngineObject.Placement.CENTER ->
			{
				xOffset = 0;
				yOffset = 0;
			}
			case EngineObject.Placement.NORTH_WEST ->
			{
				xOffset = -radius*2;
				yOffset = -radius*2;
			}
			case EngineObject.Placement.NORTH ->
			{
				xOffset = 0;
				yOffset = -radius*2;
			}
			case EngineObject.Placement.NORTH_EAST ->
			{
				xOffset = radius*2;
				yOffset = -radius*2;
			}
			case EngineObject.Placement.WEST ->
			{
				xOffset = -radius*2;
				yOffset = 0;
			}
			case EngineObject.Placement.EAST ->
			{
				xOffset = radius*2;
				yOffset = 0;
			}
			case EngineObject.Placement.SOUTH_WEST ->
			{
				xOffset = -radius*2;
				yOffset = radius*2;
			}
			case EngineObject.Placement.SOUTH ->
			{
				xOffset = 0;
				yOffset = radius*2;
			}
			case EngineObject.Placement.SOUTH_EAST ->
			{
				xOffset = radius*2;
				yOffset = radius*2;
			}
			default ->
				throw new MazeException("Invalid placement index: " + placement);
		}

		return new Point(midX+xOffset, midY+yOffset);
	}
}

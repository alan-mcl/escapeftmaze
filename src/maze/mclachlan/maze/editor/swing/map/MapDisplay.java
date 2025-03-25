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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class MapDisplay extends JPanel implements Scrollable
{
	Zone zone;
	int zoomLevel = 2;
	int tileSize = 10;
	int wallSize = 0;
	
	/** what features to display */
	BitSet displayFeatures;
	/** what features to select */
	BitSet selectionFeatures;
	
	/** layers */
	List<Layer> layers = new ArrayList<>();
	SelectionLayer selectionLayer;
	
	/** a cache of scaled images */
	private final java.util.Map<Image, Image> floorScaledImages = new HashMap<>();
	private final java.util.Map<Image, Image> horizScaledImages = new HashMap<>();
	private final java.util.Map<Image, Image> vertScaledImages = new HashMap<>();
	
	private static final int MAX_ZOOM_LEVEL = 4;

	/*-------------------------------------------------------------------------*/
	public MapDisplay(Zone zone)
	{
		this.zone = zone;
		this.setBackground(Color.LIGHT_GRAY);
		this.setSize(800, 800);
		displayFeatures = new BitSet(128);
		displayFeatures.set(0, 128);
		selectionFeatures = new BitSet(128);
		selectionFeatures.set(0, 128);
		
		selectionLayer = new SelectionLayer(this, zone.getMap());
		ScriptLayer scriptLayer = new ScriptLayer(this, zone);

		layers.add(new MapLayer(zone, this));
		layers.add(scriptLayer);
		layers.add(new PortalLayer(zone, this));
		layers.add(selectionLayer);
	}
	
	/*-------------------------------------------------------------------------*/
	public void incrementZoomLevel(int i)
	{
		if (zoomLevel+i >= 1 && zoomLevel+i <= MAX_ZOOM_LEVEL)
		{
			zoomLevel += i;
			rescaleImages();
			repaint();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void paintComponent(Graphics g)
	{
		g.setColor(getBackground());
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		
		for (Layer l : layers)
		{
			l.paint(g);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void rescaleImages()
	{
		floorScaledImages.replaceAll((i, v) -> i.getScaledInstance(
			tileSize * zoomLevel, tileSize * zoomLevel, Image.SCALE_FAST));

		horizScaledImages.replaceAll((i, v) -> i.getScaledInstance(
			tileSize * zoomLevel, wallSize + zoomLevel, Image.SCALE_FAST));

		vertScaledImages.replaceAll((i, v) -> i.getScaledInstance(
			wallSize + zoomLevel, tileSize * zoomLevel, Image.SCALE_FAST));
	}
	
	/*-------------------------------------------------------------------------*/
	Image getFloorScaledImage(BufferedImage image, boolean sample)
	{
		Image result = floorScaledImages.get(image);
		
		if (result == null)
		{
			Image temp;

			if (sample)
			{
				int w = 16;
				// take a sample
				temp = image.getSubimage(
					image.getWidth() / 2 - w / 2,
					image.getHeight() / 2 - w / 2,
					w,
					w);
			}
			else
			{
				temp = image;
			}

			Image scaledInstance = temp.getScaledInstance(tileSize * zoomLevel, tileSize * zoomLevel, Image.SCALE_FAST);

			floorScaledImages.put(image, scaledInstance);
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	Image getHorizScaledImage(Image image)
	{
		Image result = horizScaledImages.get(image);
		
		if (result == null)
		{
			horizScaledImages.put(image, image.getScaledInstance(
				tileSize*zoomLevel, wallSize+zoomLevel, Image.SCALE_FAST));
		}
		
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	Image getVertScaledImage(Image image)
	{
		Image result = vertScaledImages.get(image);
		
		if (result == null)
		{
			vertScaledImages.put(image, image.getScaledInstance(
				wallSize+zoomLevel, tileSize*zoomLevel, Image.SCALE_FAST));
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setDisplayFeature(int feature, boolean value)
	{
		displayFeatures.set(feature, value);
		repaint();
	}
	
	/*-------------------------------------------------------------------------*/
	public void setSelectionFeature(int feature, boolean value)
	{
		selectionFeatures.set(feature, value);
	}

	/*-------------------------------------------------------------------------*/
	public Object selectObjectAt(MouseEvent e)
	{
		// proceed with a brute force approach.  nasty but i'm lazy.

		Point point = e.getPoint();
		Map map = zone.getMap();
		
		if (selectionFeatures.get(Selection.HORIZ_WALLS))
		{
			Wall[] horizontalWalls = map.getHorizontalWalls();
			for (int i = 0; i < horizontalWalls.length; i++)
			{
				Rectangle r = getHorizontalWallBounds(i);
				if (r.contains(point))
				{
					selectionLayer.selected.add(horizontalWalls[i]);
					repaint();
					return horizontalWalls[i];
				}
			}
		}
		
		if (selectionFeatures.get(Selection.VERT_WALLS))
		{
			Wall[] verticalWalls = map.getVerticalWalls();
			for (int i = 0; i < verticalWalls.length; i++)
			{
				Rectangle r = getVerticalWallBounds(i);
				if (r.contains(point))
				{
					selectionLayer.selected.add(verticalWalls[i]);
					repaint();
					return verticalWalls[i];
				}
			}
		}
		
		if (selectionFeatures.get(Selection.TILES))
		{
			Tile[] tiles = map.getTiles();
			for (int i = 0; i < tiles.length; i++)
			{
				Rectangle r = getTileBounds(i);
				if (r.contains(point))
				{
					selectionLayer.selected.add(tiles[i]);
					repaint();
					return tiles[i];
				}
			}
		}
		
		if (!((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ||
			(e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK))
		{
			// click on nothing
			selectionLayer.selected.clear();
		}
		repaint();
		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public void recalculateSelectedObjects()
	{
		selectionLayer.selected.clear();
		// proceed with a brute force approach.  nasty but i'm lazy. and I cut and paste

		Map map = zone.getMap();
		
		if (selectionFeatures.get(Selection.HORIZ_WALLS))
		{
			Wall[] horizontalWalls = map.getHorizontalWalls();
			for (int i = 0; i < horizontalWalls.length; i++)
			{
				Rectangle r = getHorizontalWallBounds(i);
				if (selectionLayer.activeSelection.contains(r))
				{
					selectionLayer.selected.add(horizontalWalls[i]);
				}
			}
		}
		
		if (selectionFeatures.get(Selection.VERT_WALLS))
		{
			Wall[] verticalWalls = map.getVerticalWalls();
			for (int i = 0; i < verticalWalls.length; i++)
			{
				Rectangle r = getVerticalWallBounds(i);
				if (selectionLayer.activeSelection.contains(r))
				{
					selectionLayer.selected.add(verticalWalls[i]);
				}
			}
		}

		if (selectionFeatures.get(Selection.TILES))
		{
			Tile[] tiles = map.getTiles();
			for (int i = 0; i < tiles.length; i++)
			{
				Rectangle r = getTileBounds(i);
				if (selectionLayer.activeSelection.contains(r))
				{
					selectionLayer.selected.add(tiles[i]);
				}
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	Rectangle getTileBounds(int i)
	{
		int width = zone.getWidth();
		int tileSize = this.tileSize*this.zoomLevel;
		int wallSize = this.wallSize+this.zoomLevel;
		int column = i%width;
		int row=i/width;
		int x1 = (wallSize*(column+1))+(column*tileSize);
		int y1 = (wallSize*(row+1))+(row*tileSize);
//		int x2 = x1+tileSize;
//		int y2 = y1+tileSize;
		return new Rectangle(x1-1,y1-1,tileSize+2, tileSize+2);
	}

	/*-------------------------------------------------------------------------*/
	Rectangle getVerticalWallBounds(int i)
	{
		int width = zone.getWidth();
		int tileSize = this.tileSize*this.zoomLevel;
		int wallSize = this.wallSize+this.zoomLevel;
		int column = i%(width+1);
		int row = i/(width+1);
		int x1 = column*(wallSize+tileSize);
//		int x2 = x1+wallSize;
		int y1 = (wallSize*(row+1))+(row*tileSize);
//		int y2 = y1+tileSize;
		return new Rectangle(x1-1,y1-1,wallSize+2, tileSize+2);
	}

	/*-------------------------------------------------------------------------*/
	Rectangle getHorizontalWallBounds(int i)
	{
		int width = zone.getWidth();
		int tileSize = this.tileSize*this.zoomLevel;
		int wallSize = this.wallSize+this.zoomLevel;
		int column = i%width;
		int row = i/width;
		int x1 = (wallSize*(column+1))+(column*tileSize);
//		int x2 = wallSize+((column+1)*tileSize);
		int y1 = wallSize*row+tileSize*row;
//		int y2 = y1+wallSize;
		return new Rectangle(x1-1,y1-1,tileSize+2, wallSize+2);
	}

	/*-------------------------------------------------------------------------*/
	public void clearActiveSelection()
	{
		selectionLayer.activeSelection = null;
		repaint();
	}
	
	/*-------------------------------------------------------------------------*/
	public void clearSelection()
	{
		this.selectionLayer.selected.clear();
		repaint();
	}

	/*-------------------------------------------------------------------------*/
	public void setActiveSelection(Rectangle r)
	{
		selectionLayer.activeSelection = r;
		recalculateSelectedObjects();
		repaint();
	}
	
	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		return new Dimension(
			zone.getWidth()*tileSize*MAX_ZOOM_LEVEL, 
			zone.getLength()*tileSize*MAX_ZOOM_LEVEL);
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredScrollableViewportSize()
	{
		return new Dimension(
			zone.getWidth()*tileSize*zoomLevel, 
			zone.getLength()*tileSize*zoomLevel);
	}

	/*-------------------------------------------------------------------------*/
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return tileSize*zoomLevel;
	}

	/*-------------------------------------------------------------------------*/
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return tileSize*zoomLevel;
	}

	/*-------------------------------------------------------------------------*/
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public static class Display
	{
		public static final int TILES = 0;
		public static final int HORIZ_WALLS = 1;
		public static final int VERT_WALLS = 2;
		public static final int GRID = 3;
		public static final int CHESTS = 4;
		public static final int CAST_SPELL_SCRIPTS = 5;
		public static final int ENCOUNTERS = 6;
		public static final int FLAVOUR_TEXT_SCRIPTS = 7;
		public static final int LOOT_SCRIPTS = 8;
		public static final int REMOVE_WALL_SCRIPTS = 9;
		public static final int CUSTOM_SCRIPTS = 10;
		public static final int SCRIPTS_ON_WALLS = 11;
		public static final int OBJECTS = 12;
		public static final int PORTALS = 13;
		public static final int EXECUTE_MAZE_SCRIPT = 14;
		public static final int LEVERS = 15;
		public static final int TILE_MASK_TEXTURES = 16;
		public static final int LIGHT_LEVELS = 17;
	}
	
	/*-------------------------------------------------------------------------*/
	public static class Selection
	{
		public static final int TILES = 0;
		public static final int HORIZ_WALLS = 1;
		public static final int VERT_WALLS = 2;
	}
}

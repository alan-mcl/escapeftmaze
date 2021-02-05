/*
 * Copyright (c) 2012 Alan McLachlan
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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import mclachlan.crusader.*;
import mclachlan.crusader.Map;
import mclachlan.diygui.DIYPanel;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.PlayerTilesVisited;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MapDisplayWidget extends DIYPanel
{
	// for debugging
	private boolean filterByVisited = true;

	//	a cache of scaled images
	private java.util.Map<Image, Image> floorScaledImages = new HashMap<Image, Image>();
	private java.util.Map<Image, Image> horizScaledImages = new HashMap<Image, Image>();
	private java.util.Map<Image, Image> vertScaledImages = new HashMap<Image, Image>();
	private static final int TILE_SIZE = 10;
	private static final int WALL_SIZE = 1;

	// zoom level starts at 2
	private int zoomLevel = 2;

	private final static BasicStroke DASHED = new BasicStroke(
		1.0f,
		BasicStroke.CAP_BUTT,
		BasicStroke.JOIN_MITER,
		10.0f,
		new float[]{2.0f},
		0.0f);

	private static final int MAX_ZOOM_LEVEL = 3;

	// pre-render the image to avoid recalculating every time
	private Image image;

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		if (image == null)
		{
			image = createImage(g);
		}

		g.drawImage(image, x, y, DiyGuiUserInterface.instance);
	}

	/*-------------------------------------------------------------------------*/
	public Image createImage(Graphics2D g2d)
	{
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g = result.createGraphics();

		// set to 0 instead of this.x & this.y because we are drawing
		// on a temp image buffer.
		int originX = 0;
		int originY = 0;

		g.setColor(Color.BLACK);
		g.fillRect(originX, originY, width, height);

		g.setColor(Color.DARK_GRAY);

		Zone zone = Maze.getInstance().getCurrentZone();

		int tileSize = TILE_SIZE * zoomLevel;
		int wallSize = WALL_SIZE + zoomLevel;

		Map map = zone.getMap();
		Component component = Maze.getInstance().getComponent();
		PlayerTilesVisited visited = Maze.getInstance().getPlayerTilesVisited();

		Point playerPos = Maze.getInstance().getPlayerPos();
		int facing = Maze.getInstance().getFacing();

		List<Integer> tilesToDisplay = new ArrayList<Integer>();

		int tilesToDisplayHoriz = this.width / (tileSize/* + wallSize*/);
		int tilesToDisplayVert = this.height / (tileSize/* + wallSize*/);

		int startX = playerPos.x - tilesToDisplayHoriz / 2;
		int startY = playerPos.y - tilesToDisplayVert / 2;

		for (int xx = startX; xx < startX + tilesToDisplayHoriz; xx++)
		{
			for (int yy = startY; yy < startY + tilesToDisplayVert; yy++)
			{
				if (xx < 0 || yy < 0 || xx >= map.getWidth() || yy >= map.getLength())
				{
					// off the map, pass
					continue;
				}

				tilesToDisplay.add(map.getIndex(new Point(xx, yy)));
			}
		}

		Tile[] tiles = map.getTiles();
		Wall[] horizontalWalls = map.getHorizontalWalls();
		Wall[] verticalWalls = map.getVerticalWalls();
		for (int index : tilesToDisplay)
		{
			Point p = map.getPoint(index);

			if (!visited.hasVisited(zone.getName(), p) && filterByVisited)
			{
				continue;
			}

			int column = p.x - startX;
			int row = p.y - startY;
			int x1 = originX + tileSize * column;
			int y1 = originY + tileSize * row;

			// tile
			g.drawImage(
				getFloorScaledImage(tiles[index].getFloorTexture().getImages()[0]),
				x1, y1, component);

			if (tiles[index].getFloorMaskTexture() != null)
			{
				g.drawImage(
					getFloorScaledImage(tiles[index].getFloorMaskTexture().getImages()[0]),
					x1, y1, component);
			}

			// objects
//			EngineObject object = map.getObject(index);
//			if (object != null)
//			{
//				g.drawImage(
//					getFloorScaledImage(object.getTextures()[0].getImages()[0]),
//					x1, y1, component);
//			}

			// grid
			g.setColor(Color.DARK_GRAY);
			g.setStroke(DASHED);
			g.drawRect(x1, y1, tileSize, tileSize);
			g.setStroke(new BasicStroke());

			// horiz walls
			Set<Integer> horizWallsDrawn = new HashSet<Integer>();
			int northWall = map.getNorthWall(index);
			Wall wall = horizontalWalls[northWall];
			if (wall.isVisible() && !horizWallsDrawn.contains(northWall))
			{
				int xw = originX + tileSize * column;
				int yw = originY + tileSize * row - wallSize / 2;

				g.drawImage(
					getHorizScaledImage(wall.getTexture().getImages()[0]),
					xw, yw, component);

				if (wall.getMaskTexture() != null)
				{
					g.drawImage(
						getHorizScaledImage(wall.getMaskTexture().getImages()[0]),
						xw, yw, component);
				}

				horizWallsDrawn.add(northWall);
			}

			int southWall = map.getSouthWall(index);
			wall = horizontalWalls[southWall];
			if (wall.isVisible() && !horizWallsDrawn.contains(southWall))
			{
				int xw = originX + tileSize * column;
				int yw = originY + tileSize * (row + 1) - wallSize / 2;

				g.drawImage(
					getHorizScaledImage(wall.getTexture().getImages()[0]),
					xw, yw, component);

				if (wall.getMaskTexture() != null)
				{
					g.drawImage(
						getHorizScaledImage(wall.getMaskTexture().getImages()[0]),
						xw, yw, component);
				}

				horizWallsDrawn.add(southWall);
			}

			// vert walls
			Set<Integer> vertWallsDrawn = new HashSet<Integer>();
			int westWall = map.getWestWall(index);
			wall = verticalWalls[westWall];
			if (wall.isVisible() && !vertWallsDrawn.contains(westWall))
			{
				int xw = originX + tileSize * column - wallSize / 2;
				int yw = originY + tileSize * row;

				g.drawImage(
					getVertScaledImage(wall.getTexture().getImages()[0]),
					xw, yw, component);

				if (wall.getMaskTexture() != null)
				{
					g.drawImage(
						getVertScaledImage(wall.getMaskTexture().getImages()[0]),
						xw, yw, component);
				}

				vertWallsDrawn.add(westWall);
			}

			int eastWall = map.getEastWall(index);
			wall = verticalWalls[eastWall];
			if (wall.isVisible() && !vertWallsDrawn.contains(eastWall))
			{
				int xw = originX + tileSize * (column + 1) - wallSize / 2;
				int yw = originY + tileSize * row;

				g.drawImage(
					getVertScaledImage(wall.getTexture().getImages()[0]),
					xw, yw, component);

				if (wall.getMaskTexture() != null)
				{
					g.drawImage(
						getVertScaledImage(wall.getMaskTexture().getImages()[0]),
						xw, yw, component);
				}

				vertWallsDrawn.add(eastWall);
			}

			// player pos
			if (playerPos.equals(p))
			{
				drawPlayerPos(g, tileSize, facing, column, row, originX, originY);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void drawPlayerPos(Graphics2D g, int tileSize, int facing,
		int column, int row, int originX, int originY)
	{
		int inset = tileSize/5;

		int x1 = originX + tileSize * column +inset;
		int y1 = originY + tileSize * row +inset;

		int width1 = tileSize -inset*2;
		int height1 = tileSize -inset*2;

		int[] xs, ys;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH:
				xs = new int[]{x1, x1 + width1 / 2, x1 + width1};
				ys = new int[]{y1 + height1, y1, y1 + height1};
				break;
			case CrusaderEngine.Facing.SOUTH:
				xs = new int[]{x1, x1 + width1 / 2, x1 + width1};
				ys = new int[]{y1, y1 + height1, y1};
				break;
			case CrusaderEngine.Facing.EAST:
				xs = new int[]{x1, x1 + width1, x1};
				ys = new int[]{y1, y1 + height1 / 2, y1 + height1};
				break;
			case CrusaderEngine.Facing.WEST:
				xs = new int[]{x1 + width1, x1, x1 + width1};
				ys = new int[]{y1, y1 + height1 / 2, y1 + height1};
				break;
			case CrusaderEngine.Facing.NORTH_EAST:
				xs = new int[]{x1, x1+width1, x1+width1/2};
				ys = new int[]{y1+height1/2,y1,y1+height1};
				break;
			case CrusaderEngine.Facing.NORTH_WEST:
				xs = new int[]{x1, x1+width1/2, x1+width1};
				ys = new int[]{y1, y1+height1, y1+height1/2};
				break;
			case CrusaderEngine.Facing.SOUTH_EAST:
				xs = new int[]{x1, x1+width1, x1+width1/2};
				ys = new int[]{y1+height1/2, y1+height1, y1};
				break;
			case CrusaderEngine.Facing.SOUTH_WEST:
				xs = new int[]{x1+width1/2, x1, x1+width1};
				ys = new int[]{y1, y1+height1, y1+height1/2};
				break;
			default:
				throw new MazeException("invalid facing " + facing);
		}

		// golden triangle
		Polygon poly = new Polygon(xs, ys, 3);
		g.setColor(Constants.Colour.GOLD);
		g.fill(poly);
		g.setColor(Color.DARK_GRAY);
		g.draw(poly);
	}

	/*-------------------------------------------------------------------------*/
	private Image getFloorScaledImage(Image image)
	{
		Image result = floorScaledImages.get(image);

		if (result == null)
		{
			floorScaledImages.put(image, scaleImageFloor(image));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Image scaleImageFloor(Image image)
	{
		return image.getScaledInstance(
			TILE_SIZE * zoomLevel, TILE_SIZE * zoomLevel, Image.SCALE_SMOOTH);
	}

	/*-------------------------------------------------------------------------*/
	private Image getHorizScaledImage(Image image)
	{
		Image result = horizScaledImages.get(image);

		if (result == null)
		{
			horizScaledImages.put(image, scaleImageHoriz(image));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Image scaleImageHoriz(Image image)
	{
		return image.getScaledInstance(
			TILE_SIZE * zoomLevel, WALL_SIZE + zoomLevel, Image.SCALE_SMOOTH);
	}

	/*-------------------------------------------------------------------------*/
	private Image getVertScaledImage(Image image)
	{
		Image result = vertScaledImages.get(image);

		if (result == null)
		{
			vertScaledImages.put(image, scaleImageVert(image));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Image scaleImageVert(Image image)
	{
		// here's a trick: rotate the vert scaled images 90deg so that the map
		// doesn't come out looking funny

		int width1 = image.getWidth(DiyGuiUserInterface.instance);
		int height1 = image.getHeight(DiyGuiUserInterface.instance);

		BufferedImage target = new BufferedImage(width1, height1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D tg = target.createGraphics();

		AffineTransform at = new AffineTransform();
		at.rotate(Math.PI/2, width1/2, height1/2);

		tg.drawImage(image, at, DiyGuiUserInterface.instance);

		return target.getScaledInstance(
			WALL_SIZE + zoomLevel, TILE_SIZE * zoomLevel, Image.SCALE_SMOOTH);
	}

	/*-------------------------------------------------------------------------*/
	public void zoomOut()
	{
		if (zoomLevel > 1)
		{
			zoomLevel--;
		}

		rescaleCache();

		image = null;
	}

	/*-------------------------------------------------------------------------*/
	public void zoomIn()
	{
		if (zoomLevel < MAX_ZOOM_LEVEL)
		{
			zoomLevel++;
		}

		rescaleCache();

		image = null;
	}

	/*-------------------------------------------------------------------------*/
	private void rescaleCache()
	{
		for (Image image : floorScaledImages.keySet())
		{
			floorScaledImages.put(image, scaleImageFloor(image));
		}

		for (Image image : vertScaledImages.keySet())
		{
			vertScaledImages.put(image, scaleImageVert(image));
		}

		for (Image image : horizScaledImages.keySet())
		{
			horizScaledImages.put(image, scaleImageHoriz(image));
		}
	}
}

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

import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import java.util.List;
import java.awt.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.*;

/**
 * Displays stuff that is configured as scripts.
 */
public class ScriptLayer extends Layer
{
	Zone zone;
	MapDisplay display;
	
	/*-------------------------------------------------------------------------*/
	public ScriptLayer(MapDisplay display, Zone zone)
	{
		this.display = display;
		this.zone = zone;
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
		
		mclachlan.maze.map.Tile[][] tiles = zone.getTiles();
		for (int i = 0; i < tiles.length; i++)
		{
			for (int j = 0; j < tiles[i].length; j++)
			{
				List<TileScript> scripts = tiles[i][j].getScripts();

				int x1 = (wallSize*(i+1))+(i*tileSize);
				int y1 = (wallSize*(j+1))+(j*tileSize);

				for (TileScript script : scripts)
				{
					if (script instanceof Chest)
					{
						if (display.displayFeatures.get(MapDisplay.Display.CHESTS))
						{
							Image chest = Database.getInstance().getImage("editor/chest");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof CastSpell)
					{
						if (display.displayFeatures.get(MapDisplay.Display.CAST_SPELL_SCRIPTS))
						{
							Image chest = Database.getInstance().getImage("editor/spell");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof Encounter)
					{
						if (display.displayFeatures.get(MapDisplay.Display.ENCOUNTERS))
						{
							Image chest = Database.getInstance().getImage("editor/encounter");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof FlavourText)
					{
						if (display.displayFeatures.get(MapDisplay.Display.FLAVOUR_TEXT_SCRIPTS))
						{
							Image chest = Database.getInstance().getImage("editor/flavourtext");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof Loot)
					{
						if (display.displayFeatures.get(MapDisplay.Display.LOOT_SCRIPTS))
						{
							Image chest = Database.getInstance().getImage("editor/loot");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof RemoveWall)
					{
						if (display.displayFeatures.get(MapDisplay.Display.REMOVE_WALL_SCRIPTS))
						{
							Image chest = Database.getInstance().getImage("editor/removewall");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else if (script instanceof ExecuteMazeScript)
					{
						if (display.displayFeatures.get(MapDisplay.Display.EXECUTE_MAZE_SCRIPT))
						{
							Image img = Database.getInstance().getImage("editor/executescript");
							g2d.drawImage(img, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
					else
					{
						if (display.displayFeatures.get(MapDisplay.Display.CUSTOM_SCRIPTS))
						{
							Image chest = Database.getInstance().getImage("editor/otherscript");
							g2d.drawImage(chest, x1+2, y1+2, tileSize-4, tileSize-4, display);
						}
					}
				}
			}
		}

		if (display.displayFeatures.get(MapDisplay.Display.SCRIPTS_ON_WALLS))
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
					
					if (horizontalWalls[i].getMouseClickScript() != null)
					{
						g2d.setColor(Color.YELLOW);
						g2d.fillOval(x1+tileSize/4,y1-wallSize*2,tileSize/2,wallSize*5);
						g2d.setColor(Color.BLACK);
						g2d.drawOval(x1+tileSize/4,y1-wallSize*2,tileSize/2,wallSize*5);
					}
					if (horizontalWalls[i].getMaskTextureMouseClickScript() != null)
					{
						g2d.setColor(Color.RED);
						g2d.fillOval(x1+tileSize/3,y1-wallSize,tileSize/3,wallSize*3);
						g2d.setColor(Color.BLACK);
						g2d.drawOval(x1+tileSize/3,y1-wallSize,tileSize/3,wallSize*3);
					}
				}
			}
			
			Wall[] verticalWalls = map.getVerticalWalls();
			for (int i = 0; i < verticalWalls.length; i++)
			{
				if (verticalWalls[i].isVisible())
				{
					int column = i%(width+1);
					int row = i/(width+1);
					int x1 = column*(wallSize+tileSize);
					int y1 = (wallSize*(row+1))+(row*tileSize);
					
					if (verticalWalls[i].getMouseClickScript() != null)
					{
						g2d.setColor(Color.YELLOW);
						g2d.fillOval(x1-wallSize*2,y1+tileSize/4,wallSize*5,tileSize/2);
						g2d.setColor(Color.BLACK);
						g2d.drawOval(x1-wallSize*2,y1+tileSize/4,wallSize*5,tileSize/2);
					}
					if (verticalWalls[i].getMaskTextureMouseClickScript() != null)
					{
						g2d.setColor(Color.RED);
						g2d.fillOval(x1-wallSize,y1+tileSize/3,wallSize*3,tileSize/3);
						g2d.setColor(Color.BLACK);
						g2d.drawOval(x1-wallSize,y1+tileSize/3,wallSize*3,tileSize/3);
					}
				}
			}
		}
	}
}

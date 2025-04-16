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

import java.util.*;
import javax.swing.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.Dice;

/**
 * Shuffles objects within their selected tiles
 */
public class ShuffleObjects extends Tool
{
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Shuffle Objects";
	}

	/*-------------------------------------------------------------------------*/
	public void execute(MapEditor editor, Zone zone)
	{
		this.editor = editor;
		List<Object> selection = this.editor.getSelection();
		
		if (selection == null || selection.size() == 0)
		{
			JOptionPane.showMessageDialog(editor, "No tiles or objects selected");
			return;
		}
		
		boolean found = false;
		for (Object obj : selection)
		{
			if (obj instanceof Tile || obj instanceof EngineObject)
			{
				found = true;
				break;
			}
		}
		
		if (!found)
		{
			JOptionPane.showMessageDialog(editor, "No tiles or objects selected");
			return;
		}
		
		applyChangesToSelection();
		editor.display.repaint();
	}

	/*-------------------------------------------------------------------------*/
	private void applyChangesToSelection()
	{
		try
		{
			applyToTiles();
		}
		catch (Exception x)
		{
			x.printStackTrace();
			JOptionPane.showMessageDialog(editor, x.getMessage());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void applyToTiles()
	{
		List<Object> selection = editor.getSelection();
		Map map = editor.getMap();

		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;

				// Get the objects in the tile
				int tileIndex = editor.getCrusaderIndexOfTile(t);
				List<EngineObject> objects = map.getObjects(tileIndex);

				moveObject(map, tileIndex, objects);
			}
			else if (obj instanceof EngineObject)
			{
				moveObject(map, ((EngineObject)obj).getTileIndex(), Collections.singletonList((EngineObject)obj));
			}
		}
	}

	private void moveObject(Map map, int tileIndex, List<EngineObject> objects)
	{
		int tileX = tileIndex % map.getWidth();
		int tileY = tileIndex / map.getWidth();

		for (EngineObject eo : objects)
		{
			int xPos = new Dice(1, map.getBaseImageSize(), tileX * map.getBaseImageSize()).roll("shuffle object x");
			int yPos = new Dice(1, map.getBaseImageSize(), tileY * map.getBaseImageSize()).roll("shuffle object y");

			eo.setXPos(xPos);
			eo.setYPos(yPos);
		}
	}
}

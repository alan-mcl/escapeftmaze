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
import javax.swing.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Tile;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class DeleteObjects extends Tool
{
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Delete Objects";
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
			editor.display.repaint();
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
		
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;
				
				int index = editor.getCrusaderIndexOfTile(t);
				editor.getMap().removeObject(index);
			}
			else if (obj instanceof EngineObject)
			{
				editor.getMap().removeObject((EngineObject)obj);
			}
		}
	}
}

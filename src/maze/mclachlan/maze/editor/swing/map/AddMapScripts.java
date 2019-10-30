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
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Tile;
import mclachlan.maze.editor.swing.SwingEditor;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class AddMapScripts extends Tool
{
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Add Crusader Map Scripts";
	}

	/*-------------------------------------------------------------------------*/
	public void execute(MapEditor editor, Zone zone)
	{
		this.editor = editor;
		List<Object> selection = this.editor.getSelection();

		if (selection == null || selection.size() == 0)
		{
			JOptionPane.showMessageDialog(editor, "No tiles selected");
			return;
		}

		boolean tileFound = false;
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				tileFound = true;
				break;
			}
		}

		if (!tileFound)
		{
			JOptionPane.showMessageDialog(editor, "No tiles selected");
			return;
		}

		ToolMapScriptEditor dialog = new ToolMapScriptEditor(SwingEditor.instance, null, -1);
		MapScript script = dialog.getResult();

		if (script != null)
		{
			int i = JOptionPane.showConfirmDialog(
				SwingEditor.instance,
				"Add a new instance to each tile?",
				"Add Map Script",
				JOptionPane.YES_NO_CANCEL_OPTION);

			switch (i)
			{
				case JOptionPane.YES_OPTION:
					applyChangesToSelection(dialog, true);
					break;
				case JOptionPane.NO_OPTION:
					applyChangesToSelection(dialog, false);
					break;
				case JOptionPane.CANCEL_OPTION:
					return;
				default:
					throw new MazeException("Invalid option: "+i);
			}
		}

		editor.display.repaint();
	}

	/*-------------------------------------------------------------------------*/
	private void applyChangesToSelection(ToolMapScriptEditor dialog, boolean multipleInstances)
	{
		try
		{
			applyToTiles(dialog, multipleInstances);
		}
		catch (Exception x)
		{
			x.printStackTrace();
			JOptionPane.showMessageDialog(editor, x.getMessage());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void applyToTiles(ToolMapScriptEditor dialog, boolean multipleInstances)
	{
		List<Object> selection = editor.getSelection();

		if (multipleInstances)
		{
			for (Object obj : selection)
			{
				if (obj instanceof Tile)
				{
					int index = editor.getCrusaderIndexOfTile((Tile)obj);
					MapScript script = dialog.getNewResult(new int[]{index});
					editor.getMap().addScript(script);
				}
			}
		}
		else
		{
			List<Integer> tiles = new ArrayList<Integer>();

			for (Object obj : selection)
			{
				if (obj instanceof Tile)
				{
					int index = editor.getCrusaderIndexOfTile((Tile)obj);
					tiles.add(index);
				}
			}

			int[] ints = new int[tiles.size()];
			for (int i = 0; i < tiles.size(); i++)
			{
				ints[i] = tiles.get(i);
			}

			MapScript script = dialog.getNewResult(ints);
			editor.getMap().addScript(script);
		}
	}
}
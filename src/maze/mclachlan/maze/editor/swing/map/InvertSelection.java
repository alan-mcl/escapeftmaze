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

package mclachlan.maze.editor.swing.map;

import java.util.*;
import mclachlan.crusader.Tile;

/**
 * Inverts the current selection
 */
public class InvertSelection extends Tool
{
	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "Invert Selection";
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(MapEditor editor)
	{
		List<Object> selection = editor.getSelection();
		List<Object> newSelection = new ArrayList<Object>();

		for (Tile tile : editor.getMap().getTiles())
		{
			if (!selection.contains(tile))
			{
				newSelection.add(tile);
			}
		}

		// todo: how to handle walls

//		for (Wall w : editor.getMap().getHorizontalWalls())
//		{
//			if (!selection.contains(w))
//			{
//				newSelection.add
//			}
//		}

		editor.setSelection(newSelection);
		editor.refreshSelectionSummary();
	}
}

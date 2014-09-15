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
import mclachlan.maze.balance.ZoneScorer;
import mclachlan.maze.map.Tile;

/**
 * Selects the reachable route on the map.
 */
public class RouteFinder extends Tool
{
	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "Select Tiles Accessible by Player";
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(MapEditor editor)
	{
		ZoneScorer zs = new ZoneScorer();

		List<Tile> tiles = zs.getNavigableTiles(editor.zone);

		List<Object> selection = new ArrayList<Object>();

		for (Tile t : tiles)
		{
			mclachlan.crusader.Tile tile = editor.getCrusaderTile(t);
			selection.add(tile);
		}

		editor.setSelection(selection);
		editor.refreshSelectionSummary();
	}
}

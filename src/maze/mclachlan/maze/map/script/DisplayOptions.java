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

package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.DisplayOptionsEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.ui.diygui.MazeScriptOptions;

/**
 *
 */
public class DisplayOptions extends TileScript
{
	private final boolean forceSelection;
	private final String title;
	private final List<String> options, scripts;

	/*-------------------------------------------------------------------------*/
	public DisplayOptions(boolean forceSelection, String title, List<String> options, List<String> scripts)
	{
		this.forceSelection = forceSelection;
		this.title = title;
		this.options = options;
		this.scripts = scripts;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		HashMap<String, String> optionsMap = new HashMap<>();
		for (int i = 0; i < options.size(); i++)
		{
			optionsMap.put(options.get(i), scripts.get(i));
		}

		result.add(new DisplayOptionsEvent(
			new MazeScriptOptions(optionsMap, forceSelection), forceSelection, title, options.toArray(new String[0])));

		return result;
	}

	/*-------------------------------------------------------------------------*/

	public String getTitle()
	{
		return title;
	}

	public List<String> getOptions()
	{
		return options;
	}

	public List<String> getScripts()
	{
		return scripts;
	}

	public boolean isForceSelection()
	{
		return forceSelection;
	}
}

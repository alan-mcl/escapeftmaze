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
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.DisplayOptionsEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.ui.diygui.MazeScriptOptions;

/**
 *
 */
public class DisplayOptions extends TileScript
{
	private boolean forceSelection;
	private String title;
	private List<String> options;
//	private List<String> scripts;
	private List<MazeScript> mazeScripts;

	public DisplayOptions()
	{
	}

	/*-------------------------------------------------------------------------*/
	public DisplayOptions(boolean forceSelection, String title, List<String> options, List<MazeScript> scripts)
	{
		this.forceSelection = forceSelection;
		this.title = title;
		this.options = options;
		this.mazeScripts = scripts;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		HashMap<String, MazeScript> optionsMap = new HashMap<>();
		for (int i = 0; i < options.size(); i++)
		{
			optionsMap.put(options.get(i), mazeScripts.get(i));
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

//	public List<String> getScripts()
//	{
//		return scripts;
//	}

	public boolean isForceSelection()
	{
		return forceSelection;
	}

	public void setForceSelection(boolean forceSelection)
	{
		this.forceSelection = forceSelection;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setOptions(List<String> options)
	{
		this.options = options;
	}

//	public void setScripts(List<String> scripts)
//	{
//		this.scripts = scripts;
//
//		if (scripts != null)
//		{
//			mazeScripts = new ArrayList<>();
//			for (String s : scripts)
//			{
//				mazeScripts.add(Database.getInstance().getMazeScript(s));
//			}
//		}
//	}

	public List<MazeScript> getMazeScripts()
	{
		return mazeScripts;
	}

	public void setMazeScripts(List<MazeScript> mazeScripts)
	{
		this.mazeScripts = mazeScripts;
	}
}

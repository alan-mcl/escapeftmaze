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
	private boolean forceSelection;
	private String title;
	private List<String> options, scripts;

	public DisplayOptions()
	{
	}

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

	public void setScripts(List<String> scripts)
	{
		this.scripts = scripts;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		DisplayOptions that = (DisplayOptions)o;

		if (isForceSelection() != that.isForceSelection())
		{
			return false;
		}
		if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
		{
			return false;
		}
		if (getOptions() != null ? !getOptions().equals(that.getOptions()) : that.getOptions() != null)
		{
			return false;
		}
		return getScripts() != null ? getScripts().equals(that.getScripts()) : that.getScripts() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (isForceSelection() ? 1 : 0);
		result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
		result = 31 * result + (getOptions() != null ? getOptions().hashCode() : 0);
		result = 31 * result + (getScripts() != null ? getScripts().hashCode() : 0);
		return result;
	}
}

/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.ui.diygui.GeneralOptionsCallback;
import mclachlan.maze.ui.diygui.GeneralOptionsDialog;
import mclachlan.maze.ui.diygui.MazeScriptOptions;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DisplayOptionsEvent extends MazeEvent implements GeneralOptionsCallback
{
	private boolean forceSelection;
	private GeneralOptionsCallback callback;
	private List<String> options;
	private List<List<MazeEvent>> mazeScripts;
	private String title;

	private transient String optionChosen;

	public DisplayOptionsEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public DisplayOptionsEvent(GeneralOptionsCallback callback, boolean forceSelection, String title, String... options)
	{
		this.callback = callback;
		this.forceSelection = forceSelection;
		this.options = Arrays.asList(options);
		this.title = title;
	}

	/*-------------------------------------------------------------------------*/
	public DisplayOptionsEvent(boolean forceSelection, String title, List<String> options, List<MazeScript> scripts)
	{
		this.forceSelection = forceSelection;
		this.options = options;
		this.mazeScripts = new ArrayList<>();
		scripts.forEach(script -> {
			if (script == null)
			{
				mazeScripts.add(null);
			}
			else
			{
				mazeScripts.add(script.getEvents());
			}});

		createCallback(forceSelection, options, mazeScripts);
		this.title = title;
	}

	/*-------------------------------------------------------------------------*/
	private void createCallback(boolean forceSelection, List<String> options, List<List<MazeEvent>> scripts)
	{
		HashMap<String, MazeScript> optionsMap = new HashMap<>();
		for (int i = 0; i < options.size(); i++)
		{
			optionsMap.put(options.get(i), new MazeScript("option "+i, scripts.get(i)));
		}

		this.callback = new MazeScriptOptions(optionsMap, forceSelection);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		GeneralOptionsDialog dialog = new GeneralOptionsDialog(this, forceSelection, title, options.toArray(new String[0]));
		Maze.getInstance().getUi().showDialog(dialog);

		synchronized(Maze.getInstance().getEventMutex())
		{
			try
			{
				Maze.getInstance().getEventMutex().wait();
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}
		}

		return callback.optionChosen(optionChosen);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> optionChosen(String option)
	{
		optionChosen = option;
		synchronized(Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	public boolean isForceSelection()
	{
		return forceSelection;
	}

	public void setForceSelection(boolean forceSelection)
	{
		this.forceSelection = forceSelection;
	}

	public List<String> getOptions()
	{
		return options;
	}

	public void setOptions(List<String> options)
	{
		this.options = options;
	}

	public List<List<MazeEvent>> getMazeScripts()
	{
		return mazeScripts;
	}

	public void setMazeScripts(List<List<MazeEvent>> mazeScripts)
	{
		this.mazeScripts = mazeScripts;
		createCallback(forceSelection, options, mazeScripts);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}

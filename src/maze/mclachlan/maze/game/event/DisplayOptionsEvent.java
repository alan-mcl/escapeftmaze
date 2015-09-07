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
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.ui.diygui.GeneralOptionsCallback;
import mclachlan.maze.ui.diygui.GeneralOptionsDialog;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DisplayOptionsEvent extends MazeEvent implements GeneralOptionsCallback
{
	private Npc npc;
	private String[] options;
	private String title;

	private transient String optionChosen;

	/*-------------------------------------------------------------------------*/
	public DisplayOptionsEvent(Npc npc, String title, String... options)
	{
		this.npc = npc;
		this.options = options;
		this.title = title;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		GeneralOptionsDialog dialog = new GeneralOptionsDialog(this, title, options);
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

		return npc.getScript().optionChosen(optionChosen);
	}

	/*-------------------------------------------------------------------------*/
	public void optionChosen(String option)
	{
		optionChosen = option;
		synchronized(Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}
	}
}

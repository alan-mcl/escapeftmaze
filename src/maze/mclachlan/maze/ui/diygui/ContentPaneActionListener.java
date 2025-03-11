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

package mclachlan.maze.ui.diygui;

import java.awt.event.MouseEvent;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.game.Maze;

/**
 * Since the content pane is the ultimate parent of all the widgets, events
 * are often passed up here.  This is where all the neat shortcut keys are
 * implemented. 
 */
class ContentPaneActionListener implements ActionListener
{
	private final DiyGuiUserInterface ui;

	/*-------------------------------------------------------------------------*/
	public ContentPaneActionListener(DiyGuiUserInterface ui)
	{
		this.ui = ui;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (Maze.getInstance().getUi() == null)
		{
			return false;
		}

		if (event.getEvent() instanceof MouseEvent)
		{
			this.processMouseEvent((MouseEvent)event.getEvent());
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private boolean processMouseEvent(MouseEvent event)
	{
		boolean consumed = DiyGuiUserInterface.instance.mouseEventToAnimations(event);

		if (!consumed)
		{
			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				synchronized (Maze.getInstance().getEventMutex())
				{
					Maze.getInstance().getEventMutex().notifyAll();
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				synchronized (Maze.getInstance().getEventMutex())
				{
					Maze.getInstance().getEventMutex().notifyAll();
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.RESTING)
			{
				if (ui.restingWidget.done == event.getSource())
				{
					ui.restingWidget.done();
				}
			}
		}

		return consumed;
	}
}

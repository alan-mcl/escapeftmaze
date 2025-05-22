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

package mclachlan.maze.editor.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MazeEventsComponent extends JButton implements ActionListener
{
	private List<MazeEvent> events;
	private final MazeEventsComponentCallback callback;
	private final int dirtyFlag;

	interface MazeEventsComponentCallback
	{
		void eventsChanged(MazeEventsComponent mazeEventsComponent);
	}

	/*-------------------------------------------------------------------------*/
	public MazeEventsComponent(int dirtyFlag)
	{
		this(null, dirtyFlag, null);
	}

	/*-------------------------------------------------------------------------*/
	public MazeEventsComponent(List<MazeEvent> events, int dirtyFlag)
	{
		this(events, dirtyFlag, null);
	}

	/*-------------------------------------------------------------------------*/
	public MazeEventsComponent(
		List<MazeEvent> events,
		int dirtyFlag, 
		MazeEventsComponentCallback callback)
	{
		this.dirtyFlag = dirtyFlag;
		refresh(events);
		addActionListener(this);
		this.callback = callback;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		Dimension result = new Dimension(d);
		result.height -= 5;
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<MazeEvent> events)
	{
		this.events = events;
		if (events == null || events.isEmpty())
		{
			this.setText(EditorPanel.NONE);
		}
		else
		{
			this.setText(events.size()+" event(s)");
		}
	}

	/*-------------------------------------------------------------------------*/

	public List<MazeEvent> getEvents()
	{
		return events;
	}

	/*-------------------------------------------------------------------------*/

	public void setEvents(List<MazeEvent> events)
	{
		this.events = events;
		refresh(events);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			MazeEventsDialog dialog = new MazeEventsDialog(SwingEditor.instance, events, dirtyFlag);
			if (dialog.getEvents() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				this.setEvents(dialog.getEvents());
				if (callback != null)
				{
					callback.eventsChanged(this);
				}
			}
		}
	}
}

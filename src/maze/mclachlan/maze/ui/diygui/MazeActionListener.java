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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MazeActionListener implements ActionListener
{
	public static boolean acceptInput = true;

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getEvent() instanceof MouseEvent)
		{
			return this.processMouse(event);
		}
		else if (event.getEvent() instanceof KeyEvent)
		{
			return this.processKey(event);
		}
		else
		{
			throw new MazeException("Unrecognised InputEvent: " + event);
		}
	}

	/*----------------------------------------------------------------------*/
	private boolean processMouse(ActionEvent event)
	{
		String message = event.getMessage();

		if (message == null)
		{
			return false;
		}

//		boolean eventConsumed = DiyGuiUserInterface.instance.mouseEventToAnimations((MouseEvent)event.getEvent());
		boolean eventConsumed = false;

		if (!eventConsumed)
		{
			Maze.logDebug("UI MSG: " + message);
		}

		return eventConsumed;
	}

	/*----------------------------------------------------------------------*/
	private boolean processKey(ActionEvent event)
	{
//		boolean eventConsumed = DiyGuiUserInterface.instance.keyEventToAnimations((KeyEvent)event.getEvent());
		boolean eventConsumed = false;

		if (!eventConsumed)
		{
			if (Maze.getInstance().getState() == Maze.State.MOVEMENT && acceptInput)
			{
				KeyEvent e = (KeyEvent)event.getEvent();
				if (e.getID() != KeyEvent.KEY_PRESSED)
				{
					return false;
				}

				int code = e.getKeyCode();

				if (DiyGuiUserInterface.crusaderKeys.containsKey(code) &&
					Maze.getInstance().getState() == Maze.State.MOVEMENT &&
					DIYToolkit.getInstance().getDialog() == null)
				{
					int crusaderKey = DiyGuiUserInterface.crusaderKeys.get(code);
					return handleKeyCode(crusaderKey);
				}
			}
		}

		return eventConsumed;
	}

	/*-------------------------------------------------------------------------*/
	boolean handleKeyCode(int crusaderKey)
	{
		acceptInput = false;
		Maze.getInstance().appendEvents(
			new HandleKeyEvent(crusaderKey),
			new EnableInputEvent());
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public static class EnableInputEvent extends MazeEvent
	{
		@Override
		public List<MazeEvent> resolve()
		{
			acceptInput = true;
			return null;
		}
	}
}

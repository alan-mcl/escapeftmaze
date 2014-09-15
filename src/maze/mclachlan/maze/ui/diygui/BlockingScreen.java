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

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class BlockingScreen extends DIYPanel
{
	private int delay;
	private Object mutex;

	/*-------------------------------------------------------------------------*/
	public BlockingScreen(String imageResource, int delay, Object mutex)
	{
		super(0, 0, DiyGuiUserInterface.SCREEN_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT);
		this.delay = delay;
		this.mutex = mutex;
		Image back = Database.getInstance().getImage(imageResource);
		setBackgroundImage(back);
	}

	/*-------------------------------------------------------------------------*/
	public BlockingScreen(ContainerWidget dialog, int delay, Object mutex)
	{
		super(0, 0, DiyGuiUserInterface.SCREEN_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT);
		this.delay = delay;
		this.mutex = mutex;
		dialog.setBounds(this.getBounds());
		dialog.doLayout();
		this.add(dialog);
	}

	/*-------------------------------------------------------------------------*/
//	public void notifyListeners(InputEvent e)
//	{
//		clear();
//	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void processKeyTyped(KeyEvent e)
	{
		clear();
	}

	@Override
	public void processMouseClicked(MouseEvent e)
	{
		clear();
	}

	/*-------------------------------------------------------------------------*/
	private void clear()
	{
		if (mutex != null && delay == Mode.INTERRUPTABLE)
		{
			synchronized(mutex)
			{
				mutex.notifyAll();
			}
		}

		if (delay > -1)
		{
			Maze.getInstance().getUi().clearDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class Mode
	{
		public static final int UNINTERRUPTABLE = -1;
		public static final int INTERRUPTABLE = 1;
	}
}

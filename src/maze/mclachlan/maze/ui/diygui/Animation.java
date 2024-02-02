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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 *
 */
public abstract class Animation
{
	private Object mutex;

	/*-------------------------------------------------------------------------*/
	public abstract void draw(Graphics2D g);

	/*-------------------------------------------------------------------------*/
	public abstract Animation spawn(AnimationContext context);

	/*-------------------------------------------------------------------------*/
	public Object getMutex()
	{
		return mutex;
	}

	/*-------------------------------------------------------------------------*/
	public void setMutex(Object mutex)
	{
		this.mutex = mutex;
	}

	/*-------------------------------------------------------------------------*/
	public DiyGuiUserInterface getUi()
	{
		return DiyGuiUserInterface.instance;
	}

	/*-------------------------------------------------------------------------*/
	public abstract boolean isFinished();

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	true if the animation consumes this event, in this case no other
	 * 	listeners in the UI get it
	 */
	public boolean processMouseEvent(MouseEvent event) { return false; }

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the animation consumes this event, in this case no other
	 * 	listeners in the UI get it
	 */
	public boolean processKeyEvent(KeyEvent event) { return false; }
}

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

package mclachlan.diygui.toolkit;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 */
public class FullscreenFrame extends Frame
{
	public FullscreenFrame(String title, int screenWidth, int screenHeight)
	{
		super(title);
		
		GraphicsDevice device =
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		this.enableEvents(
			KeyEvent.KEY_EVENT_MASK |
			MouseEvent.MOUSE_EVENT_MASK);
		this.setUndecorated(true);

		device.setFullScreenWindow(this);
		this.enableInputMethods(false);
		device.setDisplayMode(new DisplayMode(screenWidth, screenHeight, 32, 0));

		this.createBufferStrategy(2);
	}
}

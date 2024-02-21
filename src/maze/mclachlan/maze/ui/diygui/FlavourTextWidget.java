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
import java.awt.event.MouseEvent;
import mclachlan.maze.game.Maze;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class FlavourTextWidget extends DIYPane
{
	private DIYTextArea textArea;

	/*-------------------------------------------------------------------------*/
	public FlavourTextWidget(Rectangle bounds)
	{
		// todo: break text up into multiple displays
		super(bounds);
		this.textArea = new DIYTextArea("");
		this.textArea.setTransparent(true);
		textArea.setBounds(bounds);
		this.add(textArea);
	}
	
	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.textArea.setText(text);
	}

	/*-------------------------------------------------------------------------*/
	public Widget getChild(int x, int y)
	{
		return this;
	}

	/*-------------------------------------------------------------------------*/
	public boolean processMouseClicked(MouseEvent e)
	{
		// todo: display more text if required
		synchronized(Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
			return true;
		}
	}
}

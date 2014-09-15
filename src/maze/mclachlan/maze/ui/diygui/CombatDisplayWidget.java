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
import java.awt.event.KeyEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;

/**
 * Poorly named since is actually displays most all the events.
 */
public class CombatDisplayWidget extends ContainerWidget
{
	StringBuilder buffer = new StringBuilder();
	StringBuilder readLineBuffer = new StringBuilder();
	private MazeEvent currentEvent;
	private DIYTextArea textArea;

	/*-------------------------------------------------------------------------*/
	public CombatDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		this.textArea = new DIYTextArea("");
		this.textArea.setTransparent(true);
		textArea.setBounds(bounds);
		this.add(textArea);
	}
	
	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentEvent(MazeEvent event, boolean displayEventText)
	{
		this.currentEvent = event;

		if (event.shouldClearText())
		{
			clear();
		}

		if (event.getText() != null && displayEventText)
		{
			String eventDesc = event.getText();
			Maze.log(eventDesc);
			buffer.append(eventDesc);
			buffer.append("\n");
	
			this.textArea.setText(buffer.toString());
		}
	}

	/*-------------------------------------------------------------------------*/
	public Widget getChild(int x, int y)
	{
		// h4x0r so that we see the mouse clicked events
		return this;
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		buffer = new StringBuilder();
		readLineBuffer = new StringBuilder();
		this.textArea.setText(buffer.toString());
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		if (currentEvent != null)
		{
			synchronized (currentEvent)
			{
				currentEvent.notifyAll();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (currentEvent == null)
		{
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_BACK_SPACE:
				if (currentEvent.getDelay() == MazeEvent.Delay.WAIT_ON_READLINE)
				{
					if (readLineBuffer.length() > 0)
					{
						buffer = new StringBuilder(buffer.substring(0,buffer.length()-1));
						readLineBuffer = new StringBuilder(readLineBuffer.substring(0,readLineBuffer.length()-1));
					}

					this.textArea.setText(buffer.toString());
				}
				break;
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_ALT:
				// ignore
				break;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_ESCAPE:
				if (currentEvent.getDelay() == MazeEvent.Delay.WAIT_ON_CLICK)
				{
					synchronized (currentEvent)
					{
						currentEvent.notifyAll();
					}
				}
				else if (currentEvent.getDelay() == MazeEvent.Delay.WAIT_ON_READLINE)
				{
					synchronized (currentEvent)
					{
						currentEvent.notifyAll();
					}
				}
				break;
			default:
				// other keys
				if (currentEvent.getDelay() == MazeEvent.Delay.WAIT_ON_READLINE)
				{
					this.buffer.append(e.getKeyChar());
					this.readLineBuffer.append(e.getKeyChar());
					this.textArea.setText(buffer.toString());
				}
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getPlayerSpeech()
	{
		return this.readLineBuffer.toString();
	}
}

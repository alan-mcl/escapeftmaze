/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.ui.diygui.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Modal speech bubbles implemented as a DIY UI Dialog
 */
public class SpeechBubbleDialog extends DIYPane
{
	private final SpeechBubble speechBubble;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleDialog(
		Color colour,
		String text,
		Rectangle origination,
		SpeechBubble.Orientation orientation)
	{
		speechBubble = new SpeechBubble(colour, text, origination, orientation);

		speechBubble.computeBounds(DIYToolkit.getInstance().getGraphics(), null, -1);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		this.speechBubble.draw(g, true);
	}

	/*-------------------------------------------------------------------------*/
	private void clear()
	{
		synchronized (Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void processMouseClicked(MouseEvent e)
	{
		clear();
	}

	@Override
	public void processKeyTyped(KeyEvent e)
	{
		e.consume();
		clear();
	}
}

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
import mclachlan.crusader.EngineObject;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Modal speech bubbles implemented as a DIY UI Dialog
 */
public class SpeechBubbleDialog extends DIYPane
{
	// if not null, let the origination track this object
	private EngineObject origination;

	private final SpeechBubble speechBubble;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleDialog(
		Color colour,
		String text,
		Rectangle origination,
		SpeechBubble.Orientation orientation)
	{
		speechBubble = init(colour, text, orientation, origination);
	}

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleDialog(
		Color colour,
		String text,
		EngineObject origination,
		SpeechBubble.Orientation orientation)
	{
		this.origination = origination;

		Rectangle rect = Maze.getInstance().getUi().getObjectBounds(origination);

		speechBubble = init(colour, text, orientation, rect);
	}

	/*-------------------------------------------------------------------------*/
	private SpeechBubble init(Color colour, String text,
		SpeechBubble.Orientation orientation, Rectangle rect)
	{
		final SpeechBubble speechBubble;
		speechBubble = new SpeechBubble(colour, text, rect, orientation);

		speechBubble.computeBounds(DIYToolkit.getInstance().getGraphics(), null, -1);
		return speechBubble;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		if (origination != null)
		{
			this.speechBubble.setOrigination(Maze.getInstance().getUi().getObjectBounds(origination));
			this.speechBubble.computeBounds(g, null, -1);
		}
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

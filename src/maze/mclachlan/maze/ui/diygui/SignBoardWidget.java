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
import mclachlan.diygui.DIYPanel;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class SignBoardWidget extends DIYPanel
{
	private Point[] positions;
	private String[] text;
	private final Rectangle bounds;
	private Font font;

	/*-------------------------------------------------------------------------*/
	public SignBoardWidget(Rectangle bounds, Image image)
	{
		super(bounds);
		this.bounds = bounds;
		this.setBackgroundImage(image);
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text = text.split("\n");

		Component comp = Maze.getInstance().getComponent();
		Font signboardFont = Maze.getInstance().getUi().getSignboardFont();

		int inset = 40;
		Rectangle textBounds = new Rectangle(
			bounds.x +inset,
			bounds.y +inset,
			bounds.width -inset*2,
			bounds.height -inset*2);

		layoutText((Graphics2D)comp.getGraphics(), this.text, textBounds, signboardFont);

	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		super.draw(g);

		g.setFont(font);
		g.setColor(Color.BLACK);
		for (int i = 0; i < text.length; i++)
		{
			g.drawString(text[i], positions[i].x, positions[i].y);
		}
		g.setFont(Maze.getInstance().getUi().getDefaultFont());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processKeyPressed(KeyEvent e)
	{
		clearSignboard();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseClicked(MouseEvent e)
	{
		clearSignboard();
	}

	/*-------------------------------------------------------------------------*/
	public void clearSignboard()
	{
		Maze.getInstance().setState(Maze.State.MOVEMENT);

		synchronized (Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}

		Maze.getInstance().getUi().clearDialog();
	}


	/*-------------------------------------------------------------------------*/
	public void layoutText(Graphics2D g2d, String[] textRows,
		Rectangle bounds, Font baseFont)
	{
		if (textRows == null || textRows.length == 0 || bounds.width <= 0 || bounds.height <= 0)
		{
			return;
		}

		int low = 1, high = 1000;
		Font bestFont = baseFont;
		FontMetrics metrics;

		while (low < high)
		{
			int mid = (low + high + 1) / 2;
			Font testFont = baseFont.deriveFont((float)mid);
			metrics = g2d.getFontMetrics(testFont);

			int totalHeight = textRows.length * metrics.getHeight();
			int maxWidth = 0;
			for (String row : textRows)
			{
				maxWidth = Math.max(maxWidth, metrics.stringWidth(row));
			}

			if (totalHeight <= bounds.height && maxWidth <= bounds.width)
			{
				bestFont = testFont;
				low = mid;
			}
			else
			{
				high = mid - 1;
			}
		}
		this.font = bestFont;

		metrics = g2d.getFontMetrics(bestFont);
		int totalHeight = textRows.length * metrics.getHeight();
		int startY = bounds.y + (bounds.height - totalHeight) / 2 + metrics.getAscent();

		positions = new Point[textRows.length];
		for (int i = 0; i < textRows.length; i++)
		{
			int textWidth = metrics.stringWidth(textRows[i]);
			int x = bounds.x + (bounds.width - textWidth) / 2;
			int y = startY + i * metrics.getHeight();
			positions[i] = new Point(x, y);
		}
	}

}

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
import mclachlan.maze.game.MazeEvent;
import mclachlan.diygui.DIYPanel;

/**
 *
 */
public class SignBoardWidget extends DIYPanel
{
	private String[] text;
	private int[] textY;
	private int[] textX;
	private Rectangle bounds;
	private MazeEvent event;
	private Font font;

	/*-------------------------------------------------------------------------*/
	public SignBoardWidget(Rectangle bounds, Image image)
	{
		super(bounds);
		this.bounds = bounds;
		this.setBackgroundImage(image);
	}
	
	/*-------------------------------------------------------------------------*/
	public void setText(String text, MazeEvent event)
	{
		this.event = event;
		this.text = text.split("\n");
		textX = new int[this.text.length];
		textY = new int[this.text.length];

		font = Maze.getInstance().getUi().getDefaultFont().deriveFont(Font.BOLD, 20.0F);
		FontMetrics fm = Maze.getInstance().getComponent().getFontMetrics(font);
		int lineHeight = fm.getHeight();

		// center vertically
		int startY = bounds.y + height/2 - lineHeight/2 - (lineHeight-1)*this.text.length;
		for (int i = 0; i < this.text.length; i++)
		{
			textY[i] = startY+lineHeight*(i+2);
		}

		// center horizontally
		for (int i = 0; i < textX.length; i++)
		{
			textX[i] = bounds.x + bounds.width/2 - fm.stringWidth(this.text[i])/2;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		super.draw(g);

		g.setFont(font);
		g.setColor(Color.BLACK);
		for (int i = 0; i < text.length; i++)
		{
			g.drawString(text[i], textX[i], textY[i]);
		}
		g.setFont(Maze.getInstance().getUi().getDefaultFont());
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		clearSignboard();
	}

	/*-------------------------------------------------------------------------*/
	public void clearSignboard()
	{
		Maze.getInstance().popState();
		
		synchronized(event)
		{
			event.notifyAll();
		}
	}
}

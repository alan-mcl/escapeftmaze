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

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

public class FlavourTextDialog extends GeneralDialog
{
	private DIYTextArea text;

	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;

	/*-------------------------------------------------------------------------*/
	public FlavourTextDialog(String title, String text)
	{
		// try to guess dialog height
		List<String> lines = DIYToolkit.wrapText(
			text,
			DiyGuiUserInterface.gui.getComponent().getGraphics(),
			DIALOG_WIDTH);

		int dialogHeight = (int)(lines.size() * (DIYToolkit.getDimension("|").getHeight()+5)) +border*2;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		this.setBounds(startX, startY, DIALOG_WIDTH, dialogHeight);

		DIYPane titlePane = null;

		Rectangle textBounds;

		if (title != null)
		{
			titlePane = getTitle(title);

			textBounds = new Rectangle(
				x +border,
				y +border +titlePaneHeight,
				width -border*2,
				height -border*2 -titlePaneHeight);
		}
		else
		{
			textBounds = new Rectangle(
				x +border,
				y +border,
				width -border*2,
				height -border*2);
		}

		this.text = new DIYTextArea(text);
		this.text.setTransparent(true);
		this.text.setBounds(textBounds);

		setBackground();

		if (titlePane != null)
		{
			this.add(titlePane);
		}
		this.add(this.text);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text.setText(text);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}
	
	/*-------------------------------------------------------------------------*/
	@Override
	public void processKeyPressed(KeyEvent e)
	{
		exitDialog();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseClicked(MouseEvent e)
	{
		exitDialog();
	}

	/*-------------------------------------------------------------------------*/
	private void exitDialog()
	{
		synchronized (Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}
		Maze.getInstance().getUi().clearDialog();
	}
}

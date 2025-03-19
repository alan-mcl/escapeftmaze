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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.script.FlavourTextEvent;

public class FlavourTextDialog extends GeneralDialog implements ActionListener
{
	private final DIYTextArea text;

	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;

	/*-------------------------------------------------------------------------*/
	public FlavourTextDialog(
		String title,
		String text,
		FlavourTextEvent.Alignment alignment)
	{
		super.setStyle(Style.PANEL_MED);

		// try to guess dialog height
		int textBoundsWidth = DIALOG_WIDTH - getBorder() * 2;
		List<String> lines = DIYToolkit.wrapText(
			text,
			textBoundsWidth -2,
			null);

		int dialogHeight = (int)(lines.size() * (DIYToolkit.getDimension("|").getHeight())) + getBorder() *2;

		if (alignment == null)
		{
			alignment = FlavourTextEvent.Alignment.CENTER;
		}

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = switch (alignment)
			{
				case TOP -> DiyGuiUserInterface.SCREEN_HEIGHT/2 - DiyGuiUserInterface.MAZE_HEIGHT/2;
				case CENTER -> DiyGuiUserInterface.SCREEN_HEIGHT / 2 - dialogHeight/2;
				case BOTTOM -> DiyGuiUserInterface.MAZE_WINDOW_BOUNDS.y + DiyGuiUserInterface.MAZE_HEIGHT -DiyGuiUserInterface.SCREEN_EDGE_INSET -dialogHeight;
			};

		this.setBounds(startX, startY, DIALOG_WIDTH, dialogHeight);

		DIYPane titlePane = null;

		Rectangle textBounds;

		if (title != null)
		{
			titlePane = getTitlePane(title);

			textBounds = new Rectangle(
				x + getBorder(),
				y + getBorder() + getTitlePaneHeight(),
				textBoundsWidth,
				height - getBorder() *2 - getTitlePaneHeight());
		}
		else
		{
			textBounds = new Rectangle(
				x + getBorder(),
				y + getBorder(),
				textBoundsWidth,
				height - getBorder() *2);
		}

		this.text = new DIYTextArea(text);
		this.text.setTransparent(true);
		this.text.setBounds(textBounds);
		this.text.addActionListener(this);

		if (titlePane != null)
		{
			this.add(titlePane);
		}
		this.add(this.text);
		this.doLayout();
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
		if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			e.consume();
			exitDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseClicked(MouseEvent e)
	{
		exitDialog();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		exitDialog();
		return true;
	}
}

/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, contains another widget and an ok button.
 */
public class ContainerDialog extends GeneralDialog implements ActionListener
{
	private final DIYButton close;

	/*-------------------------------------------------------------------------*/
	public ContainerDialog(String title, ContainerWidget contents, Rectangle bounds)
	{
		this.setBounds(bounds);

		close = getCloseButton();
		close.addActionListener(this);

		Rectangle r = new Rectangle(
			bounds.x + getInset() + getBorder(),
			bounds.y + getInset() + getBorder() + getTitlePaneHeight(),
			bounds.width - getInset() *2 - getBorder() *2,
			bounds.height - getInset() *2 - getBorder() *2 - getTitlePaneHeight());

		contents.setBounds(r);
		contents.doLayout();

		DIYPane titlePane = getTitlePane(title);
		titlePane.doLayout();

		this.add(titlePane);
		this.add(close);
		this.add(contents);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}
	
	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			exitDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	protected void exitDialog()
	{
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
		{
			exitDialog();
			return true;
		}
		return false;
	}
}

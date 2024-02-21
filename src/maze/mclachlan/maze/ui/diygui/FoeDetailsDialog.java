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
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;

/**
 * Generalised popup dialog, with a scroll pane and an OK/Cancel button.
 */
public class FoeDetailsDialog extends GeneralDialog implements ActionListener
{
	private DIYButton ok;

	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/4;

	/*-------------------------------------------------------------------------*/
	public FoeDetailsDialog(Foe foe, int information)
	{
		this.ok = new DIYButton("OK");

		int x = DiyGuiUserInterface.SCREEN_WIDTH / 6;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 6;
		Rectangle bounds = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 3 * 2, DiyGuiUserInterface.SCREEN_HEIGHT / 3 * 2);

		this.setBounds(bounds);

		int inset = 2, border = 15;
		int buttonHeight = 17;
		int buttonWidth = bounds.width/4;

		ok.setBounds(new Rectangle(
			bounds.x+ bounds.width/2 -buttonWidth/2 -inset,
			bounds.y+ bounds.height - buttonHeight - inset - border,
			buttonWidth, buttonHeight));

		ok.addActionListener(this);

		FoeDetailsWidget fdw = new FoeDetailsWidget(bounds, foe, information);

		this.add(ok);
		this.add(fdw);

		setBackground();
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}
	
	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			exitDialog();
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
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
		if (event.getSource() == ok)
		{
			exitDialog();
			return true;
		}

		return false;
	}
}

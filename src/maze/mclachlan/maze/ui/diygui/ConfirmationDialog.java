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

package mclachlan.maze.ui.diygui;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, with a scroll pane and an OK/Cancel button.
 */
public class ConfirmationDialog extends GeneralDialog implements ActionListener
{
	private DIYButton ok, cancel;
	private DIYTextArea text;

	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/4;
	private ConfirmCallback callback;

	/*-------------------------------------------------------------------------*/
	public ConfirmationDialog(String labels, ConfirmCallback callback)
	{
		this.callback = callback;
		this.ok = new DIYButton("OK");
		this.cancel = new DIYButton("Cancel");

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle bounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);

		this.setBounds(bounds);

		int inset = 2, border = 15;
		int buttonHeight = 17;
		int buttonWidth = bounds.width/4;

		ok.setBounds(new Rectangle(
			bounds.x+ bounds.width/2 -buttonWidth -inset,
			bounds.y+ bounds.height - buttonHeight - inset - border,
			buttonWidth, buttonHeight));

		cancel.setBounds(new Rectangle(
			bounds.x+ bounds.width/2 +inset,
			bounds.y+ bounds.height - buttonHeight - inset - border,
			buttonWidth, buttonHeight));

		ok.addActionListener(this);
		cancel.addActionListener(this);

		Rectangle r = new Rectangle(
			bounds.x +inset +border,
			bounds.y +inset +border,
			bounds.width -inset*2 -border*2,
			bounds.height -buttonHeight -inset*3 -border*2);

		text = new DIYTextArea(labels);
		text.setTransparent(true);
		text.setBounds(r);

		this.add(ok);
		this.add(cancel);
		this.add(text);

	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text.setText(text);
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
			callback.confirm();
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
			callback.confirm();
			exitDialog();
			return true;
		}
		else if (event.getSource() == cancel)
		{
			exitDialog();
			return true;
		}
		return false;
	}
}

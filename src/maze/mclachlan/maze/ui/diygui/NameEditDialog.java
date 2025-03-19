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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextField;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class NameEditDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3;

	private final DIYTextField text;
	private final DIYButton okButton, close;
	private final NameCallback callback;

	/*-------------------------------------------------------------------------*/
	public NameEditDialog(
		NameCallback callback,
		String title)
	{
		this.callback = callback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		int border = getBorder();
		int inset = getInset();

		int textFieldHeight = 40;
		Rectangle textBounds = new Rectangle(
			startX +border +inset,
			startY +height/2 -textFieldHeight/2,
			width -border*2 -inset*2,
			textFieldHeight);

		text = new DIYTextField("", 20);
		text.setBounds(textBounds);

		DIYPane titlePane = getTitlePane(title);
		DIYPane buttonPane = getButtonPane();

		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);

		close = getCloseButton();
		close.addActionListener(this);
		
		buttonPane.add(okButton);

		this.add(titlePane);
		this.add(text);
		this.add(buttonPane);
		this.add(close);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE -> { e.consume(); exit(); }
			case KeyEvent.VK_ENTER -> { e.consume(); setName(); }
			default -> text.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			setName();
			return true;
		}
		else if (event.getSource() == close)
		{
			exit();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void setName()
	{
		exit();
		callback.setName(text.getText());
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

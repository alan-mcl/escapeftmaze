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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextField;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class GetPlayerSpeechDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;

	private final DIYButton okButton, close;
	private final DIYTextField inputField;
	private final TextDialogCallback textDialogCallback;

	/*-------------------------------------------------------------------------*/
	public GetPlayerSpeechDialog(
		UnifiedActor pc,
		TextDialogCallback textDialogCallback)
	{
		super();
		this.textDialogCallback = textDialogCallback;

		int border = getBorder();
		int inset = getInset();
		int buttonPaneHeight = getButtonPaneHeight();
		int titlePaneHeight = getTitlePaneHeight();

		int DIALOG_HEIGHT = border*2 +inset*3 +titlePaneHeight +buttonPaneHeight +50;

		int startX = DiyGuiUserInterface.LOW_BOUNDS.x + DiyGuiUserInterface.LOW_BOUNDS.width/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.LOW_BOUNDS.y + DiyGuiUserInterface.LOW_BOUNDS.height/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);


		DIYPane title = getTitlePane(StringUtil.getUiLabel("gps.title", pc.getDisplayName()));

		inputField = new DIYTextField();
		inputField.setBounds(
			x +border +inset,
			y +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			buttonPaneHeight);
		inputField.addActionListener(this);

		DIYPane buttonPane = getButtonPane();
		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);
		
		close = getCloseButton();
		close.addActionListener(this);
		buttonPane.add(okButton);
		this.add(close);

		this.add(title);
		this.add(inputField);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER -> { e.consume(); finished(); }
			case KeyEvent.VK_ESCAPE -> { e.consume(); exit(); }
			default -> inputField.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton/* || event.getSource() == inputField*/)
		{
			finished();
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
	private void finished()
	{
		if (okButton.isEnabled())
		{
			exit();
			textDialogCallback.textEntered(inputField.getText());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

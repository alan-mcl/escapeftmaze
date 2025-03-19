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
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GetAmountDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3;

	private final DIYButton okButton, close;
	private final DIYTextField amountField;
	private final TextDialogCallback textDialogCallback;

	/*-------------------------------------------------------------------------*/
	public GetAmountDialog(
		PlayerCharacter pc,
		final int max,
		TextDialogCallback textDialogCallback)
	{
		super();
		this.textDialogCallback = textDialogCallback;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);

		this.setBounds(dialogBounds);

		int border = getBorder();
		int inset = getInset();
		int buttonPaneHeight = getButtonPaneHeight();
		int titlePaneHeight = getTitlePaneHeight();

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("gad.how.much", max));

		amountField = new DIYTextField()
		{
			public void processKeyPressed(KeyEvent e)
			{
				super.processKeyPressed(e);
				try
				{
					int amount = Integer.parseInt(amountField.getText());
					okButton.setEnabled(amount <= max);
				}
				catch (NumberFormatException x)
				{
					okButton.setEnabled(false);
				}
			}
		};
		amountField.setBounds(
			x +border +inset,
			y +border +inset +titlePaneHeight,
			width -inset*2 -border*2,
			35);

		DIYPane buttonPane = getButtonPane();
		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);
		
		close = getCloseButton();
		close.addActionListener(this);

		buttonPane.add(okButton);
		this.add(close);

		okButton.setEnabled(false);

		this.add(titlePane);
		this.add(amountField);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER -> { e.consume(); amountEntered(); }
			case KeyEvent.VK_ESCAPE -> { e.consume(); exit(); }
			default -> amountField.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			amountEntered();
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
	private void amountEntered()
	{
		if (okButton.isEnabled())
		{
			exit();
			textDialogCallback.textEntered(amountField.getText());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

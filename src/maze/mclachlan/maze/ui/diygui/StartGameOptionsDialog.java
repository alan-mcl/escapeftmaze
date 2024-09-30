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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class StartGameOptionsDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private StartGameOptionsWidget optionsWidget;
	private DIYButton okButton, cancel;
	private StartGameCallback callback;

	/*-------------------------------------------------------------------------*/
	public StartGameOptionsDialog(
		StartGameCallback callback)
	{
		this.callback = callback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(
			startX,
			startY,
			DIALOG_WIDTH,
			DIALOG_HEIGHT);

		int buttonPaneHeight = 20;
		int inset = 20;
		Rectangle isBounds = new Rectangle(
			startX+ inset,
			startY+ inset + buttonPaneHeight,
			DIALOG_WIDTH- inset *2,
			DIALOG_HEIGHT- buttonPaneHeight *2- inset *4);

		this.setBounds(dialogBounds);

		optionsWidget = new StartGameOptionsWidget(isBounds);

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("sgo.difficulty.level"));

		if (Maze.getInstance().getParty().size() < 6)
		{
			DIYPane warningPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
			warningPane.setBounds(
				x+ buttonPaneHeight,
				y+ getBorder() + inset,
				width- buttonPaneHeight *2,
				buttonPaneHeight *2);
			DIYTextArea warningLabel = new DIYTextArea(StringUtil.getUiLabel("sgo.small.party.warning"));
			warningLabel.setForegroundColour(Color.WHITE);
			warningLabel.setTransparent(true);
			warningPane.add(warningLabel);

			this.add(warningPane);
		}

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		cancel = new DIYButton("Cancel");
		cancel.addActionListener(this);

		buttonPane.add(okButton);
		buttonPane.add(cancel);

		this.add(titlePane);
		this.add(optionsWidget);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
			case KeyEvent.VK_ENTER:
				startGame();
				break;
			default:

		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			startGame();
			return true;
		}
		else if (event.getSource() == cancel)
		{
			exit();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void startGame()
	{
		exit();
		callback.startGame(
			optionsWidget.getDifficultyLevel());
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}
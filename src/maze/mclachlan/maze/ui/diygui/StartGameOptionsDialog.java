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

	private final StartGameOptionsWidget optionsWidget;
	private final DIYButton okButton, close;
	private final StartGameCallback callback;

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
		this.setBounds(dialogBounds);

		int inset = getInset();
		int border = getBorder();
		int titlePaneHeight = getTitlePaneHeight();
		int buttonPaneHeight = getButtonPaneHeight();

		Rectangle isBounds = new Rectangle(
			x +inset +border,
			y +inset +border +buttonPaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*3 - titlePaneHeight -buttonPaneHeight);

		optionsWidget = new StartGameOptionsWidget(isBounds);

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("sgo.difficulty.level"));

		if (Maze.getInstance().getParty().size() < 6)
		{
			DIYPane warningPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
			warningPane.setBounds(
				x +border +inset,
				y +height -border -inset -buttonPaneHeight -titlePaneHeight,
				width -border*2 -inset*2,
				titlePaneHeight);
			DIYTextArea warningLabel = new DIYTextArea(StringUtil.getUiLabel("sgo.small.party.warning"));
			warningLabel.setForegroundColour(Color.WHITE);
			warningLabel.setTransparent(true);
			warningLabel.setBounds(warningPane.getBounds());
			warningPane.add(warningLabel);

			this.add(warningPane);
		}

		DIYPane buttonPane = getButtonPane();

		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);
		close = getCloseButton();
		close.addActionListener(this);

		buttonPane.add(okButton);

		this.add(titlePane);
		this.add(optionsWidget);
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
			case KeyEvent.VK_ENTER -> { e.consume(); startGame(); }
			default ->
			{
			}
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
		else if (event.getSource() == close)
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
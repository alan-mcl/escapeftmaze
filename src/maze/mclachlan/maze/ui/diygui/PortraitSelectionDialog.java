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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class PortraitSelectionDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/2;

	private final PortraitSelectionWidget portraitWidget;
	private final DIYButton okButton, close;
	private final PortraitCallback callback;
	
	/*-------------------------------------------------------------------------*/
	public PortraitSelectionDialog(
		PortraitCallback callback, 
		String startingPortrait)
	{
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;
		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		this.callback = callback;

		int border = getBorder();
		int inset = getInset();
		int titlePaneHeight = getTitlePaneHeight();
		int buttonPaneHeight = getButtonPaneHeight();

		Rectangle isBounds = new Rectangle(
			startX +border +inset,
			startY +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*2 -buttonPaneHeight -titlePaneHeight);

		portraitWidget = new PortraitSelectionWidget(isBounds);
		if (startingPortrait != null)
		{
			portraitWidget.setToPortrait(startingPortrait);
		}

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("sdw.portrait"));

		DIYPane buttonPane = getButtonPane();
		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);
		close = getCloseButton();
		close.addActionListener(this);
		
		buttonPane.add(okButton);

		this.add(titlePane);
		this.add(portraitWidget);
		this.add(buttonPane);
		this.add(close);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE -> exit();
			case KeyEvent.VK_ENTER -> setPortrait();
			case KeyEvent.VK_LEFT -> portraitWidget.previous();
			case KeyEvent.VK_RIGHT -> portraitWidget.next();
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			setPortrait();
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
	private void setPortrait()
	{
		exit();
		callback.setPortrait(portraitWidget.portraits.get(portraitWidget.currentImage));
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

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
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class GeneralOptionsDialog extends GeneralDialog implements ActionListener
{
	private final DIYButton close;
	private final DIYButton[] optionButtons;
	private final GeneralOptionsCallback callback;
	private final boolean forceSelection;

	/*-------------------------------------------------------------------------*/
	public GeneralOptionsDialog(
		GeneralOptionsCallback callback,
		boolean forceSelection,
		String title,
		String... options)
	{
		super();
		this.callback = callback;
		this.forceSelection = forceSelection;

		int buttonHeight = getButtonPaneHeight();
		int inset = getInset();
		int border = getBorder();
		int titlePaneHeight = getTitlePaneHeight();

		int dialogWidth = DiyGuiUserInterface.SCREEN_WIDTH/2;
		int dialogHeight = buttonHeight*options.length +titlePaneHeight +inset*4 +border*2;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - dialogWidth/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, dialogWidth, dialogHeight);

		this.setBounds(dialogBounds);

		DIYPane titlePane = getTitlePane(title);

		DIYPane optionsPane = new DIYPane(new DIYGridLayout(1,options.length,5,5));
		optionsPane.setBounds(
			x +border +inset,
			y +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*2 -titlePaneHeight);

		optionButtons = new DIYButton[options.length];
		for (int i=0; i<options.length; i++)
		{
			optionButtons[i] = new DIYButton(options[i]);
			optionButtons[i].addActionListener(this);
			optionsPane.add(optionButtons[i]);
		}

		this.add(titlePane);
		this.add(optionsPane);

		close = getCloseButton();
		close.addActionListener(this);

		if (!this.forceSelection)
		{
			this.add(close);
		}

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if (!forceSelection)
			{
				cancel();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
		{
			cancel();
			return true;
		}
		else
		{
			for (DIYButton b : optionButtons)
			{
				if (event.getSource() == b)
				{
					Maze.getInstance().getUi().clearDialog();
					callback.optionChosen(b.getText());
					return true;
				}
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void cancel()
	{
		Maze.getInstance().getUi().clearDialog();
		callback.optionChosen(null);
	}
}

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
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class GeneralOptionsDialog extends GeneralDialog implements ActionListener
{
	private DIYButton cancel;
	private DIYButton[] optionButtons;
	private GeneralOptionsCallback callback;

	/*-------------------------------------------------------------------------*/
	public GeneralOptionsDialog(
		GeneralOptionsCallback callback,
		String title,
		String... options)
	{
		super();
		this.callback = callback;

		int buttonHeight = 20;
		int inset = 10;
		int buttonPaneHeight = 18;

		int dialogWidth = DiyGuiUserInterface.SCREEN_WIDTH/4;
		int dialogHeight = buttonHeight*options.length +buttonPaneHeight*2 +inset*4;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - dialogWidth/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, dialogWidth, dialogHeight);

		this.setBounds(dialogBounds);

		DIYPane labelPane = new DIYPane(new DIYFlowLayout(0,0,DIYToolkit.Align.CENTER));

		labelPane.setBounds(x, y + inset, width, buttonPaneHeight);
		labelPane.add(new DIYLabel(title));

		DIYPane optionsPane = new DIYPane(new DIYGridLayout(1,options.length,5,5));
		optionsPane.setBounds(x+inset, y+buttonPaneHeight+inset,
			width-inset*2, options.length*buttonHeight);

		optionButtons = new DIYButton[options.length];
		for (int i=0; i<options.length; i++)
		{
			optionButtons[i] = new DIYButton(options[i]);
			optionButtons[i].addActionListener(this);
			optionsPane.add(optionButtons[i]);
		}

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);

		cancel = new DIYButton(StringUtil.getUiLabel("common.cancel"));
		cancel.addActionListener(this);
		buttonPane.add(cancel);

		setBackground();

		this.add(labelPane);
		this.add(optionsPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				cancel();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == cancel)
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

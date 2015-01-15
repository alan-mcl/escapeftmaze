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
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class RestingProgressDialog extends GeneralDialog
	implements ActionListener, ProgressListenerCallback
{
	private DIYButton ok;
	private DIYTextArea text;
	private FilledBarWidget progress;

	/*-------------------------------------------------------------------------*/
	public RestingProgressDialog(
		String title)
	{
		super();

		int buttonHeight = 20;
		int inset = 10;
		int buttonPaneHeight = 18;

		int dialogWidth = DiyGuiUserInterface.SCREEN_WIDTH/2-inset;
		int dialogHeight = DiyGuiUserInterface.SCREEN_WIDTH/5;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - dialogWidth/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, dialogWidth, dialogHeight);

		this.setBounds(dialogBounds);

		DIYPane titlePane = new DIYPane(new DIYFlowLayout(0,0,DIYToolkit.Align.CENTER));

		titlePane.setBounds(x, y + inset, width, buttonPaneHeight);
		DIYLabel label = new DIYLabel(title);
		label.setForegroundColour(Constants.Colour.GOLD);
		titlePane.add(label);

		DIYPane infoPane = new DIYPane(new DIYBorderLayout(5,5));
		infoPane.setBounds(x+inset, y+inset+buttonPaneHeight, width-inset*2, dialogHeight-buttonPaneHeight*3);

		progress = new FilledBarWidget(infoPane.x, infoPane.y, dialogWidth/2, buttonHeight, 0, 100);
		progress.setCallback(this);
		infoPane.add(progress, DIYBorderLayout.Constraint.NORTH);

		text = new DIYTextArea("");
		text.setTransparent(true);
		text.setAlignment(DIYToolkit.Align.CENTER);
		infoPane.add(text, DIYBorderLayout.Constraint.CENTER);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);

		ok = new DIYButton(StringUtil.getUiLabel("common.ok"));
		ok.addActionListener(this);
		ok.setEnabled(false);
		buttonPane.add(ok);

		setBackground();

		this.add(titlePane);
		this.add(infoPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == ok)
		{
			exit();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		if (ok.isEnabled())
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
			Maze.getInstance().getUi().clearDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	public ProgressListener getProgressListener()
	{
		return progress;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void callback(int progress)
	{
		if (progress == 100)
		{
			ok.setEnabled(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void message(String msg)
	{
		text.addText(msg+"\n");
	}
}

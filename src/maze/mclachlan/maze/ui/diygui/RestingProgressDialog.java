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
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYBorderLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class RestingProgressDialog extends GeneralDialog
	implements ActionListener, ProgressListenerCallback
{
	private final DIYButton close;
	private final DIYTextArea text;
	private final FilledBarWidget progress;

	/*-------------------------------------------------------------------------*/
	public RestingProgressDialog(
		String title)
	{
		super();

		int startX = DiyGuiUserInterface.SCREEN_WIDTH / 2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT / 2;
		int height = DiyGuiUserInterface.SCREEN_WIDTH / 4;

		this.setBounds(
			new Rectangle(
				startX - (startX -getInset()) /2,
				startY - height / 2,
				startX -getInset(),
				height));

		DIYPane titlePane = getTitlePane(title);

		DIYPane infoPane = new DIYPane(new DIYBorderLayout(5,5));
		infoPane.setBounds(
			x +getBorder() +getInset(),
			y +getBorder() +getInset() +getTitlePaneHeight(),
			this.width -getBorder()*2 -getInset()*2,
			this.height -getTitlePaneHeight() -getInset()*2);

		progress = new FilledBarWidget(infoPane.x, infoPane.y, infoPane.width, getButtonPaneHeight()/2, 0, 100);
		progress.setCallback(this);
		infoPane.add(progress, DIYBorderLayout.Constraint.NORTH);

		text = new DIYTextArea("");
		text.setTransparent(true);
		text.setAlignment(DIYToolkit.Align.CENTER);
		infoPane.add(text, DIYBorderLayout.Constraint.CENTER);

		close = getCloseButton();
		close.addActionListener(this);
		close.setEnabled(false);

		this.add(titlePane);
		this.add(infoPane);
		this.add(close);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE -> { e.consume(); exit(); }
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
		{
			exit();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		if (close.isEnabled())
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
			close.setEnabled(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void message(String msg)
	{
		text.addText(msg+"\n");
	}
}

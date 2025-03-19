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
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, with a scroll pane and an OK/Cancel button.
 */
public class ConfirmationDialog extends GeneralDialog implements ActionListener
{
	private final DIYButton ok, cancel, close;
	private final DIYTextArea text;

	private final ConfirmCallback callback;

	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3;

	/*-------------------------------------------------------------------------*/
	public ConfirmationDialog(String title, String labels, ConfirmCallback callback)
	{
		this.callback = callback;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle bounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);

		this.setBounds(bounds);

		DIYPane titlePane = getTitlePane(title);

		DIYPane buttonPane = getButtonPane();

		ok = new DIYButton(StringUtil.getUiLabel("common.ok"));
		ok.addActionListener(this);

		cancel = new DIYButton(StringUtil.getUiLabel("common.cancel"));
		cancel.addActionListener(this);

		close = getCloseButton();
		close.addActionListener(this);

		buttonPane.add(ok);
		buttonPane.add(cancel);
		this.add(close);

		Rectangle r = new Rectangle(
			bounds.x +getBorder() +getInset(),
			bounds.y +getBorder() +getTitlePaneHeight() +getInset(),
			bounds.width -getBorder()*2 -getInset()*2,
			bounds.height -getButtonPaneHeight() -getInset()*3 -getBorder()*2);

		text = new DIYTextArea(labels);
		text.setTransparent(true);
		text.setBounds(r);

		this.add(titlePane);
		this.add(text);
		this.add(buttonPane);

		doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text.setText(text);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}
	
	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			e.consume();
			exitDialog();
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			e.consume();
			callback.confirm();
			exitDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	protected void exitDialog()
	{
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == ok)
		{
			callback.confirm();
			exitDialog();
			return true;
		}
		else if (event.getSource() == cancel || event.getSource() == close)
		{
			exitDialog();
			return true;
		}
		return false;
	}
}

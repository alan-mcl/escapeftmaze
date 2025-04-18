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
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, no button
 */
public class TextDialogWidget extends GeneralDialog implements ActionListener
{
	private final DIYTextArea text;
	private final boolean modal;

	/*-------------------------------------------------------------------------*/
	public TextDialogWidget(String title, String text, boolean modal)
	{
		this(
			new Rectangle(
				DiyGuiUserInterface.SCREEN_WIDTH / 4,
				DiyGuiUserInterface.SCREEN_HEIGHT / 4,
				DiyGuiUserInterface.SCREEN_WIDTH / 2,
				DiyGuiUserInterface.SCREEN_HEIGHT / 2),
			title,
			text,
			modal);
	}

	/*-------------------------------------------------------------------------*/
	public TextDialogWidget(Rectangle bounds, String title, String text,
		boolean modal)
	{
		super(bounds);

		this.modal = modal;
		if (modal)
		{
			super.setStyle(Style.PANEL_HEAVY);
		}
		else
		{
			super.setStyle(Style.DIALOG);
		}

		DIYPane titlePane = null;

		Rectangle textBounds;

		if (title != null)
		{
			titlePane = getTitlePane(title);

			textBounds = new Rectangle(
				x + getInset() + getBorder(),
				y + getInset() + getBorder() + getTitlePaneHeight(),
				width - getInset() * 2 - getBorder() * 2,
				height - getInset() * 3 - getBorder() * 2 - getTitlePaneHeight());
		}
		else
		{
			textBounds = new Rectangle(
				x + getInset() + getBorder(),
				y + getInset() + getBorder(),
				width - getInset() * 2 - getBorder() * 2,
				height - getInset() * 3 - getBorder() * 2);
		}

		this.text = new DIYTextArea(text);
		this.text.setTransparent(true);
		this.text.setBounds(textBounds);

		if (!modal)
		{
			DIYButton close = getCloseButton();
			close.addActionListener(this);
			this.add(close);
		}

		if (titlePane != null)
		{
			this.add(titlePane);
		}
		this.add(this.text);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text.setText(text);
	}

	/*-------------------------------------------------------------------------*/
	public void addText(String text)
	{
		this.text.addText(text);
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return this.text.getText();
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (!modal)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
				e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				e.consume();
				exitDialog();
			}
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
		exitDialog();
		return false;
	}
}

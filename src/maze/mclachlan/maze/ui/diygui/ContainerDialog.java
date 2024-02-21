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
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, contains another widget and an ok button.
 */
public class ContainerDialog extends GeneralDialog implements ActionListener
{
	private DIYButton ok;
	private DIYTextArea text;

	/*-------------------------------------------------------------------------*/
	public ContainerDialog(String title, ContainerWidget contents, Rectangle bounds)
	{
		this.ok = new DIYButton("OK");

		this.setBounds(bounds);

		int inset = 2, border = 15;
		int buttonHeight = 17;
		int buttonWidth = bounds.width/4;

		ok.setBounds(new Rectangle(
			bounds.x+ bounds.width/2 -buttonWidth/2 -inset,
			bounds.y+ bounds.height - buttonHeight - inset - border,
			buttonWidth, buttonHeight));

		ok.addActionListener(this);

		Rectangle r = new Rectangle(
			bounds.x +inset +border,
			bounds.y +inset +border +titlePaneHeight,
			bounds.width -inset*2 -border*2,
			bounds.height -buttonHeight -inset*3 -border*2 -titlePaneHeight);

		contents.setBounds(r);
		contents.doLayout();

		DIYPane titlePane = getTitle(title);
		titlePane.doLayout();
		
		this.add(titlePane);
		this.add(contents);
		this.add(ok);

		setBackground();
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
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER)
		{
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
			exitDialog();
			return true;
		}
		return false;
	}
}

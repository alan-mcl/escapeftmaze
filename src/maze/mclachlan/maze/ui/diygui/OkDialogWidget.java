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
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;

/**
 * Generalised popup dialog, with a scroll pane and an OK button.
 */
public class OkDialogWidget extends GeneralDialog implements ActionListener
{
	private DIYButton ok;
	private DIYTextArea text;

	/*-------------------------------------------------------------------------*/
	public OkDialogWidget(Rectangle bounds, String title, String text)
	{
		super(bounds);

		int okButtonWidth, okButtonHeight = 17;
		okButtonWidth = width/4;
		this.ok = new DIYButton("OK");
		ok.setBounds(new Rectangle(
			x+ width/2 - okButtonWidth/2,
			y+ height - okButtonHeight - inset - border,
			okButtonWidth, okButtonHeight));
		
		ok.addActionListener(this);

		DIYPane titlePane = null;

		Rectangle textBounds;

		if (title != null)
		{
			titlePane = getTitle(title);

			textBounds = new Rectangle(
				x +inset +border,
				y +inset +border +titlePaneHeight,
				width -inset*2 -border*2,
				height -okButtonHeight -inset*3 -border*2 -titlePaneHeight);
		}
		else
		{
			textBounds = new Rectangle(
				x +inset +border,
				y +inset +border,
				width -inset*2 -border*2,
				height -okButtonHeight -inset*3 -border*2);
		}

		this.text = new DIYTextArea(text);
		this.text.setTransparent(true);
		this.text.setBounds(textBounds);


		setBackground();

		if (titlePane != null)
		{
			this.add(titlePane);
		}

		if (text.length() > 500)
		{
			// heuristic to add a scroll bar
			this.add(new DIYScrollPane(textBounds, this.text));
		}
		else
		{
			this.add(this.text);
		}

		this.add(ok);
		this.doLayout();
	}
	
	/*-------------------------------------------------------------------------*/
	public OkDialogWidget(Rectangle bounds)
	{
		this(bounds, "", "");
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
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
			e.getKeyCode() == KeyEvent.VK_ENTER)
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
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == ok)
		{
			exitDialog();
		}
	}
}

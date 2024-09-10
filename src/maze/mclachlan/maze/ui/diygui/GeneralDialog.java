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

import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class GeneralDialog extends DIYPanel
{
	protected static int buttonPaneHeight = 50;
	protected static int titlePaneHeight = 50;
	protected static int border = 30;
	protected static int inset = 20;

	/*-------------------------------------------------------------------------*/
	public GeneralDialog()
	{
		this.setStyle(Style.DIALOG);
	}

	/*-------------------------------------------------------------------------*/
	public GeneralDialog(Rectangle bounds)
	{
		super(bounds);
		this.setStyle(Style.DIALOG);
	}

	/*-------------------------------------------------------------------------*/
	protected void setBackground()
	{
		Image back = Database.getInstance().getImage("screen/general_dialog_back");
		this.setBackgroundImage(back);
	}

	/*-------------------------------------------------------------------------*/
	protected DIYPane getTitle(String titleText)
	{
		DIYPane titlePane = new DIYPane();
		titlePane.setBounds(
			x +border, y +border,
			width -border*2, titlePaneHeight);

		DIYLabel title = new DIYLabel(titleText, DIYToolkit.Align.CENTER);
		title.setForegroundColour(GOLD);
		title.setBounds(
			x +border, y +border,
						width -border*2, titlePaneHeight
		);

		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+5);

		title.setFont(f);

		titlePane.add(title);

		return titlePane;
	}
}

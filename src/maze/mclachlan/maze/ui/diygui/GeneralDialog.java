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
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class GeneralDialog extends DIYPanel
{
	protected static int buttonPaneHeight = 120;
	protected static int titlePaneHeight = 20;
	protected static int border = 10;
	protected static int inset = 20;

	/*-------------------------------------------------------------------------*/
	public GeneralDialog()
	{
	}

	/*-------------------------------------------------------------------------*/
	public GeneralDialog(Rectangle bounds)
	{
		super(bounds);
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
		DIYPane titlePane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
		DIYLabel title = new DIYLabel(titleText);
		titlePane.setBounds(x, y+ border, width, titlePaneHeight);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		title.setFont(f);
		titlePane.add(title);
		return titlePane;
	}
}

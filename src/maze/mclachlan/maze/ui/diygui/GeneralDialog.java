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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.RendererProperties;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 * Utility dialog super class, provides a title bar, button pane and close button
 */
public class GeneralDialog extends DIYPanel
{
	private final RendererProperties rendererProperties;

	/*-------------------------------------------------------------------------*/
	public GeneralDialog()
	{
		this.setStyle(Style.DIALOG);
		rendererProperties = DIYToolkit.getInstance().getRendererProperties();
	}

	/*-------------------------------------------------------------------------*/
	public GeneralDialog(Rectangle bounds)
	{
		super(bounds);
		this.setStyle(Style.DIALOG);
		rendererProperties = DIYToolkit.getInstance().getRendererProperties();
	}

	/*-------------------------------------------------------------------------*/
	protected int getButtonPaneHeight()
	{
		return rendererProperties.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT);
	}

	protected int getTitlePaneHeight()
	{
		return rendererProperties.getProperty(RendererProperties.Property.TITLE_PANE_HEIGHT);
	}

	protected int getBorder()
	{
		return rendererProperties.getProperty(RendererProperties.Property.DIALOG_BORDER);
	}

	protected int getInset()
	{
		return rendererProperties.getProperty(RendererProperties.Property.INSET);
	}

	/*-------------------------------------------------------------------------*/
	protected DIYPane getTitlePane(String titleText)
	{
		return getTitlePane(titleText, GOLD);
	}

	/*-------------------------------------------------------------------------*/
	protected DIYPane getTitlePane(String titleText, Color titleCol)
	{
		DIYPane titlePane = new DIYPane();
		titlePane.setBounds(
			x + getBorder(),
			y + getBorder(),
			width - getBorder() * 2,
			getTitlePaneHeight());

		DIYLabel title = new DIYLabel(titleText, DIYToolkit.Align.CENTER);
		title.setForegroundColour(titleCol);
		title.setBounds(
			x + getBorder(), y + getBorder(),
			width - getBorder() * 2, getTitlePaneHeight());

		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize() + 5);

		title.setFont(f);

		titlePane.add(title);

		return titlePane;
	}

	/*-------------------------------------------------------------------------*/
	protected DIYPane getButtonPane()
	{
		DIYPane result = new DIYPane(new DIYFlowLayout(getInset(), 0, DIYToolkit.Align.CENTER));

		result.setBounds(
			x +getBorder() +getInset(),
			y +height -getBorder() -getInset() -getButtonPaneHeight(),
			width -getBorder()*2 -getInset()*2,
			getButtonPaneHeight());

		return result;
	}

	/*-------------------------------------------------------------------------*/

	protected DIYButton getCloseButton()
	{
		final DIYButton close;
		close = new DIYButton(null);
		close.setImage("icon/close");
		close.setBounds(x + width - 45, y, 45, 45);
		return close;
	}

}

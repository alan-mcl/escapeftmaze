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
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.script.FlavourTextEvent;

public class FlavourTextDialog extends GeneralDialog implements ActionListener
{
	private static final int NARROW_DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH / 3;
	private static final int WIDE_DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH / 2;
	private static final int MAX_DIALOG_HEIGHT =
		DiyGuiUserInterface.SCREEN_HEIGHT - DiyGuiUserInterface.SCREEN_EDGE_INSET * 2;

	private final DIYTextArea text;
	private DIYScrollPane scrollPane;

	/*-------------------------------------------------------------------------*/
	public FlavourTextDialog(
		String title,
		String text,
		FlavourTextEvent.Alignment alignment)
	{
		super.setStyle(Style.PANEL_MED);

		int border = getBorder();
		int titlePaneHeight = title != null ? getTitlePaneHeight() : 0;
		int maxContentHeight = MAX_DIALOG_HEIGHT - border * 2 - titlePaneHeight;
		int lineHeight = (int)DIYToolkit.getDimension("|").getHeight();

		int narrowTextWidth = NARROW_DIALOG_WIDTH - border * 2;
		int narrowPreferredHeight = measurePreferredTextHeight(text, narrowTextWidth, lineHeight);

		int dialogWidth;
		int textBoundsWidth;
		int preferredTextHeight;
		int contentHeight;
		boolean useScroll;

		if (narrowPreferredHeight <= maxContentHeight)
		{
			dialogWidth = NARROW_DIALOG_WIDTH;
			textBoundsWidth = narrowTextWidth;
			preferredTextHeight = narrowPreferredHeight;
			contentHeight = preferredTextHeight;
			useScroll = false;
		}
		else
		{
			dialogWidth = WIDE_DIALOG_WIDTH;
			textBoundsWidth = dialogWidth - border * 2;
			preferredTextHeight = measurePreferredTextHeight(text, textBoundsWidth, lineHeight);
			contentHeight = Math.min(preferredTextHeight, maxContentHeight);
			useScroll = preferredTextHeight > contentHeight;
		}

		int dialogHeight = contentHeight + border * 2 + titlePaneHeight;

		if (alignment == null)
		{
			alignment = FlavourTextEvent.Alignment.CENTER;
		}

		int startX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - dialogWidth / 2;
		int startY = switch (alignment)
			{
				case TOP -> DiyGuiUserInterface.SCREEN_HEIGHT / 2 - DiyGuiUserInterface.MAZE_HEIGHT / 2;
				case CENTER -> DiyGuiUserInterface.SCREEN_HEIGHT / 2 - dialogHeight / 2;
				case BOTTOM -> DiyGuiUserInterface.MAZE_WINDOW_BOUNDS.y + DiyGuiUserInterface.MAZE_HEIGHT
					- DiyGuiUserInterface.SCREEN_EDGE_INSET - dialogHeight;
			};

		int minY = DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int maxY = DiyGuiUserInterface.SCREEN_HEIGHT - dialogHeight - DiyGuiUserInterface.SCREEN_EDGE_INSET;
		startY = Math.max(minY, Math.min(startY, maxY));

		this.setBounds(startX, startY, dialogWidth, dialogHeight);

		Rectangle contentBounds = new Rectangle(
			x + border,
			y + border + titlePaneHeight,
			textBoundsWidth,
			contentHeight);

		this.text = new DIYTextArea(text);
		this.text.setTransparent(true);
		this.text.addActionListener(this);

		if (title != null)
		{
			this.add(getTitlePane(title));
		}

		if (useScroll)
		{
			this.text.width = textBoundsWidth;
			this.text.setBounds(
				this.text.x,
				this.text.y,
				textBoundsWidth,
				this.text.getPreferredSize().height);
			scrollPane = new DIYScrollPane(contentBounds, this.text);
			this.add(scrollPane);
		}
		else
		{
			this.text.setBounds(contentBounds);
			this.add(this.text);
		}

		this.doLayout();
	}

	private static int measurePreferredTextHeight(String text, int textBoundsWidth, int lineHeight)
	{
		List<String> lines = DIYToolkit.wrapText(
			text,
			textBoundsWidth - 2,
			null);
		return lines.size() * lineHeight;
	}

	/*-------------------------------------------------------------------------*/
	private void exitDialog()
	{
		synchronized (Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text.setText(text);
		if (scrollPane != null)
		{
			scrollPane.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			e.consume();
			exitDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseClicked(MouseEvent e)
	{
		exitDialog();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		exitDialog();
		return true;
	}
}

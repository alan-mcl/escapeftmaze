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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;

/**
 */
public class MapDisplayDialog extends GeneralDialog implements ActionListener
{
	private static final int XX = 25, YY = 25;

	private final MapDisplayWidget mapWidget;
	private final DIYButton close, zoomIn, zoomOut;

	/*-------------------------------------------------------------------------*/
	public MapDisplayDialog()
	{
		super(new Rectangle(
			XX,
			YY,
			DiyGuiUserInterface.SCREEN_WIDTH-XX*2,
			DiyGuiUserInterface.SCREEN_HEIGHT-YY*2));

		Zone zone = Maze.getInstance().getCurrentZone();

		int inset = getInset();
		int border = getBorder();
		int buttonPaneHeight = getButtonPaneHeight();
		int titlePaneHeight = getTitlePaneHeight();

		Rectangle mapBounds = new Rectangle(
			XX +border +inset,
			YY +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*3 -titlePaneHeight -buttonPaneHeight);

		mapWidget = new MapDisplayWidget();
		mapWidget.setBounds(mapBounds);

		DIYPane buttonPane = getButtonPane();


		zoomIn = new DIYButton("Zoom In (+)");
		zoomIn.addActionListener(this);

		zoomOut = new DIYButton("Zoom Out (-)");
		zoomOut.addActionListener(this);
		
		buttonPane.add(zoomIn);
		buttonPane.add(zoomOut);

		DIYPane title = getTitlePane(zone.getName());

		close = getCloseButton();
		close.addActionListener(this);

		this.add(title);
		this.add(mapWidget);
		this.add(buttonPane);
		this.add(close);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
		{
			exit();
			return true;
		}
		else if (event.getSource() == zoomIn)
		{
			zoomIn();
			return true;
		}
		else if (event.getSource() == zoomOut)
		{
			zoomOut();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void zoomOut()
	{
		mapWidget.zoomOut();
	}

	/*-------------------------------------------------------------------------*/
	private void zoomIn()
	{
		mapWidget.zoomIn();
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		DIYToolkit.getInstance().clearDialog(this);
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> exit();
			case KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS ->
				zoomIn();
			case KeyEvent.VK_PAGE_UP, KeyEvent.VK_MINUS -> zoomOut();
		}
	}
}

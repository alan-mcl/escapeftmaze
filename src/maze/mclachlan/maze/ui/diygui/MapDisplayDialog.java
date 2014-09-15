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
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;

/**
 */
public class MapDisplayDialog extends GeneralDialog implements ActionListener
{
	private static int XX = 50;
	private static int YY = 50;
	private static Rectangle bounds = new Rectangle(XX, YY,
		DiyGuiUserInterface.SCREEN_WIDTH-100, DiyGuiUserInterface.SCREEN_HEIGHT-100);

	private static int buttonPaneHeight = 20;
	private static int inset = 10;

	private static Rectangle mapBounds = new Rectangle(
		XX+inset,
		YY+inset +titlePaneHeight,
		bounds.width -inset*2,
		bounds.height -buttonPaneHeight*2 -inset*2 -titlePaneHeight);

	private MapDisplayWidget mapWidget;
	private DIYButton okButton, zoomIn, zoomOut;

	/*-------------------------------------------------------------------------*/
	public MapDisplayDialog()
	{
		super(bounds);

		Zone zone = Maze.getInstance().getZone();

		mapWidget = new MapDisplayWidget();
		mapWidget.setBounds(mapBounds);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height-buttonPaneHeight-inset, width, buttonPaneHeight);

		okButton = new DIYButton("Exit");
		okButton.addActionListener(this);

		zoomIn = new DIYButton("Zoom In (+)");
		zoomIn.addActionListener(this);

		zoomOut = new DIYButton("Zoom Out (-)");
		zoomOut.addActionListener(this);
		
		buttonPane.add(zoomIn);
		buttonPane.add(zoomOut);
		buttonPane.add(okButton);

		DIYPane title = getTitle(zone.getName());

		setBackground();

		this.add(title);
		this.add(mapWidget);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			exit();
		}
		else if (event.getSource() == zoomIn)
		{
			zoomIn();
		}
		else if (event.getSource() == zoomOut)
		{
			zoomOut();
		}
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

		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				exit();
				break;
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_EQUALS:
				zoomIn();
				break;
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_MINUS:
				zoomOut();
				break;
		}
	}
}

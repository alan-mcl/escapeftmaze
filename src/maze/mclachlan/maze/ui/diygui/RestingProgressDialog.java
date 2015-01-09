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
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.PlayerParty;

/**
 *
 */
public class RestingProgressDialog extends GeneralDialog implements ActionListener
{
	private DIYButton ok;
	private FilledBarWidget progress;

	/*-------------------------------------------------------------------------*/
	public RestingProgressDialog(
		String title)
	{
		super();

		int buttonHeight = 20;
		int inset = 10;
		int buttonPaneHeight = 18;

		int dialogWidth = DiyGuiUserInterface.SCREEN_WIDTH/2;
		int dialogHeight = DiyGuiUserInterface.SCREEN_WIDTH/6;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - dialogWidth/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, dialogWidth, dialogHeight);

		this.setBounds(dialogBounds);

		Tile tile = Maze.getInstance().getCurrentTile();
		PlayerParty party = Maze.getInstance().getParty();

		DIYPane titlePane = new DIYPane(new DIYFlowLayout(0,0,DIYToolkit.Align.CENTER));

		titlePane.setBounds(x, y + inset, width, buttonPaneHeight);
		titlePane.add(new DIYLabel(title));

		DIYPane infoPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
		infoPane.setBounds(x+inset, y+inset+buttonPaneHeight, width, dialogHeight-buttonPaneHeight*3);

		progress = new FilledBarWidget(infoPane.x, infoPane.y, dialogWidth/2, buttonHeight, 0, 100);

		infoPane.add(progress);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);

		ok = new DIYButton(StringUtil.getUiLabel("common.ok"));
		ok.addActionListener(this);
		buttonPane.add(ok);

		setBackground();

		this.add(titlePane);
		this.add(infoPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == ok)
		{
			exit();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		if (ok.isEnabled())
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
			Maze.getInstance().getUi().clearDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	public ProgressListener getProgressListener()
	{
		return progress;
	}
}

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

import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.SetStateEvent;
import mclachlan.maze.map.script.Chest;

/**
 *
 */
public class EncounterChestStateHandler implements ActionListener
{
	private final Maze maze;
	private final int buttonRows;
	private final int inset;
	private final MessageDestination msg;

	private DIYButton leave;
	private DIYPane result;
	private Chest chest;

	/*-------------------------------------------------------------------------*/
	public EncounterChestStateHandler(Maze maze, int buttonRows, int inset,
		MessageDestination msg)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		this.msg = msg;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getStateHandlerPane()
	{
		result = new DIYPane(new DIYGridLayout(4, buttonRows, inset, inset));

		leave = new DIYButton(StringUtil.getUiLabel("poatw.leave"));
		leave.setTooltip(StringUtil.getUiLabel("poatw.leave.tooltip"));
		leave.addActionListener(this);

		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());

		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(leave);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getRightPane()
	{
		return new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// encounter actor options
		if (obj == leave)
		{
			leave();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void leave()
	{
		if (leave.isVisible())
		{
			maze.appendEvents(new SetStateEvent(maze, Maze.State.MOVEMENT));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_L -> leave();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setChest(Chest chest)
	{
		this.chest = chest;
		msg.setHeader(
			StringUtil.getUiLabel(
				"poatw.chest.encounter"));

		if (leave.isVisible()) result.add(leave);

		result.doLayout();
	}
}

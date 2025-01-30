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
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.UserInterface;

/**
 *
 */
public class MovementStateHandler implements ActionListener, FormationCallback
{
	private final Maze maze;
	private final UserInterface ui;

	private final int buttonRows;
	private final int inset;

	private DIYButton rest, formation;

	/*-------------------------------------------------------------------------*/
	public MovementStateHandler(Maze maze, int buttonRows, int inset)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		ui = DiyGuiUserInterface.instance;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getStateHandlerPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(4, buttonRows, inset, inset));

		rest = new DIYButton(StringUtil.getUiLabel("poatw.rest"));
		rest.setTooltip(StringUtil.getUiLabel("poatw.rest.tooltip"));
		rest.addActionListener(this);

		formation = new DIYButton(StringUtil.getUiLabel("poatw.formation"));
		formation.setTooltip(StringUtil.getUiLabel("poatw.formation.tooltip"));
		formation.addActionListener(this);

//		hide = new DIYButton(StringUtil.getUiLabel("poatw.hide"));
//		hide.addActionListener(this);

//		locks = new DIYButton(StringUtil.getUiLabel("poatw.open"));
//		locks.addActionListener(this);
//
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());

		result.add(rest);
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(formation);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// movement options
		if (obj == formation)
		{
			formation();
			return true;
		}
//		else if (obj == hide)
//		{
//			hide();
//			return true;
//		}
//		else if (obj == locks)
//		{
//			open();
//			return true;
//		}
		else if (obj == rest)
		{
			rest();
			return true;
		}
//		else if (obj == map)
//		{
//			showMap();
//			return true;
//		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void formation()
	{
		if (formation.isVisible())
		{
			FormationDialog formationDialog = new FormationDialog(this);
			maze.getUi().showDialog(formationDialog);
		}
	}

	public void stats()
	{
		maze.setState(Maze.State.STATSDISPLAY);
	}

	public void rest()
	{
		if (maze.processPlayerAction(
			TileScript.PlayerAction.REST,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing()))
		{
			maze.setState(Maze.State.RESTING);
		}
	}

	public void open()
	{
		maze.processPlayerAction(
			TileScript.PlayerAction.LOCKS,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void formationChanged(List<PlayerCharacter> actors, int formation)
	{
		maze.reorderParty(actors, formation);
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_1 ->
			{
				ui.characterSelected(0);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_2 ->
			{
				ui.characterSelected(1);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_3 ->
			{
				ui.characterSelected(2);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_4 ->
			{
				ui.characterSelected(3);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_5 ->
			{
				ui.characterSelected(4);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_6 ->
			{
				ui.characterSelected(5);
				Maze.getInstance().setState(Maze.State.INVENTORY);
			}
			case KeyEvent.VK_R -> rest();
			case KeyEvent.VK_F -> formation();
//			case KeyEvent.VK_O -> open();
//			case KeyEvent.VK_H -> hide();
		}

	}
}

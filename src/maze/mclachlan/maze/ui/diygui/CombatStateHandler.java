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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.TerminateGameEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class CombatStateHandler implements ActionListener, ConfirmCallback, FormationCallback
{
	private final Maze maze;
	private final int buttonRows;
	private final int inset;
	private final MessageDestination messageDestination;

	private DIYButton startRound, terminateGame, formation;

	/*-------------------------------------------------------------------------*/
	public CombatStateHandler(Maze maze, int buttonRows, int inset,
		MessageDestination messageDestination)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		this.messageDestination = messageDestination;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getLeftPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		startRound = new DIYButton(StringUtil.getUiLabel("poatw.start.round"));
		startRound.addActionListener(this);

		formation = new DIYButton(StringUtil.getUiLabel("poatw.formation"));
		formation.addActionListener(this);

		result.add(startRound);
		result.add(formation);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getRightPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		terminateGame = new DIYButton(StringUtil.getUiLabel("poatw.terminate.game"));
		terminateGame.addActionListener(this);

		result.add(terminateGame);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// combat options
		if (obj == startRound)
		{
			startRound();
		}
		else if (obj == formation)
		{
			formation();
		}
		else if (obj == terminateGame)
		{
			terminateGame();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void startRound()
	{
		if (startRound.isVisible())
		{
			// transparent modal dialog to block all user input while combat runs
			maze.getUi().showDialog(new DIYPane(DiyGuiUserInterface.SCREEN_BOUNDS));

			maze.executeCombatRound(maze.getCurrentCombat());
		}
	}

	public void terminateGame()
	{
		if (terminateGame.isVisible())
		{
			maze.appendEvents(new TerminateGameEvent(maze));
		}
	}

	public void formation()
	{
		if (formation.isVisible())
		{
			FormationDialog formationDialog = new FormationDialog(this);
			maze.getUi().showDialog(formationDialog);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentCombat(Combat currentCombat)
	{
		startRound.setVisible(true);
		terminateGame.setVisible(true);
		formation.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void confirm()
	{
		// confirming the exit to main
		maze.backToMain();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void formationChanged(List<PlayerCharacter> actors, int formation)
	{
		messageDestination.addMessage(StringUtil.getUiLabel("cow.formation.changed"));
		maze.setPendingFormationChanges(actors, formation);
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_S: startRound(); break;
			case KeyEvent.VK_F: formation(); break;
		}
	}
}

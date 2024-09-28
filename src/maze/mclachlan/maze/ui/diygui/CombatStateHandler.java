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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.DefendOption;

/**
 *
 */
public class CombatStateHandler implements ActionListener, ConfirmCallback, FormationCallback
{
	private final Maze maze;
	private final int buttonRows;
	private final int inset;
	private final MessageDestination messageDestination;

	private DIYButton startRound, formation, defendAll;

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
	public ContainerWidget getStateHandlerPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(4, buttonRows, inset, inset));

		startRound = new DIYButton(StringUtil.getUiLabel("poatw.start.round"));
		startRound.setTooltip(StringUtil.getUiLabel("poatw.start.round.tooltip"));
		startRound.addActionListener(this);

		formation = new DIYButton(StringUtil.getUiLabel("poatw.formation"));
		formation.setTooltip(StringUtil.getUiLabel("poatw.formation.tooltip.combat"));
		formation.addActionListener(this);

		defendAll = new DIYButton(StringUtil.getUiLabel("poatw.defend.all"));
		defendAll.setTooltip(StringUtil.getUiLabel("poatw.defend.all.tooltip"));
		defendAll.addActionListener(this);

		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());
		result.add(new DIYLabel());

		result.add(startRound);
		result.add(new DIYLabel());
		result.add(defendAll);
		result.add(formation);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// combat options
		if (obj == startRound)
		{
			startRound();
			return true;
		}
		else if (obj == formation)
		{
			formation();
			return true;
		}
		else if (obj == defendAll)
		{
			defendAll();
			return true;
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void startRound()
	{
		if (startRound.isVisible())
		{
			maze.getUi().disableInput();
			maze.executeCombatRound(maze.getCurrentCombat());
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

	public void defendAll()
	{
		if (defendAll.isVisible())
		{
			for (PlayerCharacter pc : maze.getParty().getPlayerCharacters())
			{
				maze.getUi().setPlayerCharacterActionOption(pc, DefendOption.class);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentCombat(Combat currentCombat)
	{
		startRound.setVisible(true);
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
			case KeyEvent.VK_ENTER, KeyEvent.VK_S -> startRound();
			case KeyEvent.VK_F -> formation();
			case KeyEvent.VK_D -> defendAll();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setEnabled(boolean b)
	{
		startRound.setEnabled(b);
		formation.setEnabled(b);
		defendAll.setEnabled(b);
	}
}

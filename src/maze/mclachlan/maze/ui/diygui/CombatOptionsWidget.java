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

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.ui.UserInterface;

public class CombatOptionsWidget extends ContainerWidget
	implements FormationCallback
{
	private PlayerCharacter playerCharacter;

	// party options
	private DIYButton formation, startRound, terminateGame;

	// evasion options
	private DIYButton evade, surprise;

	private CardLayoutWidget cards;
	private DIYPane finalOptions;
	private DIYPane characterOptions;
	private DIYPane evasionOptions;

	/*-------------------------------------------------------------------------*/
	public CombatOptionsWidget(Rectangle bounds, PlayerCharacter actor)
	{
		super(bounds);
		this.playerCharacter = actor;
		ActionListener listener = new CombatOptionsActionListener();

		int buttonCols = 4;
		int inset = MovementOptionsWidget.INSET;
		int buttonHeight = MovementOptionsWidget.BUTTON_HEIGHT;
		int buttonRows = height/buttonHeight;

		characterOptions = new DIYPane(
					new DIYGridLayout(buttonCols, buttonRows, inset, inset));
		characterOptions.setInsets(new Insets(inset, inset, inset, inset));

		formation = new DIYButton(StringUtil.getUiLabel("cow.formation"));
		formation.addActionListener(listener);

		characterOptions.add(formation);

		finalOptions = new DIYPane(new DIYGridLayout(3, buttonRows, inset, inset));

		startRound = new DIYButton(StringUtil.getUiLabel("cow.start.round"));
		startRound.addActionListener(listener);

		terminateGame = new DIYButton(StringUtil.getUiLabel("cow.terminate.game"));
		terminateGame.addActionListener(listener);


		finalOptions.add(new DIYLabel());
		finalOptions.add(startRound);
		finalOptions.add(new DIYLabel());

		finalOptions.add(new DIYLabel());
		finalOptions.add(formation);
		finalOptions.add(new DIYLabel());

		finalOptions.add(new DIYLabel());
		finalOptions.add(terminateGame);
		finalOptions.add(new DIYLabel());

		evasionOptions = new DIYPane(new DIYGridLayout(3, buttonRows, inset, inset));

		evade = new DIYButton(StringUtil.getUiLabel("cow.evade"));
		evade.addActionListener(listener);

		surprise = new DIYButton(StringUtil.getUiLabel("cow.surprise"));
		surprise.addActionListener(listener);

		evasionOptions.add(new DIYLabel());
		evasionOptions.add(evade);
		evasionOptions.add(new DIYLabel());
		evasionOptions.add(new DIYLabel());
		evasionOptions.add(surprise);

		characterOptions.setBounds(bounds);
		finalOptions.setBounds(bounds);
		evasionOptions.setBounds(bounds);

		ArrayList<ContainerWidget> widgets = new ArrayList<ContainerWidget>();
		widgets.add(evasionOptions);
		widgets.add(characterOptions);
		widgets.add(finalOptions);

		cards = new CardLayoutWidget(bounds, widgets);
		this.add(cards);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	private void finished(UserInterface.CombatOption option)
	{
		DiyGuiUserInterface.instance.setCombatOption(option);

		synchronized (DiyGuiUserInterface.instance.combatOptionsMutex)
		{
			DiyGuiUserInterface.instance.combatOptionsMutex.notifyAll();
		}
	}

	/*-------------------------------------------------------------------------*/
	void showEvasionOptions()
	{
		this.cards.show(evasionOptions);
	}

	/*-------------------------------------------------------------------------*/
	public void showFinalCombatOptions()
	{
		this.cards.show(finalOptions);
	}

	/*-------------------------------------------------------------------------*/
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (cards != null)
		{
			cards.setBounds(x, y, width, height);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		if (cards.getCurrentWidget() == evasionOptions)
		{
			switch (keyCode)
			{
				case KeyEvent.VK_S: surprise(); break;
				case KeyEvent.VK_E: evade(); break;
			}
		}
		else if (cards.getCurrentWidget() == characterOptions)
		{
			switch (keyCode)
			{
				case KeyEvent.VK_1: Maze.getInstance().getUi().setSelectedFoeGroup(0); break;
				case KeyEvent.VK_2: Maze.getInstance().getUi().setSelectedFoeGroup(1);  break;
				case KeyEvent.VK_3: Maze.getInstance().getUi().setSelectedFoeGroup(2);  break;
				case KeyEvent.VK_4: Maze.getInstance().getUi().setSelectedFoeGroup(3);  break;
				case KeyEvent.VK_5: Maze.getInstance().getUi().setSelectedFoeGroup(4);  break;
				case KeyEvent.VK_6: Maze.getInstance().getUi().setSelectedFoeGroup(5);  break;
				case KeyEvent.VK_7: Maze.getInstance().getUi().setSelectedFoeGroup(6);  break;
				case KeyEvent.VK_8: Maze.getInstance().getUi().setSelectedFoeGroup(7);  break;
				case KeyEvent.VK_9: Maze.getInstance().getUi().setSelectedFoeGroup(8);  break;
				case KeyEvent.VK_0: Maze.getInstance().getUi().setSelectedFoeGroup(9);  break;
//				case KeyEvent.VK_A: attack(); break;
//				case KeyEvent.VK_D: defend(); break;
//				case KeyEvent.VK_C: spell(); break;
//				case KeyEvent.VK_U: item(); break;
//				case KeyEvent.VK_H: hide(); break;
//				case KeyEvent.VK_E: equip(); break;
//				case KeyEvent.VK_N: run(); break;
				case KeyEvent.VK_BACK_SPACE:
//				case KeyEvent.VK_K: back(); break;
				case KeyEvent.VK_ESCAPE:
//				case KeyEvent.VK_L: cancel(); break;
				case KeyEvent.VK_F: formation(); break;
//				case KeyEvent.VK_R: repeat(); break;
//				case KeyEvent.VK_W: swapWeapons(); break;
			}
		}
		else if (cards.getCurrentWidget() == finalOptions)
		{
			switch (keyCode)
			{
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_S: startRound(); break;
				case KeyEvent.VK_BACK_SPACE:
//				case KeyEvent.VK_K: back(); break;
				case KeyEvent.VK_ESCAPE:
//				case KeyEvent.VK_L: cancel(); break;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void surprise()
	{
		if (surprise.isEnabled())
			finished(UserInterface.CombatOption.SURPRISE_FOES);
	}

	public void evade()
	{
		if (evade.isEnabled())
			finished(UserInterface.CombatOption.EVADE_FOES);
	}

	public void startRound()
	{
		if (startRound.isEnabled())
			finished(UserInterface.CombatOption.START_ROUND);
	}

	public void displace(PlayerCharacter other, int targetIndex)
	{
		playerCharacter.getCombatantData().setDisplaced(true);

		PlayerParty playerParty = Maze.getInstance().getParty();
		List<PlayerCharacter> newParty = new ArrayList<PlayerCharacter>(playerParty.getPlayerCharacters());

		int displacerIndex = playerParty.getPlayerCharacterIndex(this.playerCharacter);
		newParty.set(targetIndex, this.playerCharacter);
		newParty.set(displacerIndex, other);

		Maze.getInstance().reorderParty(newParty, playerParty.getFormation());
		finished(UserInterface.CombatOption.CANCEL);
	}

	public void formation()
	{
		if (formation.isEnabled())
		{
			FormationDialog formationDialog = new FormationDialog(this);
			Maze.getInstance().getUi().showDialog(formationDialog);
		}
	}

	public void formationChanged(List<PlayerCharacter> actors, int formation)
	{
		DiyGuiUserInterface.instance.addMessage(
			StringUtil.getUiLabel("cow.formation.changed"));
		Maze.getInstance().setPendingFormationChanges(actors, formation);
	}
	
	public void terminateGame()
	{
		finished(UserInterface.CombatOption.TERMINATE_GAME);
	}

	/*-------------------------------------------------------------------------*/
	private class CombatOptionsActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			Object obj = event.getSource();
			if (obj == startRound)
			{
				startRound();
			}
			else if (obj == evade)
			{
				evade();
			}
			else if (obj == surprise)
			{
				surprise();
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
	}
}

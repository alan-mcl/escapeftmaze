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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;

/**
 *
 */
public class GuildDisplayDialogForNpc extends GeneralDialog
	implements ActionListener, ChooseCharacterCallback
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private GuildDisplayWidget gdWidget;
	private DIYButton addToParty, removeFromParty, createCharacter, exit;
	private GuildCallback guildCallback;
	private int recruitPrice;
	private int createPrice;
	private DIYLabel partyGoldLabel = new DIYLabel();

	/*-------------------------------------------------------------------------*/
	public GuildDisplayDialogForNpc(
		Foe npc,
		GuildCallback guildCallback)
	{
		this.guildCallback = guildCallback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		int buttonPaneHeight = 125;
		int inset = 10;
		int titlePaneHeight = 20;
		Rectangle isBounds = new Rectangle(startX+ inset, startY+ inset + titlePaneHeight,
			DIALOG_WIDTH- inset *2, DIALOG_HEIGHT- buttonPaneHeight - titlePaneHeight - inset *4);
		
		this.setBounds(dialogBounds);

		List<PlayerCharacter> list = new ArrayList<PlayerCharacter>();
		for (String name : npc.getGuild())
		{
			list.add(Maze.getInstance().getPlayerCharacters().get(name));
		}
			
		gdWidget = new GuildDisplayWidget(isBounds, list);

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("gdd.title", npc.getDisplayName()));

		DIYPane buttonPane = new DIYPane(new DIYGridLayout(2, 5, 5, 5));
		buttonPane.setInsets(new Insets(5,20,5,20));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		exit = new DIYButton(StringUtil.getUiLabel("common.exit"));
		exit.addActionListener(this);
		
		addToParty = new DIYButton(StringUtil.getUiLabel("gdd.add.to.party"));
		addToParty.addActionListener(this);
		
		removeFromParty = new DIYButton(StringUtil.getUiLabel("gdd.remove.from.party"));
		removeFromParty.addActionListener(this);
		
		createCharacter = new DIYButton(StringUtil.getUiLabel("gdd.create.character"));
		createCharacter.addActionListener(this);

		recruitPrice = GameSys.getInstance().getRecruitCharacterCost()*npc.getSellsAt()/100;
		createPrice = GameSys.getInstance().getCreateCharacterCost()*npc.getSellsAt()/100;

		buttonPane.add(addToParty);
		buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", recruitPrice), DIYToolkit.Align.LEFT));
		buttonPane.add(removeFromParty);
		buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", recruitPrice), DIYToolkit.Align.LEFT));
		buttonPane.add(createCharacter);
		buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", createPrice), DIYToolkit.Align.LEFT));
		buttonPane.add(partyGoldLabel);
		buttonPane.add(new DIYLabel());
		buttonPane.add(exit);
		buttonPane.add(new DIYLabel(""));

		this.add(titlePane);
		this.add(gdWidget);
		this.add(buttonPane);
		this.doLayout();
		
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	void refresh()
	{
		PlayerParty party = Maze.getInstance().getParty();
		int partyGold = party.getGold();

		partyGoldLabel.setText(StringUtil.getUiLabel("gdd.party.gold", partyGold));
		
		addToParty.setEnabled(party.size() < 6 && partyGold >= recruitPrice);
		removeFromParty.setEnabled(party.size() > 1 && partyGold >= recruitPrice);
		createCharacter.setEnabled(partyGold >= createPrice);
		gdWidget.refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				exit();
				break;
			case KeyEvent.VK_A:
				addToParty();
				break;
			case KeyEvent.VK_R:
				removeFromParty();
				break;
			case KeyEvent.VK_C:
				createCharacter();
				break;
			default:
				gdWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == exit)
		{
			exit();
			return true;
		}
		else if (event.getSource() == addToParty)
		{
			addToParty();
			return true;
		}
		else if (event.getSource() == removeFromParty)
		{
			removeFromParty();
			return true;
		}
		else if (event.getSource() == createCharacter)
		{
			createCharacter();
			return true;
		}

		return false;
	}

	private void createCharacter()
	{
		guildCallback.createCharacter(createPrice);
	}

	private void removeFromParty()
	{
		DiyGuiUserInterface.instance.chooseACharacter(this);
	}

	private void addToParty()
	{
		PlayerCharacter pc = gdWidget.getSelected();
		if (pc != null)
		{
			guildCallback.transferPlayerCharacterToParty(pc, recruitPrice);
			gdWidget.remove(pc);
			refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		// assume it is the "remove character from party" callback
		PlayerParty party = Maze.getInstance().getParty();
		if (party != null && party.size() > 1)
		{
			guildCallback.removeFromParty(pc, recruitPrice);
			gdWidget.add(pc);
			refresh();
		}

		return true;
	}
}
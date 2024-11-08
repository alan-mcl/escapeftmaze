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
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;

/**
 *
 */
public class GuildDisplayDialog extends GeneralDialog
	implements ActionListener, ChooseCharacterCallback
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3*2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private final GuildDisplayWidget gdWidget;
	private final DIYButton addToParty, removeFromParty, createCharacter, close;
	private final GuildCallback guildCallback;
	private final int recruitPrice;
	private final int createPrice;
	private final DIYLabel partyGoldLabel;

	private final Mode mode;

	public enum Mode
	{
		MAIN_MENU,
		NPC
	}

	/*-------------------------------------------------------------------------*/
	public GuildDisplayDialog(
		Mode mode,
		String title,
		List<PlayerCharacter> guild,
		int costMult,
		GuildCallback guildCallback)
	{
		this.mode = mode;

		this.guildCallback = guildCallback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		int buttonPaneHeight = getButtonPaneHeight();
		int inset = getInset();
		int titlePaneHeight = getTitlePaneHeight();
		int border = getBorder();

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		Rectangle isBounds = new Rectangle(
			startX +border +inset,
			startY +border +inset +titlePaneHeight,
			DIALOG_WIDTH -inset*2 -border*2,
			DIALOG_HEIGHT -buttonPaneHeight -titlePaneHeight -inset*4 -border*2);
		
		this.setBounds(dialogBounds);

		gdWidget = new GuildDisplayWidget(isBounds, guild);

		DIYPane titlePane = getTitlePane(title);

		DIYPane buttonPane = getButtonPane();

		close = getCloseButton();
		close.addActionListener(this);

		addToParty = new DIYButton(StringUtil.getUiLabel("gdd.add.to.party"));
		addToParty.addActionListener(this);

		removeFromParty = new DIYButton(StringUtil.getUiLabel("gdd.remove.from.party"));
		removeFromParty.addActionListener(this);

		createCharacter = new DIYButton(StringUtil.getUiLabel("gdd.create.character"));
		createCharacter.addActionListener(this);

		recruitPrice = GameSys.getInstance().getRecruitCharacterCost()*costMult/100;
		createPrice = GameSys.getInstance().getCreateCharacterCost()*costMult/100;

		partyGoldLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
		partyGoldLabel.setBounds(
			x +border +inset,
			y +border +inset,
			100,
			25);

		if (mode == Mode.NPC)
		{
			buttonPane.setLayoutManager(new DIYGridLayout(3, 2, 5, 5));
			buttonPane.setBounds(
				buttonPane.x,
				buttonPane.y -buttonPaneHeight,
				buttonPane.width,
				buttonPaneHeight*2);

			isBounds.height -= buttonPaneHeight;

			gdWidget.setBounds(isBounds);
		}

		buttonPane.add(addToParty);
		buttonPane.add(removeFromParty);

		if (mode == Mode.NPC)
		{
			buttonPane.add(createCharacter);
			buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", recruitPrice), DIYToolkit.Align.CENTER));
			buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", recruitPrice), DIYToolkit.Align.CENTER));
			buttonPane.add(new DIYLabel(StringUtil.getUiLabel("gdd.cost", createPrice), DIYToolkit.Align.CENTER));
			this.add(partyGoldLabel);
		}

		this.add(titlePane);
		this.add(gdWidget);
		this.add(buttonPane);
		this.add(close);

		this.doLayout();
		
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	void refresh()
	{
		if (mode == Mode.NPC)
		{
			PlayerParty party = Maze.getInstance().getParty();
			int partyGold = party.getGold();

			partyGoldLabel.setText(StringUtil.getUiLabel("gdd.party.gold", partyGold));

			addToParty.setEnabled(party.size() < 6 && partyGold >= recruitPrice);
			removeFromParty.setEnabled(party.size() > 1 && partyGold >= recruitPrice);
			createCharacter.setEnabled(partyGold >= createPrice);
			gdWidget.refresh();
		}
		else
		{
			// no op
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> exit();
			case KeyEvent.VK_A -> addToParty();
			case KeyEvent.VK_R -> removeFromParty();
			case KeyEvent.VK_C ->
			{
				if (mode == Mode.MAIN_MENU) { createCharacter(); }
			}
			default -> gdWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
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

		int minInParty = mode == Mode.MAIN_MENU ? 0 : 1;

		if (party != null && party.size() > minInParty)
		{
			guildCallback.removeFromParty(pc, recruitPrice);
			gdWidget.add(pc);
			refresh();
		}

		return true;
	}
}
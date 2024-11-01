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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GuildDisplayDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private final GuildDisplayWidget gdWidget;
	private final DIYButton addCharacter, deleteCharacter, close;
	private final GuildCallback guildCallback;
	private final Map<String, PlayerCharacter> guild;

	/*-------------------------------------------------------------------------*/
	public GuildDisplayDialog(
		Map<String, PlayerCharacter> guild,
		GuildCallback guildCallback)
	{
		this.setStyle(Style.DIALOG);

		this.guild = guild;
		this.guildCallback = guildCallback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		Rectangle listBounds = new Rectangle(
			startX + getBorder() + getInset(),
			startY + getBorder() + getTitlePaneHeight() + getInset(),
			DIALOG_WIDTH - getBorder() *2 - getInset() *2,
			DIALOG_HEIGHT - getBorder() *2 - getInset() *4 - getButtonPaneHeight());

		this.setBounds(dialogBounds);
		List<PlayerCharacter> niceList = new ArrayList<>(guild.values());

		//remove any already in the party
		if (Maze.getInstance().getParty() != null)
		{
			for (PlayerCharacter pc : Maze.getInstance().getParty().getPlayerCharacters())
			{
				niceList.removeIf(guildPc -> guildPc.getName().equals(pc.getName()));
			}
		}

		// todo: sorting the list
		gdWidget = new GuildDisplayWidget(listBounds, niceList);

		DIYPane titlePane = getTitlePane("Guild");
		DIYPane buttonPane = getButtonPane();

		addCharacter = new DIYButton(StringUtil.getUiLabel("gdd.add.to.party"));
		addCharacter.addActionListener(this);

		deleteCharacter = new DIYButton(StringUtil.getUiLabel("gdd.delete.character"));
		deleteCharacter.addActionListener(this);

		buttonPane.add(addCharacter);
		buttonPane.add(deleteCharacter);

		close = getCloseButton();
		close.addActionListener(this);

		this.add(close);
		this.add(titlePane);
		this.add(gdWidget);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_A, KeyEvent.VK_ENTER -> addCharacter();
			case KeyEvent.VK_D -> deleteCharacter();
			case KeyEvent.VK_ESCAPE -> exit();
			default -> gdWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == addCharacter)
		{
			addCharacter();
			return true;
		}
		else if (event.getSource() == close)
		{
			exit();
			return true;
		}
		else if (event.getSource() == deleteCharacter)
		{
			deleteCharacter();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void deleteCharacter()
	{
		PlayerCharacter pc = gdWidget.getSelected();
		Maze.getInstance().removePlayerCharacterFromGuild(pc, guild);
		exit();
	}

	/*-------------------------------------------------------------------------*/
	private void addCharacter()
	{
		exit();
		if (gdWidget.getSelected() != null)
		{
			guildCallback.transferPlayerCharacterToParty(gdWidget.getSelected(), 0);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

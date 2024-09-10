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
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
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
	private final DIYButton okButton, cancel, delete;
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
			startX +border +inset,
			startY +border +titlePaneHeight +inset,
			DIALOG_WIDTH -border*2 -inset*2,
			DIALOG_HEIGHT -border*2 -inset*3 -buttonPaneHeight);

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

		DIYPane titlePane = getTitle("Guild");

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(
			startX +border +inset,
			startY +DIALOG_HEIGHT -border -buttonPaneHeight,
			DIALOG_WIDTH -border*2 -inset*2,
			buttonPaneHeight);

		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		delete = new DIYButton("Delete");
		delete.addActionListener(this);
		cancel = new DIYButton("Cancel");
		cancel.addActionListener(this);
		
		buttonPane.add(okButton);
		buttonPane.add(delete);
		buttonPane.add(cancel);

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
			case KeyEvent.VK_ESCAPE -> exit();
			case KeyEvent.VK_ENTER -> addCharacter();
			default -> gdWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			addCharacter();
			return true;
		}
		else if (event.getSource() == cancel)
		{
			exit();
			return true;
		}
		else if (event.getSource() == delete)
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

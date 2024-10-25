/*
 * Copyright (c) 2012 Alan McLachlan
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
import mclachlan.diygui.DIYListBox;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;

/**
 *
 */
public class PersonalitySelectionDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int MAX_DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/5*4;

	private final DIYListBox personalities;
	private final DIYButton okButton, close;
	private final PlayerCharacter pc;

	/*-------------------------------------------------------------------------*/
	public PersonalitySelectionDialog(
		String startingPersonality,
		PlayerCharacter pc)
	{
		int buttonPaneHeight = getButtonPaneHeight();

		List<String> list = new ArrayList<>(Database.getInstance().getPersonalities().keySet());
		Collections.sort(list);

		int border = getBorder();
		int inset = getInset();
		int titlePaneHeight = getTitlePaneHeight();

		int dialogHeight = Math.min(
			list.size()*20 + border *2 + buttonPaneHeight + titlePaneHeight + inset *2,
			MAX_DIALOG_HEIGHT);

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;
		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, dialogHeight);
		this.setBounds(dialogBounds);

		this.pc = pc;

		Rectangle isBounds = new Rectangle(
			startX +border +inset*2,
			startY +border +inset*2 +titlePaneHeight,
			width -border*2 -inset*4,
			height -buttonPaneHeight -titlePaneHeight -border*2 -inset*4);

		personalities = new DIYListBox(list);
		personalities.setSelected(startingPersonality);
		personalities.addActionListener(this);
		personalities.setBounds(isBounds);

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("sdw.personality"));

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(startX+ border, startY+height-buttonPaneHeight- border,
			width- border *2, buttonPaneHeight);
		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		close = getCloseButton();
		close.addActionListener(this);
		
		buttonPane.add(okButton);

		this.add(titlePane);
		this.add(personalities);
		this.add(buttonPane);
		this.add(close);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE -> exit();
			case KeyEvent.VK_ENTER -> setPersonality();
			default -> personalities.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			setPersonality();
			return true;
		}
		else if (event.getSource() == close)
		{
			exit();
			return true;
		}
		else if (event.getSource() == personalities)
		{
			Maze.getInstance().speechBubble(
				Personality.BasicSpeech.PERSONALITY_SELECTED.getKey(),
				pc,
				Database.getInstance().getPersonalities().get((String)personalities.getSelected()),
				DiyGuiUserInterface.instance.partyDisplay.getSelectedCharacterBounds(),
				SpeechBubble.Orientation.RIGHT);

			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void setPersonality()
	{
		exit();
		pc.setPersonality(
			Database.getInstance().getPersonalities().get(
				(String)personalities.getSelected()));
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		DiyGuiUserInterface.instance.stopAllAnimations();
		Maze.getInstance().getUi().clearDialog();
	}
}

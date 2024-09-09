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
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYRadioButton;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.UserConfig;
import mclachlan.maze.stat.SpeechUtil;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SettingsDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3*2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private DIYButton okButton, cancel;
	private UserConfig userConfig;

	private DIYRadioButton musicOff, musicLow, musicMed, musicHigh;
	private DIYRadioButton chatOff, chatLow, chatMed, chatHigh;
	private DIYRadioButton eventDelayNone, eventDelaySmall, eventDelayLarge, eventDelayMedium;
	private DIYRadioButton autoAddConsumablesNever, autoAddConsumablesAlways;

	/*-------------------------------------------------------------------------*/
	public SettingsDialog()
	{
		userConfig = new UserConfig(Maze.getInstance().getUserConfig().toProperties());

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		int buttonPaneHeight = 120;
		int border = 10;
		int inset = 20;

		Rectangle innerBounds = new Rectangle(
			startX +border +inset,
			startY +inset +buttonPaneHeight,
			DIALOG_WIDTH -inset*2,
			DIALOG_HEIGHT -buttonPaneHeight*2 -inset*4);

		DIYPane leftPane = new DIYPane(
			startX +border +inset,
			startY +inset +buttonPaneHeight,
			(DIALOG_WIDTH -inset*2) /3,
			DIALOG_HEIGHT -buttonPaneHeight*2 -inset*4);
		leftPane.setLayoutManager(new DIYGridLayout(1, 10, 5, 5));

		DIYPane rightPane = new DIYPane(startX +border +inset + (DIALOG_WIDTH -inset*2) /3,
			startY +inset +buttonPaneHeight,
			(DIALOG_WIDTH -inset*2) /3 *2,
			DIALOG_HEIGHT -buttonPaneHeight*2 -inset*4);
		rightPane.setLayoutManager(new DIYGridLayout(1, 10, 5, 5));

		//---

		musicOff = new DIYRadioButton(StringUtil.getUiLabel("sd.music.off"));
		musicLow = new DIYRadioButton(StringUtil.getUiLabel("sd.music.low"));
		musicMed = new DIYRadioButton(StringUtil.getUiLabel("sd.music.med"));
		musicHigh = new DIYRadioButton(StringUtil.getUiLabel("sd.music.high"));

		DIYPane musicPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.LEFT));
		musicPane.add(musicOff);
		musicPane.add(musicLow);
		musicPane.add(musicMed);
		musicPane.add(musicHigh);

		DIYButtonGroup musicBg = new DIYButtonGroup();
		musicBg.addButton(musicOff);
		musicBg.addButton(musicLow);
		musicBg.addButton(musicMed);
		musicBg.addButton(musicHigh);

		//---

		chatOff = new DIYRadioButton(StringUtil.getUiLabel("sd.chat.off"));
		chatLow = new DIYRadioButton(StringUtil.getUiLabel("sd.chat.low"));
		chatMed = new DIYRadioButton(StringUtil.getUiLabel("sd.chat.med"));
		chatHigh = new DIYRadioButton(StringUtil.getUiLabel("sd.chat.high"));

		DIYPane chattinessPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.LEFT));
		chattinessPane.add(chatOff);
		chattinessPane.add(chatLow);
		chattinessPane.add(chatMed);
		chattinessPane.add(chatHigh);

		DIYButtonGroup chatBg = new DIYButtonGroup();
		chatBg.addButton(chatOff);
		chatBg.addButton(chatLow);
		chatBg.addButton(chatMed);
		chatBg.addButton(chatHigh);

		//---

		eventDelayNone = new DIYRadioButton(StringUtil.getUiLabel("sd.event.delay.instant"));
		eventDelaySmall = new DIYRadioButton(StringUtil.getUiLabel("sd.event.delay.fast"));
		eventDelayMedium = new DIYRadioButton(StringUtil.getUiLabel("sd.event.delay.med"));
		eventDelayLarge = new DIYRadioButton(StringUtil.getUiLabel("sd.event.delay.slow"));

		DIYPane eventDelayPane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.LEFT));
		eventDelayPane.add(eventDelayNone);
		eventDelayPane.add(eventDelaySmall);
		eventDelayPane.add(eventDelayMedium);
		eventDelayPane.add(eventDelayLarge);

		DIYButtonGroup eventDelayBg = new DIYButtonGroup();
		eventDelayBg.addButton(eventDelayNone);
		eventDelayBg.addButton(eventDelaySmall);
		eventDelayBg.addButton(eventDelayMedium);
		eventDelayBg.addButton(eventDelayLarge);

		//---

		autoAddConsumablesNever = new DIYRadioButton(StringUtil.getUiLabel("sd.auto.add.consumables.never"));
		autoAddConsumablesAlways = new DIYRadioButton(StringUtil.getUiLabel("sd.auto.add.consumables.always"));

		DIYPane autoAddConsumables = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.LEFT));
		autoAddConsumables.add(autoAddConsumablesAlways);
		autoAddConsumables.add(autoAddConsumablesNever);

		DIYButtonGroup aACBg = new DIYButtonGroup();
		aACBg.addButton(autoAddConsumablesAlways);
		aACBg.addButton(autoAddConsumablesNever);

		//---

		leftPane.add(new DIYLabel());
		rightPane.add(new DIYLabel(StringUtil.getUiLabel("sd.audio.settings"), DIYToolkit.Align.LEFT));

		leftPane.add(new DIYLabel(StringUtil.getUiLabel("sd.music.volume"), DIYToolkit.Align.LEFT));
		rightPane.add(musicPane);

		leftPane.add(new DIYLabel());
		rightPane.add(new DIYLabel());

		leftPane.add(new DIYLabel());
		rightPane.add(new DIYLabel(StringUtil.getUiLabel("sd.ui.settings"), DIYToolkit.Align.LEFT));

		leftPane.add(new DIYLabel(StringUtil.getUiLabel("sd.event.display.speed"),  DIYToolkit.Align.LEFT));
		rightPane.add(eventDelayPane);

		leftPane.add(new DIYLabel(StringUtil.getUiLabel("sd.personality.chattiness"), DIYToolkit.Align.LEFT));
		rightPane.add(chattinessPane);

		leftPane.add(new DIYLabel());
		rightPane.add(new DIYLabel());

		leftPane.add(new DIYLabel());
		rightPane.add(new DIYLabel(StringUtil.getUiLabel("sd.gameplay.settings"), DIYToolkit.Align.LEFT));

		leftPane.add(new DIYLabel(StringUtil.getUiLabel("sd.auto.add.consumables"), DIYToolkit.Align.LEFT));
		rightPane.add(autoAddConsumables);

		//---

		refresh(userConfig);

		DIYPane titlePane = getTitle(StringUtil.getUiLabel("sd.title"));

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y + height - buttonPaneHeight - inset, width, buttonPaneHeight);
		okButton = new DIYButton(StringUtil.getUiLabel("common.ok"));
		okButton.addActionListener(this);
		cancel = new DIYButton(StringUtil.getUiLabel("common.cancel"));
		cancel.addActionListener(this);
		
		buttonPane.add(okButton);
		buttonPane.add(cancel);

		setBackground();

		this.add(titlePane);
		this.add(leftPane);
		this.add(rightPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void refresh(UserConfig userConfig)
	{
		switch (userConfig.getMusicVolume())
		{
			case 0: musicOff.setSelected(true); break;
			case 33: musicLow.setSelected(true); break;
			case 66: musicMed.setSelected(true); break;
			case 100: musicHigh.setSelected(true); break;
			default: musicMed.setSelected(true); break;
		}

		switch (userConfig.getPersonalityChattiness())
		{
			case SpeechUtil.OFF: chatOff.setSelected(true); break;
			case SpeechUtil.LOW: chatLow.setSelected(true); break;
			case SpeechUtil.MEDIUM: chatMed.setSelected(true); break;
			case SpeechUtil.HIGH: chatHigh.setSelected(true); break;
			default: throw new MazeException("Invalid value: "+userConfig.getPersonalityChattiness());
		}

		switch (userConfig.getCombatDelay())
		{
			case 0: eventDelayNone.setSelected(true); break;
			case 150: eventDelaySmall.setSelected(true); break;
			case 400: eventDelayMedium.setSelected(true); break;
			case 1000: eventDelayLarge.setSelected(true); break;
			default: eventDelayMedium.setSelected(true); break;
		}

		if (userConfig.isAutoAddConsumables())
		{
			autoAddConsumablesAlways.setSelected(true);
		}
		else
		{
			autoAddConsumablesNever.setSelected(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
			case KeyEvent.VK_ENTER:
				save();
				break;
			default:
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			save();
			return true;
		}
		else if (event.getSource() == cancel)
		{
			exit();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void commit()
	{
		// music volume
		if (musicOff.isSelected())
		{
			this.userConfig.setMusicVolume(0);
		}
		else if (musicLow.isSelected())
		{
			this.userConfig.setMusicVolume(33);
		}
		else if (musicMed.isSelected())
		{
			this.userConfig.setMusicVolume(66);
		}
		else if (musicHigh.isSelected())
		{
			this.userConfig.setMusicVolume(100);
		}

		// chattiness
		if (chatOff.isSelected())
		{
			this.userConfig.setPersonalityChattiness(SpeechUtil.OFF);
		}
		else if (chatLow.isSelected())
		{
			this.userConfig.setPersonalityChattiness(SpeechUtil.LOW);
		}
		else if (chatMed.isSelected())
		{
			this.userConfig.setPersonalityChattiness(SpeechUtil.MEDIUM);
		}
		else if (chatHigh.isSelected())
		{
			this.userConfig.setPersonalityChattiness(SpeechUtil.HIGH);
		}

		// combat event delay
		if (eventDelayNone.isSelected())
		{
			this.userConfig.setCombatDelay(0);
		}
		else if (eventDelaySmall.isSelected())
		{
			this.userConfig.setCombatDelay(150);
		}
		else if (eventDelayMedium.isSelected())
		{
			this.userConfig.setCombatDelay(400);
		}
		else if (eventDelayLarge.isSelected())
		{
			this.userConfig.setCombatDelay(1000);
		}

		// auto add consumables
		if (autoAddConsumablesAlways.isSelected())
		{
			this.userConfig.setAutoAddConsumables(true);
		}
		else if (autoAddConsumablesNever.isSelected())
		{
			this.userConfig.setAutoAddConsumables(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void save()
	{
		commit();
		try
		{
			Maze.getInstance().saveUserConfig(userConfig);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
		exit();
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

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

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.journal.Journal;
import mclachlan.maze.game.journal.JournalEntry;
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class JournalDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH -100;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT -100;

	private DIYButton quests, npcs, logbook, zones, exitButton;

	private DIYListBox journalKeys;
	private DIYTextArea textArea;
	private JournalManager.JournalType journalType;
	private DIYLabel title;

	/*-------------------------------------------------------------------------*/
	public JournalDialog()
	{
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		int buttonPaneHeight = 20;
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

		DIYPane titlePane = getTitle(StringUtil.getUiLabel("jd.title.logbook"));

		journalKeys = new DIYListBox(new ArrayList());
		journalKeys.addActionListener(this);

		leftPane.add(journalKeys);

		textArea = new DIYTextArea("");
		textArea.setTransparent(true);
		textArea.setBounds(rightPane.getBounds());

		DIYScrollPane scroller = new DIYScrollPane(textArea);
		scroller.setBounds(rightPane.getBounds());

		rightPane.add(scroller);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y + height - buttonPaneHeight - inset, width, buttonPaneHeight);

		logbook = new DIYButton(StringUtil.getUiLabel("jd.logbook"));
		logbook.addActionListener(this);

		zones = new DIYButton(StringUtil.getUiLabel("jd.zones"));
		zones.addActionListener(this);

		quests = new DIYButton(StringUtil.getUiLabel("jd.quests"));
		quests.addActionListener(this);

		npcs = new DIYButton(StringUtil.getUiLabel("jd.npcs"));
		npcs.addActionListener(this);

		exitButton = new DIYButton(StringUtil.getUiLabel("common.exit"));
		exitButton.addActionListener(this);

		buttonPane.add(logbook);
		buttonPane.add(zones);
		buttonPane.add(quests);
		buttonPane.add(npcs);
		buttonPane.add(exitButton);

		refresh(JournalManager.JournalType.LOGBOOK);

		setBackground();

		this.add(titlePane);
		this.add(leftPane);
		this.add(rightPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	protected DIYPane getTitle(String titleText)
	{
		DIYPane titlePane = new DIYPane(new DIYFlowLayout(0,0, DIYToolkit.Align.CENTER));
		title = new DIYLabel(titleText);
		titlePane.setBounds(x, y+ border, width, titlePaneHeight);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		title.setFont(f);
		titlePane.add(title);
		return titlePane;
	}

	/*-------------------------------------------------------------------------*/
	private void refresh(JournalManager.JournalType journalType)
	{
		this.journalType = journalType;

		switch (journalType)
		{
			case LOGBOOK:
				title.setText(StringUtil.getUiLabel("jd.title.logbook"));
				break;
			case ZONE:
				title.setText(StringUtil.getUiLabel("jd.title.zones"));
				break;
			case QUEST:
				title.setText(StringUtil.getUiLabel("jd.title.quests"));
				break;
			case NPC:
				title.setText(StringUtil.getUiLabel("jd.title.npcs"));
				break;
			default: throw new MazeException(journalType.toString());
		}

		Journal journal = JournalManager.getInstance().getJournal(journalType);

		List<String> keys = new ArrayList<String>();
		keys.addAll(journal.getContents().keySet());
		Collections.sort(keys);

		journalKeys.setItems(keys);

		if (keys.size() > 0)
		{
			journalKeys.setSelected(keys.get(0));
			refreshText(keys.get(0), journal);
		}
		else
		{
			textArea.setText(StringUtil.getUiLabel("jd.none.yet"));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void refreshText(String key, Journal journal)
	{
		StringBuilder sb = new StringBuilder();

		List<JournalEntry> journalEntries = journal.getContents().get(key);

		for (JournalEntry je : journalEntries)
		{
			GameTime.GameDate gameDate = GameTime.getGameDate(je.getTurnNr());
			sb.append(gameDate.toFormattedString()).append(": ").append(je.getText()).append("\n\n");
		}

		textArea.setText(sb.toString());
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_L:
				refresh(JournalManager.JournalType.LOGBOOK);
				break;
			case KeyEvent.VK_Z:
				refresh(JournalManager.JournalType.ZONE);
				break;
			case KeyEvent.VK_N:
				refresh(JournalManager.JournalType.NPC);
				break;
			case KeyEvent.VK_Q:
				refresh(JournalManager.JournalType.QUEST);
				break;
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				exit();
				break;
			default:
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == exitButton)
		{
			exit();
			return true;
		}
		else if (event.getSource() == logbook)
		{
			refresh(JournalManager.JournalType.LOGBOOK);
			return true;
		}
		else if (event.getSource() == zones)
		{
			refresh(JournalManager.JournalType.ZONE);
			return true;
		}
		else if (event.getSource() == npcs)
		{
			refresh(JournalManager.JournalType.NPC);
			return true;
		}
		else if (event.getSource() == quests)
		{
			refresh(JournalManager.JournalType.QUEST);
			return true;
		}
		else if (event.getSource() == journalKeys)
		{
			String key = (String)journalKeys.getSelected();

			refreshText(key, JournalManager.getInstance().getJournal(journalType));
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}

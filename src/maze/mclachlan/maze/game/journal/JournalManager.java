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

package mclachlan.maze.game.journal;

import java.util.*;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 * Manager for all the player's journals
 */
public class JournalManager
{
	public static String LOGBOOK_KEY;

	public enum JournalType
	{
		// Single key. Includes zone entries, exits, npc meetings, day turnovers
		LOGBOOK("logbook"),
		// Key per zone. Includes all flavour text displayed in the zone
		ZONE("zone"),
		// Key per quest. Includes quest flavour text, completion status
		QUEST("quest"),
		// Key per NPC. Includes all NPC dialogs
		NPC("npc");

		private final String journalName;

		JournalType(String journalName)
		{
			this.journalName = journalName;
		}

		public String getJournalName()
		{
			return journalName;
		}
	}

	/*-------------------------------------------------------------------------*/

	private static final JournalManager instance = new JournalManager();

	private Map<JournalType, Journal> journals;

	/*-------------------------------------------------------------------------*/
	private JournalManager()
	{
		LOGBOOK_KEY = StringUtil.getUiLabel("jd.logbook.key");
	}

	/*-------------------------------------------------------------------------*/
	public static JournalManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public void startGame()
	{
		journals = new HashMap<>();

		journals.put(JournalType.QUEST, new Journal(JournalType.QUEST.getJournalName()));
		journals.put(JournalType.NPC, new Journal(JournalType.NPC.getJournalName()));
		journals.put(JournalType.LOGBOOK, new Journal(JournalType.LOGBOOK.getJournalName()));
		journals.put(JournalType.ZONE, new Journal(JournalType.ZONE.getJournalName()));
	}

	/*-------------------------------------------------------------------------*/
	public void loadGame(String name, Loader loader) throws Exception
	{
		journals = new HashMap<>();

		journals.put(JournalType.QUEST, loader.loadJournal(name, JournalType.QUEST.getJournalName()));
		journals.put(JournalType.NPC, loader.loadJournal(name, JournalType.NPC.getJournalName()));
		journals.put(JournalType.LOGBOOK, loader.loadJournal(name, JournalType.LOGBOOK.getJournalName()));
		journals.put(JournalType.ZONE, loader.loadJournal(name, JournalType.ZONE.getJournalName()));
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.saveJournal(saveGameName, journals.get(JournalType.QUEST));
		saver.saveJournal(saveGameName, journals.get(JournalType.NPC));
		saver.saveJournal(saveGameName, journals.get(JournalType.LOGBOOK));
		saver.saveJournal(saveGameName, journals.get(JournalType.ZONE));
	}

	/*-------------------------------------------------------------------------*/
	public void addJournalEntry(JournalType j, String key, String entry)
	{
		Journal journal = getJournal(j);

		long turnNr = GameTime.getTurnNr();

		journal.addJournalEntry(turnNr, key, entry);
	}

	/*-------------------------------------------------------------------------*/
	public Journal getJournal(JournalType journal)
	{
		if (!journals.containsKey(journal))
		{
			throw new MazeException("journal not present "+journal);
		}
		return journals.get(journal);
	}

	/*-------------------------------------------------------------------------*/
	public void logbook(String text)
	{
		getJournal(JournalType.LOGBOOK).addJournalEntry(
			GameTime.getTurnNr(),
			JournalManager.LOGBOOK_KEY,
			text);
	}

	/*-------------------------------------------------------------------------*/
	public void zoneJournal(String text)
	{
		getJournal(JournalType.ZONE).addJournalEntry(
			GameTime.getTurnNr(),
			Maze.getInstance().getCurrentZone().getName(),
			text);
	}

	/*-------------------------------------------------------------------------*/
	public void npcJournal(String text)
	{
		getJournal(JournalType.NPC).addJournalEntry(
			GameTime.getTurnNr(),
			Maze.getInstance().getCurrentActorEncounter().getLeader().getName(),
			text);
	}

	/*-------------------------------------------------------------------------*/
	public void questJournal(String key, String text)
	{
		getJournal(JournalType.QUEST).addJournalEntry(
			GameTime.getTurnNr(),
			key,
			text);
	}
}

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

/**
 * Data structure to represent a party journal.
 */
public class Journal
{
	/**
	 * Name of this journal, e.g. "NPCs", "Quests".
	 */
	private String name;

	/**
	 * KEY: Name of a journal section, e.g. "Red Ear", "Main Quest" <br/>
	 * VALUE: List of journal entries for that section, e.g. conversations
	 * with Red Ear
	 */
	private Map<String, List<JournalEntry>> contents;

	public Journal()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Journal(String name)
	{
		this.name = name;
		this.contents = new HashMap<>();
	}

	/*-------------------------------------------------------------------------*/
	public Journal(
		String name, Map<String, List<JournalEntry>> contents)
	{
		this.name = name;
		this.contents = contents;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, List<JournalEntry>> getContents()
	{
		return contents;
	}

	/*-------------------------------------------------------------------------*/
	public void setContents(Map<String, List<JournalEntry>> contents)
	{
		this.contents = contents;
	}

	/*-------------------------------------------------------------------------*/
	public void addJournalEntry(long turnNr, String key, String text)
	{
		if (!contents.containsKey(key))
		{
			contents.put(key, new ArrayList<>());
		}

		List<JournalEntry> journalEntries = contents.get(key);

		journalEntries.add(new JournalEntry(turnNr, text));
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Journal))
		{
			return false;
		}

		Journal journal = (Journal)o;

		if (getName() != null ? !getName().equals(journal.getName()) : journal.getName() != null)
		{
			return false;
		}
		return getContents() != null ? getContents().equals(journal.getContents()) : journal.getContents() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getContents() != null ? getContents().hashCode() : 0);
		return result;
	}
}

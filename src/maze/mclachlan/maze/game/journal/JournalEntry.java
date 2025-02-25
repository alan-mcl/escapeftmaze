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

/**
 *
 */
public class JournalEntry
{
	private long turnNr;
	private String text;

	public JournalEntry()
	{
	}

	public JournalEntry(long turnNr, String text)
	{
		this.turnNr = turnNr;
		this.text = text;
	}

	public long getTurnNr()
	{
		return turnNr;
	}

	public void setTurnNr(long turnNr)
	{
		this.turnNr = turnNr;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof JournalEntry))
		{
			return false;
		}

		JournalEntry that = (JournalEntry)o;

		if (getTurnNr() != that.getTurnNr())
		{
			return false;
		}
		return getText() != null ? getText().equals(that.getText()) : that.getText() == null;
	}

	@Override
	public int hashCode()
	{
		int result = (int)(getTurnNr() ^ (getTurnNr() >>> 32));
		result = 31 * result + (getText() != null ? getText().hashCode() : 0);
		return result;
	}
}

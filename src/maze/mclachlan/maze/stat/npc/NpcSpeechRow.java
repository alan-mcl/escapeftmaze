
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

package mclachlan.maze.stat.npc;

import java.util.*;

/**
 *
 */
public class NpcSpeechRow
{
	/**
	 * The priority of this bit of speech. 0 is the top priority, a higher
	 * value is lower priority
	 */
	private int priority;

	/**
	 * A set of keywords that cause this response.
	 */
	private Set<String> keywords;

	/**
	 * What the NPC has to say in response.
	 */
	private String speech;

	public NpcSpeechRow()
	{
	}

	/*-------------------------------------------------------------------------*/
	public NpcSpeechRow(int priority, Set<String> keywords, String speech)
	{
		this.priority = priority;
		this.keywords = keywords;
		this.speech = speech;
	}

	public int getPriority()
	{
		return priority;
	}

	public Set<String> getKeywords()
	{
		return keywords;
	}

	public String getSpeech()
	{
		return speech;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public void setKeywords(Set<String> keywords)
	{
		this.keywords = keywords;
	}

	public void setSpeech(String speech)
	{
		this.speech = speech;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof NpcSpeechRow))
		{
			return false;
		}

		NpcSpeechRow that = (NpcSpeechRow)o;

		if (priority != that.priority)
		{
			return false;
		}
		if (!keywords.equals(that.keywords))
		{
			return false;
		}
		if (!speech.equals(that.speech))
		{
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int hashCode()
	{
		int result = priority;
		result = 31 * result + keywords.hashCode();
		result = 31 * result + speech.hashCode();
		return result;
	}
}

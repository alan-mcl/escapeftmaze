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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class CharacterClassKnowledgeEvent extends MazeEvent
{
	private Map<String, String> knowledgeText;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param classes
	 * 	A list of character class names that this know this nugget of wisdom
	 * @param text
	 * 	The text to display to the user if the party contains one or more of
	 * 	those classes
	 */
	public CharacterClassKnowledgeEvent(String text, String... classes)
	{
		this.knowledgeText = new HashMap<>();
		for (String c : classes)
		{
			knowledgeText.put(c, text);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param classes
	 * 	A list of character class names that this know this nugget of wisdom
	 * @param text
	 * 	The text to display to the user if the party contains one or more of
	 * 	those classes
	 */
	public CharacterClassKnowledgeEvent(List<String> classes, String text)
	{
		this.knowledgeText = new HashMap<>();
		for (String c : classes)
		{
			knowledgeText.put(c, text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public CharacterClassKnowledgeEvent(Map<String, String> knowledgeText)
	{
		this.knowledgeText = knowledgeText;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		boolean debug = Boolean.valueOf(
			Maze.getInstance().getAppConfig().get((
				Maze.AppConfig.DEBUG_KNOWLEDGE_EVENTS)));

		for (String characterClass : knowledgeText.keySet())
		{
			if (Maze.getInstance().getParty().containsCharacterClass(characterClass) || debug)
			{
				result.add(new FlavourTextEvent(
					characterClass+":\n\n"+ knowledgeText.get(characterClass),
					FlavourTextEvent.Delay.WAIT_ON_CLICK,
					true));
			}
		}
		
		return result;
	}
	
	/*-------------------------------------------------------------------------*/

	public Map<String, String> getKnowledgeText()
	{
		return knowledgeText;
	}

	public void setKnowledgeText(Map<String, String> knowledgeText)
	{
		this.knowledgeText = knowledgeText;
	}
}

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

package mclachlan.maze.data.v1;

import java.util.*;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcSpeechRow;

/**
 *
 */
public class V1NpcSpeech
{
	/*-------------------------------------------------------------------------*/
	static Properties toProperties(NpcSpeech speech)
	{
		Properties result = new Properties();

		for (NpcSpeechRow row : speech.getDialogue())
		{
			result.setProperty(getKey(row.getKeywords()), getValue(row.getPriority(), row.getSpeech()));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static String getValue(int priority, String speech)
	{
		return priority+"|"+V1Utils.escapeNewlines(speech);
	}

	/*-------------------------------------------------------------------------*/
	public static String getKey(Set<String> keywords)
	{
		String[] arr = keywords.toArray(new String[keywords.size()]);
		Arrays.sort(arr);
		return V1Utils.toStringStrings(arr,",");
	}

	/*-------------------------------------------------------------------------*/
	private static Set<String> getKeywords(String key)
	{
		Set<String> result = new HashSet<String>();
		String[] strings = key.split(",");
		result.addAll(Arrays.asList(strings));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static NpcSpeech fromProperties(Properties p)
	{
		NpcSpeech result = new NpcSpeech();

		for (String key : p.stringPropertyNames())
		{
			String[] value = p.getProperty(key).split("\\|", 2);
			int priority = Integer.parseInt(value[0]);
			String speech = V1Utils.replaceNewlines(value[1]);
			Set<String> keywords = getKeywords(key);

			NpcSpeechRow temp = new NpcSpeechRow(priority, keywords, speech);
			result.addNpcSpeechRow(temp);
		}

		return result;
	}
}

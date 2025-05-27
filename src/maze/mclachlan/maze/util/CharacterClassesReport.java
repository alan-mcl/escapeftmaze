/*
 * Copyright (c) 2014 Alan McLachlan
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


package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.CharacterClass;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class CharacterClassesReport
{
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Map<String, CharacterClass> items = db.getCharacterClasses();

		List<CharacterClass> list = new ArrayList<CharacterClass>(items.values());
		Collections.sort(list, new Comparator<CharacterClass>()
		{
			public int compare(CharacterClass o1, CharacterClass o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		// print HTML for the google site
		for (CharacterClass cc : list)
		{
			h2(cc.getName());
			p(cc.getDescription());
			p(descCollection("Races: ", cc.getAllowedRaces()));
			p(descCollection("Genders: ", cc.getAllowedGenders()));
			p(descModifiers("Starting Modifiers: ", cc.getStartingModifiers()));

			System.out.println();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String descCollection(String label,
		Collection collection)
	{
		if (collection == null)
		{
			return "None";
		}

		StringBuilder sb = new StringBuilder(label);

		for (Object obj : collection)
		{
			sb.append(obj).append(", ");
		}

		return sb.substring(0, sb.length()-2);
	}

	private static String descModifiers(String label, StatModifier modifiers)
	{
		StringBuilder sb = new StringBuilder(label);

		for (Stats.Modifier s : modifiers.getModifiers().keySet())
		{
			sb.append(StringUtil.getModifierName(s)).
				append(" ").
				append(Stats.descModifier(s, modifiers.getModifier(s))).
				append(", ");
		}

		return sb.toString();
	}

	private static void p(String paragraph)
	{
		System.out.println("<p>"+paragraph+"</p>");
	}

	private static void h2(String s)
	{
		System.out.println("<h2>"+ s +"</h2>");
	}
}

/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.ui.diygui.NullProgressListener;

/**
 *
 */
public class PersonalitiesReport
{
	public static void main(String[] args) throws Exception
	{
		V2Loader loader = new V2Loader();
		V2Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());
		db.initImpls();
		db.initCaches(new NullProgressListener());

		Map<String, Personality> items = db.getPersonalities();

		List<Personality> list = new ArrayList<>(items.values());
		list.sort(Comparator.comparing(Personality::getName));

		for (Personality p : list)
		{
			System.out.println("Personality: "+p.getName());
			System.out.println("Description: "+p.getDescription());
			System.out.println("Example speech: ");
			for (String s : p.getSpeech().values())
			{
				if (s.length() > 0)
				{
					System.out.println("\"" + s.trim().replaceAll("\n", " ") + "\"");
				}
			}
			System.out.println();
		}


		printHtml(list);
	}

	/*-------------------------------------------------------------------------*/
	private static void printHtml(List<Personality> list)
	{
		// print HTML for the google site
		for (Personality p : list)
		{
			ArrayList<String> keys = new ArrayList<>(p.getSpeech().keySet());
			Collections.sort(keys);

			h2(p.getName());
			p(p.getDescription());
			System.out.println("<table>");
			for (String k : keys)
			{
				System.out.println("<tr><td>"+k+"</td><td>"+p.getWords(k)+"</td></tr>");
			}
			System.out.println("</table>");
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String descStringList(String label, List<String> strings)
	{
		StringBuilder sb = new StringBuilder(label);

		for (String s : strings)
		{
			sb.append(s).append(", ");
		}

		return sb.substring(0, sb.length()-2);
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

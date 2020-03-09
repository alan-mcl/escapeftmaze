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
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.StartingKit;

/**
 *
 */
public class StartingKitsReport
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Map<String, StartingKit> items = db.getStartingKits();

		List<StartingKit> list = new ArrayList<>(items.values());
		list.sort(Comparator.comparing(StartingKit::getDisplayName));

		// print HTML for the google site
		for (StartingKit sk : list)
		{
			ArrayList<String> classes = new ArrayList<>(sk.getUsableByCharacterClass());
			Collections.sort(classes);

			h2(sk.getDisplayName());
			p(sk.getDescription());
			p(descStringList("Character Classes: ", classes));
			p(descStringList("Starting Items: ", sk.getStartingItemNames()));
		}

		// print CSV for the balancing
		List<String> columns = new ArrayList<>();
		columns.add("Kit");
		columns.add("P Weap");
		columns.add("S Weap");
		List<String> classes = new ArrayList<>(Database.getInstance().getCharacterClasses().keySet());
		Collections.sort(classes);
		columns.addAll(classes);

		for (String s : columns)
		{
			System.out.print(s+",");
		}
		System.out.println();

		for (StartingKit sk : list)
		{
			System.out.print(sk.getDisplayName()+",");
			System.out.print(sk.getPrimaryWeapon()+",");
			System.out.print(sk.getSecondaryWeapon()==null?",":sk.getSecondaryWeapon()+",");
			for (String s : classes)
			{
				if (sk.getUsableByCharacterClass().contains(s))
				{
					System.out.print("Y,");
				}
				else
				{
					System.out.print(",");
				}
			}
			System.out.println();
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

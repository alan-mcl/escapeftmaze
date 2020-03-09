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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.stat.magic.PlayerSpellBook;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1PlayerSpellBook
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, PlayerSpellBook> load(BufferedReader reader)
	{
		try
		{
			Map <String, PlayerSpellBook> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				PlayerSpellBook g = fromProperties(p);
				result.put(g.getName(), g);
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, PlayerSpellBook> map) throws Exception
	{
		for (String name : map.keySet())
		{
			PlayerSpellBook g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(PlayerSpellBook obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append("description=");
		b.append(obj.getDescription());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != PlayerSpellBook.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("spellNames=");
			b.append(V1Utils.stringList.toString(new ArrayList<String>(obj.getSpellNames())));
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static PlayerSpellBook fromProperties(Properties p) throws Exception
	{
		String name = p.getProperty("name");
		String description = p.getProperty("description");

		if (p.getProperty("impl") != null)
		{
			// custom PlayerSpellBook impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (PlayerSpellBook)clazz.newInstance();
		}
		else
		{

			Collection<String> spellNames = V1Utils.stringList.fromString(p.getProperty("spellNames"));
			return new PlayerSpellBook(name, description, spellNames);
		}
	}
}

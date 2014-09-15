/*
 * Copyright (c) 2012 Alan McLachlan
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
import mclachlan.maze.stat.ItemEnchantment;
import mclachlan.maze.stat.ItemEnchantments;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class V1ItemEnchantments
{
	public static final String SEP = ",";

	/*-------------------------------------------------------------------------*/
	static V1PercentageTable<ItemEnchantment> enchantmentTable
		= new V1PercentageTable<ItemEnchantment>()
	{
		public ItemEnchantment typeFromString(String s)
		{
			return V1ItemEnchantment.fromString(s);
		}

		public String typeToString(ItemEnchantment ie)
		{
			return V1ItemEnchantment.toString(ie);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, ItemEnchantments> load(BufferedReader reader) throws Exception
	{
		Map <String, ItemEnchantments> result = new HashMap<String, ItemEnchantments>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ItemEnchantments g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, ItemEnchantments> map) throws Exception
	{
		for (String name : map.keySet())
		{
			ItemEnchantments g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(ItemEnchantments obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append("enchantments=");
		b.append(enchantmentTable.toString(obj.getEnchantments(), "~", "!"));
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static ItemEnchantments fromProperties(Properties p) throws Exception
	{
		String name = p.getProperty("name");
		PercentageTable<ItemEnchantment> enchantments =
				enchantmentTable.fromString(p.getProperty("enchantments"), "~", "!");

		return new ItemEnchantments(name, enchantments);
	}
}

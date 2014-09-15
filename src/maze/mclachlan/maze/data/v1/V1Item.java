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

import mclachlan.maze.data.Database;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class V1Item
{
	public static final String SEP = "/";

	/*-------------------------------------------------------------------------*/
	public static String toString(Item item)
	{
		if (item == null)
		{
			return "";
		}

		StringBuilder buffer = new StringBuilder();

		buffer.append(item.getTemplate().getName());
		buffer.append(SEP);
		buffer.append(item.getCursedState());
		buffer.append(SEP);
		buffer.append(item.getIdentificationState());
		buffer.append(SEP);
		buffer.append(V1CurMax.toString(item.getStack()));
		buffer.append(SEP);
		buffer.append(V1CurMax.toString(item.getCharges()));
		buffer.append(SEP);
		buffer.append(item.getEnchantment()==null?"":item.getEnchantment().getName());

		return buffer.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Item fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP, -1);

		ItemTemplate template = Database.getInstance().getItemTemplate(strs[0]);
		int cursedState = Integer.parseInt(strs[1]);
		int indentificationState = Integer.parseInt(strs[2]);
		CurMax stack = V1CurMax.fromString(strs[3]);
		CurMax charges = V1CurMax.fromString(strs[4]);
		String enchantment = strs[5];

		return new Item(
			template,
			cursedState,
			indentificationState,
			stack,
			charges,
			template.getEnchantment(enchantment));
	}
}

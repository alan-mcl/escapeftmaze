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

import mclachlan.maze.stat.ItemEnchantment;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class V1ItemEnchantment
{
	public static final String SEP = "/";

	/*-------------------------------------------------------------------------*/
	public static String toString(ItemEnchantment ie)
	{
		if (ie == null)
		{
			return "";
		}

		StringBuilder buffer = new StringBuilder();

		buffer.append(ie.getName());
		buffer.append(SEP);
		buffer.append(ie.getPrefix()==null?"":ie.getPrefix());
		buffer.append(SEP);
		buffer.append(ie.getSuffix()==null?"":ie.getSuffix());
		buffer.append(SEP);
		buffer.append(V1StatModifier.toString(ie.getModifiers()));
		buffer.append(SEP);
		buffer.append(ie.getCostModifier());

		return buffer.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static ItemEnchantment fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP, -1);

		String name = strs[0];
		String prefix = strs[1];
		String suffix = strs[2];
		StatModifier modifiers = V1StatModifier.fromString(strs[3]);
		int costModifier = Integer.parseInt(strs[4]);

		return new ItemEnchantment(
			name,
			prefix.length()==0?null:prefix,
			suffix.length()==0?null:suffix, 
			modifiers,
			costModifier);
	}
}
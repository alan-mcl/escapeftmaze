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

/**
 *
 */
public abstract class V1Map<K, V>
{
	public static final String SEP = ",";
	public static final String SUB_SEP = ":";

	private String sep, sub_sep;

	/*-------------------------------------------------------------------------*/
	protected V1Map()
	{
		sep = SEP;
		sub_sep = SUB_SEP;
	}

	/*-------------------------------------------------------------------------*/
	protected V1Map(String sep, String sub_sep)
	{
		this.sep = sep;
		this.sub_sep = sub_sep;
	}

	/*-------------------------------------------------------------------------*/
	public String toString(Map<K, V> map)
	{
		if (map == null)
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (K k : map.keySet())
		{
			if (!first)
			{
				sb.append(sep);
			}
			sb.append(typeToStringKey(k));
			sb.append(sub_sep);
			sb.append(typeToStringValue(map.get(k)));
			first = false;
		}

		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public Map<K, V> fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		Map<K, V> result = new HashMap<K, V>();
		String[] strs = s.split(sep);

		for (String str : strs)
		{
			String[] items = str.split(sub_sep, -1);
			K key = typeFromStringKey(items[0]);
			V value = typeFromStringValue(items[1]);
			result.put(key, value);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public abstract String typeToStringKey(K k);

	/*-------------------------------------------------------------------------*/
	public abstract String typeToStringValue(V v);

	/*-------------------------------------------------------------------------*/
	public abstract K typeFromStringKey(String s);

	/*-------------------------------------------------------------------------*/
	public abstract V typeFromStringValue(String s);
}

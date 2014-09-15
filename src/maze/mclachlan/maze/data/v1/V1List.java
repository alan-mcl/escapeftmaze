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

import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public abstract class V1List<T>
{
	public static final String DEFAULT_SEPARATOR = ",";
	
	protected String separator;
	
	/*-------------------------------------------------------------------------*/
	protected V1List()
	{
		this (DEFAULT_SEPARATOR);
	}

	/*-------------------------------------------------------------------------*/
	public V1List(String separator)
	{
		this.separator = separator;
	}

	/*-------------------------------------------------------------------------*/
	public String toString(List<T> list)
	{
		if (list == null)
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();

		int max = list.size();
		for (int i = 0; i < max; i++)
		{
			sb.append(typeToString(list.get(i)));
			if (i < max-1)
			{
				sb.append(separator);
			}
		}

		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public List<T> fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		List<T> result = new ArrayList<T>();
		String[] strs = s.split(separator, -1);

		for (String str : strs)
		{
			result.add(typeFromString(str));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public abstract String typeToString(T t);

	/*-------------------------------------------------------------------------*/
	public abstract T typeFromString(String s);
}

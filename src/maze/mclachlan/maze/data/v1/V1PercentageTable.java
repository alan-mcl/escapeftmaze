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

import mclachlan.maze.stat.PercentageTable;
import java.util.List;

/**
 *
 */
public abstract class V1PercentageTable<T>
{
	public static final String ROW_SEP = ",";
	public static final String COL_SEP = ":";
	
	/*-------------------------------------------------------------------------*/
	public String toString(PercentageTable<T> pt)
	{
		return toString(pt, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/
	public String toString(PercentageTable<T> pt, String rowSep, String colSep)
	{
		if (pt == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		s.append(pt.shouldSumTo100()?1:0);
		s.append(rowSep);

		List<T> items = pt.getItems();
		List<Integer> percentages = pt.getPercentages();

		int max = items.size();
		for (int i = 0; i < max; i++)
		{
			s.append(typeToString(items.get(i)));
			s.append(colSep);
			s.append(percentages.get(i));
			if (i < max-1)
			{
				s.append(rowSep);
			}
		}

		return s.toString();
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<T> fromString(String s, String rowSep, String colSep)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] rows = s.split(rowSep);
		PercentageTable<T> result = new PercentageTable<T>(Boolean.valueOf(rows[0]));

		for (int i = 1; i < rows.length; i++)
		{
			String[] cols = rows[i].split(colSep);

			T item = typeFromString(cols[0]);
			int perc = Integer.parseInt(cols[1]);

			result.add(item, perc);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<T> fromString(String s)
	{
		return fromString(s, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/
	public abstract T typeFromString(String s);

	/*-------------------------------------------------------------------------*/
	public abstract String typeToString(T t);
}

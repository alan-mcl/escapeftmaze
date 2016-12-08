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
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public abstract class V1GroupOfPossibilties<T>
{
	public String ROW_SEP = ",";
	public String COL_SEP = ":";

	/*-------------------------------------------------------------------------*/
	protected V1GroupOfPossibilties()
	{
	}

	/*-------------------------------------------------------------------------*/
	protected V1GroupOfPossibilties(String ROW_SEP, String COL_SEP)
	{
		this.ROW_SEP = ROW_SEP;
		this.COL_SEP = COL_SEP;
	}

	/*-------------------------------------------------------------------------*/
	public String toString(GroupOfPossibilities<T> pt)
	{
		if (pt == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		List<T> items = pt.getPossibilities();
		List<Integer> percentages = pt.getPercentages();

		int max = items.size();
		for (int i = 0; i < max; i++)
		{
			s.append(typeToString(items.get(i)));
			s.append(COL_SEP);
			s.append(percentages.get(i));
			if (i < max-1)
			{
				s.append(ROW_SEP);
			}
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities<T> fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] rows = s.split(ROW_SEP);
		GroupOfPossibilities<T> result = new GroupOfPossibilities<T>();

		for (String row : rows)
		{
			String[] cols = row.split(COL_SEP);

			T item = typeFromString(cols[0]);
			int perc = Integer.parseInt(cols[1]);

			result.add(item, perc);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public abstract T typeFromString(String s);

	/*-------------------------------------------------------------------------*/
	public abstract String typeToString(T t);
}

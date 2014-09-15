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

import mclachlan.maze.stat.CurMaxSub;

/**
 *
 */
public class V1CurMaxSub
{
	public static final String SEP = "-";

	public static String toString(CurMaxSub cm)
	{
		return cm.getCurrent()+SEP+cm.getMaximum()+SEP+cm.getSub();
	}

	public static CurMaxSub fromString(String s)
	{
		String[] strs = s.split(SEP);
		return new CurMaxSub(
			Integer.parseInt(strs[0]),
			Integer.parseInt(strs[1]),
			Integer.parseInt(strs[2]));
	}
}

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

import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.CurMaxSub;

/**
 *
 */
public class V1Stats
{
	public static final String SEP = ";";

	/*-------------------------------------------------------------------------*/
	public static String toString(Stats stats)
	{
		StringBuilder s = new StringBuilder();

		s.append(V1CurMaxSub.toString(stats.getHitPoints()));
		s.append(SEP);
		s.append(V1CurMax.toString(stats.getActionPoints()));
		s.append(SEP);
		s.append(V1CurMax.toString(stats.getMagicPoints()));
		s.append(SEP);
		s.append(V1StatModifier.toString(stats.getModifiers()));

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Stats fromString(String s)
	{
		String[] strs = s.split(SEP, -1);

		CurMaxSub hp = V1CurMaxSub.fromString(strs[0]);
		CurMax sp = V1CurMax.fromString(strs[1]);
		CurMax mp = V1CurMax.fromString(strs[2]);
		StatModifier modifiers = V1StatModifier.fromString(strs[3]);

		Stats result = new Stats();
		result.setHitPoints(hp);
		result.setActionPoints(sp);
		result.setMagicPoints(mp);
		result.setModifiers(modifiers);
		return result;
	}
}

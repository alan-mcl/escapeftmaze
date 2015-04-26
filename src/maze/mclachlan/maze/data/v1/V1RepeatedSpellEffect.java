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

import mclachlan.maze.stat.condition.RepeatedSpellEffect;

/**
 *
 */
public class V1RepeatedSpellEffect
{
	public static final String SEP=":";

	/*-------------------------------------------------------------------------*/
	public static RepeatedSpellEffect fromString(String s)
	{
		String[] strs = s.split(SEP);

		String spellEffect = strs[0];
		int startTurn = Integer.parseInt(strs[1]);
		int endTurn = Integer.parseInt(strs[2]);
		int turnMod = Integer.parseInt(strs[3]);
		int prob = Integer.parseInt(strs[4]);

		return new RepeatedSpellEffect(startTurn, endTurn, turnMod, prob, spellEffect);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(RepeatedSpellEffect rse)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(rse.getSpellEffect());
		sb.append(SEP);
		sb.append(rse.getStartTurn());
		sb.append(SEP);
		sb.append(rse.getEndTurn());
		sb.append(SEP);
		sb.append(rse.getTurnMod());
		sb.append(SEP);
		sb.append(rse.getProbability());

		return sb.toString();
	}
}

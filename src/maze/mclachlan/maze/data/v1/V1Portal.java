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

import mclachlan.maze.map.Portal;
import java.awt.*;
import java.util.BitSet;

/**
 *
 */
public class V1Portal
{
	static final String SEP = ",";
	static final String SUB_SEP = ":";

	/*-------------------------------------------------------------------------*/
	public static String toString(Portal t)
	{
		if (t == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		s.append(t.getMazeVariable());
		s.append(SEP);
		s.append(t.isTwoWay());
		s.append(SEP);
		s.append(t.getInitialState());
		s.append(SEP);
		s.append(V1Point.toString(t.getFrom()));
		s.append(SEP);
		s.append(t.getFromFacing());
		s.append(SEP);
		s.append(V1Point.toString(t.getTo()));
		s.append(SEP);
		s.append(t.getToFacing());
		s.append(SEP);
		s.append(t.canForce());
		s.append(SEP);
		s.append(t.canPick());
		s.append(SEP);
		s.append(t.canSpellPick());
		s.append(SEP);
		s.append(t.getHitPointCostToForce());
		s.append(SEP);
		s.append(t.getResistForce());
		s.append(SEP);
		s.append(V1Utils.toStringInts(t.getDifficulty(), SUB_SEP));
		s.append(SEP);
		s.append(V1BitSet.toString(t.getRequired()));
		s.append(SEP);
		s.append(t.getKeyItem());
		s.append(SEP);
		s.append(t.consumeKeyItem());
		s.append(SEP);
		s.append(t.getMazeScript()==null?"":t.getMazeScript());

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Portal fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP, -1);
		String mazeVariable = strs[0];
		boolean twoWay = Boolean.valueOf(strs[1]);
		String initialState = strs[2];
		Point from = V1Point.fromString(strs[3]);
		int fromFacing = Integer.parseInt(strs[4]);
		Point to = V1Point.fromString(strs[5]);
		int toFacing = Integer.parseInt(strs[6]);
		boolean canForce = Boolean.valueOf(strs[7]);
		boolean canPick = Boolean.valueOf(strs[8]);
		boolean canSpellPick = Boolean.valueOf(strs[9]);
		int hitPointCostToForce = Integer.parseInt(strs[10]);
		int resistForce = Integer.parseInt(strs[11]);
		int[] difficulty = V1Utils.fromStringInts(strs[12], SUB_SEP);
		BitSet required = V1BitSet.fromString(strs[13]);
		String keyItem = strs[14];
		boolean consumeKeyItem = Boolean.valueOf(strs[15]);
		String mazeScript = "".equals(strs[16])?null:strs[16];

		return new Portal(
			mazeVariable,
			initialState,
			from,
			fromFacing, 
			to,
			toFacing, 
			twoWay,
			canForce,
			canPick,
			canSpellPick,
			hitPointCostToForce,
			resistForce,
			difficulty,
			required,
			keyItem,
			consumeKeyItem,
			mazeScript);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
	}
}

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

package mclachlan.maze.map;

import java.util.BitSet;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Trap
{
	/** Name of this trap */
	String name;

	/**
	 * Indices correspond to Tool constants; value indicates difficulty with that
	 * tool.  0 indicates not required.
	 */
	int[] difficulty;

	/**
	 * Indices indicate if a given tool is required (1) or not (0).
	 */
	BitSet required;

	/** The nasty effects of this trap */
	TileScript payload;

	/*-------------------------------------------------------------------------*/
	public Trap(String name, int[] difficulty, BitSet required, TileScript payload)
	{
		this.difficulty = difficulty;
		this.name = name;
		this.required = required;
		this.payload = payload;
	}

	/*-------------------------------------------------------------------------*/
	public int[] getDifficulty()
	{
		return difficulty;
	}

	public BitSet getRequired()
	{
		return required;
	}

	public String getName()
	{
		return name;
	}

	public TileScript getPayload()
	{
		return payload;
	}

	/*-------------------------------------------------------------------------*/
	public void setDifficulty(int[] difficulty)
	{
		this.difficulty = difficulty;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPayload(TileScript payload)
	{
		this.payload = payload;
	}

	public void setRequired(BitSet required)
	{
		this.required = required;
	}

	/*-------------------------------------------------------------------------*/
	public static class Tool
	{
		public static final int CHISEL = 0;
		public static final int CROWBAR = 1;
		public static final int DRILL = 2;
		public static final int HAMMER = 3;
		public static final int JACKKNIFE = 4;
		public static final int LOCKPICK = 5;
		public static final int SKELETON_KEY = 6;
		public static final int TENSION_WRENCH = 7;

		public static final int MAX_TOOLS = 8;

		public static String toString(int i)
		{
			switch (i)
			{
				case CHISEL: return "chisel";
				case CROWBAR: return "crowbar";
				case DRILL: return "drill";
				case HAMMER: return "hammer";
				case JACKKNIFE: return "jackknife";
				case LOCKPICK: return "lockpick";
				case SKELETON_KEY: return "skeleton key";
				case TENSION_WRENCH: return "tension wrench";
				default: throw new MazeException("Invalid tool "+i);
			}
		}

		public static int valueOf(String s)
		{
			if (s.equals("chisel")) return CHISEL;
			else if (s.equals("crowbar")) return CROWBAR;
			else if (s.equals("drill")) return DRILL;
			else if (s.equals("hammer")) return HAMMER;
			else if (s.equals("jackknife")) return JACKKNIFE;
			else if (s.equals("lockpick")) return LOCKPICK;
			else if (s.equals("skeleton key")) return SKELETON_KEY;
			else if (s.equals("tension wrench")) return TENSION_WRENCH;
			else throw new MazeException("Invalid tool ["+s+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class InspectionResult
	{
		public static final int PRESENT = 1;
		public static final int NOT_PRESENT = 2;
		public static final int UNKNOWN = 3;
	}

	/*-------------------------------------------------------------------------*/
	public static class DisarmResult
	{
		public static final int DISARMED = 1;
		public static final int NOTHING = 2;
		public static final int SPRING_TRAP = 3;
	}
}

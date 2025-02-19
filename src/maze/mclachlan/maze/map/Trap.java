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

import java.util.*;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Trap extends DataObject
{
	/**
	 * Name of this trap
	 */
	private String name;

	/**
	 * Indices correspond to Tool constants; value indicates difficulty with that
	 * tool.  0 indicates not required.
	 */
	private int[] difficulty;

	/**
	 * Indices indicate if a given tool is required (1) or not (0).
	 */
	private BitSet required;

	/**
	 * The nasty effects of this trap
	 */
	private TileScript payload;

	public Trap()
	{
	}

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		Trap trap = (Trap)o;

		if (getName() != null ? !getName().equals(trap.getName()) : trap.getName() != null)
		{
			return false;
		}
		if (!Arrays.equals(getDifficulty(), trap.getDifficulty()))
		{
			return false;
		}
		if (getRequired() != null ? !getRequired().equals(trap.getRequired()) : trap.getRequired() != null)
		{
			return false;
		}
		return getPayload() != null ? getPayload().equals(trap.getPayload()) : trap.getPayload() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + Arrays.hashCode(getDifficulty());
		result = 31 * result + (getRequired() != null ? getRequired().hashCode() : 0);
		result = 31 * result + (getPayload() != null ? getPayload().hashCode() : 0);
		return result;
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
			return switch (i)
				{
					case CHISEL -> "chisel";
					case CROWBAR -> "crowbar";
					case DRILL -> "drill";
					case HAMMER -> "hammer";
					case JACKKNIFE -> "jackknife";
					case LOCKPICK -> "lockpick";
					case SKELETON_KEY -> "skeleton key";
					case TENSION_WRENCH -> "tension wrench";
					default -> throw new MazeException("Invalid tool " + i);
				};
		}

		public static int valueOf(String s)
		{
			return switch (s)
				{
					case "chisel" -> CHISEL;
					case "crowbar" -> CROWBAR;
					case "drill" -> DRILL;
					case "hammer" -> HAMMER;
					case "jackknife" -> JACKKNIFE;
					case "lockpick" -> LOCKPICK;
					case "skeleton key" -> SKELETON_KEY;
					case "tension wrench" -> TENSION_WRENCH;
					default -> throw new MazeException("Invalid tool [" + s + "]");
				};
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

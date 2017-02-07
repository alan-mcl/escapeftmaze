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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A composite class for expressing values, for eg: <br>
 * <ul>
 * <li> 1d6 + THOUGHT
 * <li> 7 + 2d10
 * <li> 4
 * </ul>
 */
public class ValueList
{
	/**
	 * Other values that make up this computed value.
	 */
	private List<Value> values = new ArrayList<Value>();

	/*-------------------------------------------------------------------------*/
	public ValueList()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ValueList(Value... values)
	{
		for (Value v : values)
		{
			add(v);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Performs a deep clone of the given value list
	 */
	public ValueList(ValueList values)
	{
		for (Value v : values.getValues())
		{
			add(new Value(v));
		}
	}

	/*-------------------------------------------------------------------------*/
	public ValueList(List<Value> list)
	{
		for (Value v : list)
		{
			add(v);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void add(Value v)
	{
		this.values.add(v);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Computes this value based on the situation of the given Actor and an
	 * assumed casting level of 1.
	 *
	 * @return
	 * 	The computed value
	 */
	public int compute(UnifiedActor source)
	{
		return compute(source, 1);
	}
	
	/*-------------------------------------------------------------------------*/

	/**
	 * Computes this value based on the situation of the given Actor and the
	 * given casting level
	 *
	 * @return
	 * 	The computed value
	 */
	public int compute(UnifiedActor source, int castingLevel)
	{
		int result = 0;

		for (Value v : this.values)
		{
			result += v.compute(source, castingLevel);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Returns a clone of this ValueList, with as much precomputed as possible.
	 * This is used for example on values for Condition damage, where the damage
	 * ValueList must be calculated each turn based on the situation when it is
	 * cast.
	 */
	public ValueList getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		ValueList result = new ValueList();

		for (Value v : values)
		{
			result.add(v.getSnapShotValue(source, castingLevel));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Value> getValues()
	{
		return values;
	}

	/*-------------------------------------------------------------------------*/
	public void setValues(List<Value> values)
	{
		this.values = values;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (Value v : values)
		{
			sb.append(v.toString());
			sb.append(",");
		}

		return sb.toString();
	}
}

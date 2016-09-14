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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class StatModifier
{
	public static final StatModifier NULL_STAT_MODIFIER = new StatModifier()
	{
		public void setModifier(String modifier, int value)
		{
			throw new MazeException("Attempt to modify NULL_STAT_MODIFIER");
		}
	};

	/**
	 * Key: String(modifier name) <br>
	 * Value: Integer
	 */ 
	private Map<Stats.Modifier, Integer> modifiers = new HashMap<Stats.Modifier, Integer>();

	/*-------------------------------------------------------------------------*/
	public StatModifier()
	{
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier(StatModifier modifier)
	{
		if (modifier != null)
		{
			setModifiers(modifier);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<Stats.Modifier, Integer> getModifiers()
	{
		return Collections.unmodifiableMap(modifiers);
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(Stats.Modifier modifier)
	{
		Integer result = this.modifiers.get(modifier);
		if (result == null)
		{
			return 0;
		}
		return result.intValue();
	}
	
	/*-------------------------------------------------------------------------*/
	public void setModifier(Stats.Modifier modifier, int value)
	{
		this.modifiers.put(modifier, value);		
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets all the modifiers of this StatModifier to be the same as the given
	 * StatModifier
	 */
	public void setModifiers(StatModifier stats)
	{
		this.modifiers = new HashMap<Stats.Modifier, Integer>(stats.modifiers);
	}

	/*-------------------------------------------------------------------------*/
	public void addModifiers(StatModifier stats)
	{
		for (Stats.Modifier key : stats.getModifiers().keySet())
		{
			int value = stats.getModifier(key);
			if (this.modifiers.containsKey(key))
			{
				value += this.modifiers.get(key);
			}
			this.modifiers.put(key, value);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void incModifier(Stats.Modifier modifier, int value)
	{
		setModifier(modifier, getModifier(modifier)+value);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEmpty()
	{
		return this.modifiers == null || this.modifiers.isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public boolean equals(Object obj)
	{
		if (!(obj instanceof StatModifier))
		{
			return false;
		}

		StatModifier other = (StatModifier)obj;
		return this.modifiers.equals(other.modifiers);
	}

	/*-------------------------------------------------------------------------*/
	public int hashCode()
	{
		return this.modifiers.hashCode();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("StatModifier");
		sb.append("{modifiers=").append(modifiers);
		sb.append('}');
		return sb.toString();
	}
}

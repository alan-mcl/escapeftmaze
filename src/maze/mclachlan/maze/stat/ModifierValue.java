/*
 * Copyright (c) 2013 Alan McLachlan
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

/**
 * Class to hold a returned modifier value and a list of things that have
 * impacted it.
 */
public class ModifierValue
{
	private int value;
	private List<ModifierInfluence> influences = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	public ModifierValue()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ModifierValue(String name, int n)
	{
		value = n;
		influences.add(new ModifierInfluence(name, n));
	}

	/*-------------------------------------------------------------------------*/
	public void add(String name, int n)
	{
		if (n == 0)
		{
			return;
		}

		value += n;
		influences.add(new ModifierInfluence(name, n));
	}

	/*-------------------------------------------------------------------------*/
	public void add(ModifierValue m)
	{
		if (m == null || m.value == 0)
		{
			return;
		}

		value += m.value;
		influences.addAll(m.influences);
	}

	/*-------------------------------------------------------------------------*/
	public void addAll(List<ModifierValue> modifierValues)
	{
		for (ModifierValue mv : modifierValues)
		{
			add(mv);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<ModifierInfluence> getInfluences()
	{
		return influences;
	}

	/*-------------------------------------------------------------------------*/
	public int getValue()
	{
		return value;
	}

	/*-------------------------------------------------------------------------*/
	public static class ModifierInfluence
	{
		private String name;
		private int value;

		public ModifierInfluence(String name, int value)
		{
			this.name = name;
			this.value = value;
		}

		public String getName()
		{
			return name;
		}

		public int getValue()
		{
			return value;
		}
	}
}

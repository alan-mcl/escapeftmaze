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

package mclachlan.jgpgoap.goap;

/**
 * Represents an atomic piece of world state for the planner
 */
public class Atom
{
	private String name;
	private boolean value;

	/*-------------------------------------------------------------------------*/
	public Atom(String name, boolean value)
	{
		this.name = name;
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean getValue()
	{
		return value;
	}

	public void setValue(boolean value)
	{
		this.value = value;
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

		Atom atom = (Atom)o;

		if (value != atom.value)
		{
			return false;
		}
		if (name != null ? !name.equals(atom.name) : atom.name != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (value ? 1 : 0);
		return result;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("=");
		sb.append(value);
		return sb.toString();
	}
}

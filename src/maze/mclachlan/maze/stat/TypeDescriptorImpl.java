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

import mclachlan.maze.data.v2.V2DataObject;

/**
 *
 */
public class TypeDescriptorImpl implements TypeDescriptor, V2DataObject
{
	private String name;

	public TypeDescriptorImpl(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String newName)
	{
		this.name = name;
	}

	@Override
	public Stats.Modifier getFavouredEnemyModifier()
	{
		return null;
	}

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

		TypeDescriptorImpl that = (TypeDescriptorImpl)o;

		return getName() != null ? getName().equals(that.getName()) : that.getName() == null;
	}

	@Override
	public int hashCode()
	{
		return getName() != null ? getName().hashCode() : 0;
	}
}

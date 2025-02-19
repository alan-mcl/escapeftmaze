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
import mclachlan.maze.data.v1.DataObject;

/**
 * A set of spells that player characters can learn.
 */
public class PlayerSpellBook extends DataObject
{
	private String name;
	private String description;
	private List<String> spellNames;

	public PlayerSpellBook()
	{
	}

	/*-------------------------------------------------------------------------*/
	public PlayerSpellBook(String name, String description, List<String> spellNames)
	{
		this.name = name;
		this.spellNames = spellNames;
		this.description = description;
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<String> getSpellNames()
	{
		return spellNames;
	}

	public void setSpellNames(List<String> spellNames)
	{
		this.spellNames = spellNames;
	}

	/*-------------------------------------------------------------------------*/
	public void addSpell(String spellName)
	{
		this.spellNames.add(spellName);
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

		PlayerSpellBook that = (PlayerSpellBook)o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
		{
			return false;
		}
		return getSpellNames() != null ? getSpellNames().equals(that.getSpellNames()) : that.getSpellNames() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getSpellNames() != null ? getSpellNames().hashCode() : 0);
		return result;
	}
}

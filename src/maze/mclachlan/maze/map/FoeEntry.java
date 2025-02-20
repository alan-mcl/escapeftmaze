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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.*;

/**
 * Represents a row in a random encounter table.
 */
public class FoeEntry extends DataObject
{
	private String name;
	private GroupOfPossibilities<FoeEntryRow> contains;

	public FoeEntry()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FoeEntry(String name, GroupOfPossibilities<FoeEntryRow> contains)
	{
		this.name = name;
		this.contains = contains;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities<FoeEntryRow> getContains()
	{
		return contains;
	}

	/*-------------------------------------------------------------------------*/
	public void setContains(GroupOfPossibilities<FoeEntryRow> contains)
	{
		this.contains = contains;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	spawn a list of foes, unsorted, from this FoeEntry
	 */
	public List<FoeGroup> generate()
	{
		List<FoeGroup> result = new ArrayList<>();

		List<FoeEntryRow> entries = contains.getRandom();

		for (FoeEntryRow t : entries)
		{
			FoeTemplate foeTemplate = Database.getInstance().getFoeTemplate(t.getFoeName());
			int max = t.getQuantity().roll("Foe entry generate ["+getName()+"]");
			FoeGroup fg = new FoeGroup();

			for (int i=0; i<max; i++)
			{
				Foe foe = foeTemplate.create();
				fg.add(foe);
			}

			result.add(fg);
		}

		return result;
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

		FoeEntry foeEntry = (FoeEntry)o;

		if (getName() != null ? !getName().equals(foeEntry.getName()) : foeEntry.getName() != null)
		{
			return false;
		}
		return getContains() != null ? getContains().equals(foeEntry.getContains()) : foeEntry.getContains() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getContains() != null ? getContains().hashCode() : 0);
		return result;
	}
}

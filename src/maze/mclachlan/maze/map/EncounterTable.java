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

import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class EncounterTable extends DataObject
{
	private String name;
	private PercentageTable<FoeEntry> encounterTable;

	public EncounterTable()
	{
	}

	/*-------------------------------------------------------------------------*/
	public EncounterTable(String name, PercentageTable<FoeEntry> encounterTable)
	{
		this.encounterTable = encounterTable;
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<FoeEntry> getEncounterTable()
	{
		return encounterTable;
	}

	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setEncounterTable(PercentageTable<FoeEntry> encounterTable)
	{
		this.encounterTable = encounterTable;
	}

	public void setName(String name)
	{
		this.name = name;
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

		EncounterTable that = (EncounterTable)o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		return getEncounterTable() != null ? getEncounterTable().equals(that.getEncounterTable()) : that.getEncounterTable() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getEncounterTable() != null ? getEncounterTable().hashCode() : 0);
		return result;
	}
}

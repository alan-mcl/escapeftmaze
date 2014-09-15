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

import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class EncounterTable
{
	String name;
	PercentageTable<FoeEntry> encounterTable;

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
}

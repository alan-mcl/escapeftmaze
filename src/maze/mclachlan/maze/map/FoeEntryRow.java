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

import mclachlan.maze.stat.Dice;

/**
 *
 */
public class FoeEntryRow
{
	private String foeName;
	private Dice quantity;

	public FoeEntryRow()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FoeEntryRow(String foeName, Dice quantity)
	{
		this.foeName = foeName;
		this.quantity = quantity;
	}

	/*-------------------------------------------------------------------------*/
	public String getFoeName()
	{
		return foeName;
	}

	public Dice getQuantity()
	{
		return quantity;
	}

	public void setFoeName(String foeName)
	{
		this.foeName = foeName;
	}

	public void setQuantity(Dice quantity)
	{
		this.quantity = quantity;
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

		FoeEntryRow that = (FoeEntryRow)o;

		if (getFoeName() != null ? !getFoeName().equals(that.getFoeName()) : that.getFoeName() != null)
		{
			return false;
		}
		return getQuantity() != null ? getQuantity().equals(that.getQuantity()) : that.getQuantity() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getFoeName() != null ? getFoeName().hashCode() : 0;
		result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
		return result;
	}
}

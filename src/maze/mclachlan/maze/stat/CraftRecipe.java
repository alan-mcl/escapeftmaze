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

import mclachlan.maze.data.v1.DataObject;

/**
 *
 */
public class CraftRecipe extends DataObject
{
	private String name;
	private StatModifier requirements;
	private String item1;
	private String item2;
	private String resultingItem;

	public CraftRecipe()
	{
	}

	/*-------------------------------------------------------------------------*/
	public CraftRecipe(
		String name, StatModifier requirements,
		String item1,
		String item2,
		String resultingItem)
	{
		this.name = name;
		this.requirements = requirements;
		this.item1 = item1;
		this.item2 = item2;
		this.resultingItem = resultingItem;
	}

	/*-------------------------------------------------------------------------*/

	public String getName()
	{
		return name;
	}

	public StatModifier getRequirements()
	{
		return requirements;
	}

	public String getItem1()
	{
		return item1;
	}

	public String getItem2()
	{
		return item2;
	}

	public String getResultingItem()
	{
		return resultingItem;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setRequirements(StatModifier requirements)
	{
		this.requirements = requirements;
	}

	public void setItem1(String item1)
	{
		this.item1 = item1;
	}

	public void setItem2(String item2)
	{
		this.item2 = item2;
	}

	public void setResultingItem(String resultingItem)
	{
		this.resultingItem = resultingItem;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isMatch(String name1, String name2)
	{
		return (name1.equals(item1) && name2.equals(item2)) ||
			(name1.equals(item2) && name2.equals(item1));
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

		CraftRecipe that = (CraftRecipe)o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		if (getRequirements() != null ? !getRequirements().equals(that.getRequirements()) : that.getRequirements() != null)
		{
			return false;
		}
		if (getItem1() != null ? !getItem1().equals(that.getItem1()) : that.getItem1() != null)
		{
			return false;
		}
		if (getItem2() != null ? !getItem2().equals(that.getItem2()) : that.getItem2() != null)
		{
			return false;
		}
		return getResultingItem() != null ? getResultingItem().equals(that.getResultingItem()) : that.getResultingItem() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getRequirements() != null ? getRequirements().hashCode() : 0);
		result = 31 * result + (getItem1() != null ? getItem1().hashCode() : 0);
		result = 31 * result + (getItem2() != null ? getItem2().hashCode() : 0);
		result = 31 * result + (getResultingItem() != null ? getResultingItem().hashCode() : 0);
		return result;
	}
}
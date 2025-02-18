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
public class Gender extends DataObject
{
	private String name;

	/** once off bonus applied to a new character of this gender */
	private StatModifier startingModifiers;
	/** constantly applied to characters of this gender */
	private StatModifier constantModifiers;
	/** constantly applied to the whole party */
	private StatModifier bannerModifiers;

	/*-------------------------------------------------------------------------*/
	public Gender()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Gender(
		String name,
		StatModifier startingModifiers,
		StatModifier constantModifiers,
		StatModifier bannerModifiers)
	{
		this.name = name;
		this.constantModifiers = constantModifiers;
		this.startingModifiers = startingModifiers;
		this.bannerModifiers = bannerModifiers;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public StatModifier getBannerModifiers()
	{
		return bannerModifiers;
	}

	public StatModifier getConstantModifiers()
	{
		return constantModifiers;
	}

	public StatModifier getStartingModifiers()
	{
		return startingModifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setBannerModifiers(StatModifier bannerModifiers)
	{
		this.bannerModifiers = bannerModifiers;
	}

	public void setConstantModifiers(StatModifier constantModifiers)
	{
		this.constantModifiers = constantModifiers;
	}

	public void setStartingModifiers(StatModifier startingModifiers)
	{
		this.startingModifiers = startingModifiers;
	}

	@Override
	public String toString()
	{
		return "Gender{" +
			"name='" + name + '\'' +
			"campaign='" + super.getCampaign() + '\'' +
			"} ";
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

		Gender gender = (Gender)o;

		if (!getName().equals(gender.getName()))
		{
			return false;
		}
		if (getStartingModifiers() != null ? !getStartingModifiers().equals(gender.getStartingModifiers()) : gender.getStartingModifiers() != null)
		{
			return false;
		}
		if (getConstantModifiers() != null ? !getConstantModifiers().equals(gender.getConstantModifiers()) : gender.getConstantModifiers() != null)
		{
			return false;
		}
		return getBannerModifiers() != null ? getBannerModifiers().equals(gender.getBannerModifiers()) : gender.getBannerModifiers() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + (getStartingModifiers() != null ? getStartingModifiers().hashCode() : 0);
		result = 31 * result + (getConstantModifiers() != null ? getConstantModifiers().hashCode() : 0);
		result = 31 * result + (getBannerModifiers() != null ? getBannerModifiers().hashCode() : 0);
		return result;
	}
}

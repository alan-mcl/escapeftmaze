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

/**
 *
 */
public class Gender
{
	private String name;

	/** once off bonus applied to a new character of this gender */
	private StatModifier startingModifiers;
	/** constantly applied to characters of this gender */
	private StatModifier constantModifiers;
	/** constantly applied to the whole party */
	private StatModifier bannerModifiers;

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
}

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

/**
 *
 */
public class CloudSpellResult extends SpellResult
{
	/** The duration for this condition */
	ValueList duration;

	/** The strength of this condition */
	ValueList strength;

	/** The Spell delivered every turn */
	String spell;

	/** the icon for this cloud effect */
	String icon;

	/*-------------------------------------------------------------------------*/
	public CloudSpellResult(
		ValueList duration,
		ValueList strength,
		String icon,
		String spell)
	{
		this.duration = duration;
		this.icon = icon;
		this.spell = spell;
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getDuration()
	{
		return duration;
	}

	public void setDuration(ValueList duration)
	{
		this.duration = duration;
	}

	public String getSpell()
	{
		return spell;
	}

	public void setSpell(String effect)
	{
		this.spell = effect;
	}

	public ValueList getStrength()
	{
		return strength;
	}

	public void setStrength(ValueList strength)
	{
		this.strength = strength;
	}

	public String getIcon()
	{
		return icon;
	}
}

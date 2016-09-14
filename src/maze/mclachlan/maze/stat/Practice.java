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

import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Practice
{
	private StatModifier modifiers = new StatModifier();

	/*-------------------------------------------------------------------------*/
	public Practice()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Practice(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}
	
	/*-------------------------------------------------------------------------*/
	public Practice(Practice p)
	{
		this.modifiers = new StatModifier(p.modifiers);
	}

	/*-------------------------------------------------------------------------*/
	public int getPracticePoints(Stats.Modifier modifier)
	{
		if (!Stats.regularModifiers.contains(modifier))
		{
			throw new MazeException("Can't practice a non regular modifier");
		}

		Integer result = this.modifiers.getModifier(modifier);
		if (result == null)
		{
			return 0;
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setPracticePoints(Stats.Modifier modifier, int value)
	{
		if (!Stats.regularModifiers.contains(modifier))
		{
			throw new MazeException("Can't practice a non regular modifier");
		}

		this.modifiers.setModifier(modifier, value);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}
}

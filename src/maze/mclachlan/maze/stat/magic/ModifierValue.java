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

import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A value based on a modifier.
 */
public class ModifierValue extends Value
{
	private Stats.Modifier modifier;

	/*-------------------------------------------------------------------------*/
	public ModifierValue(Stats.Modifier modifier)
	{
		this.modifier = modifier;
	}
	
	/*-------------------------------------------------------------------------*/
	public int compute(UnifiedActor source, int castingLevel)
	{
		int result = source.getModifier(modifier);

		result *= computeScale(source, castingLevel);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Value getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		// transform this Modifier ValueList into a constant value based on the
		// source's current modifier.  It won't need to be scaled by casting level
		// in the future.
		Value result = new Value(this.compute(source, castingLevel), SCALE.NONE);
		result.setValue(this.getValue());
		result.setNegate(this.shouldNegate());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return modifier.toString();
	}

	/*-------------------------------------------------------------------------*/
	public Stats.Modifier getModifier()
	{
		return modifier;
	}
}

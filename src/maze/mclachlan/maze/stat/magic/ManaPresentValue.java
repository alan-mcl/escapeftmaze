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

import mclachlan.maze.stat.UnifiedActor;

/**
 * A value based on the amount of a certain colour of mana present.
 */
public class ManaPresentValue extends Value
{
	private int colour;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param colour
	 * 	A contant from {@link mclachlan.maze.stat.magic.MagicSys.ManaType}
	 */
	public ManaPresentValue(int colour)
	{
		this.colour = colour;
	}
	
	/*-------------------------------------------------------------------------*/
	public int compute(UnifiedActor source, int castingLevel)
	{
		// the actor accumulates mana from the tile
		int result = source.getModifier(MagicSys.ManaType.getModifier(colour));

		result *= computeScale(source, castingLevel);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Value getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		// We actually want this Value to change as the mana present changes.
		ManaPresentValue result = new ManaPresentValue(colour);
		result.setValue(this.getValue());
		result.setNegate(this.shouldNegate());
		result.setScaling(this.getScaling());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return MagicSys.ManaType.describe(colour);
	}

	/*-------------------------------------------------------------------------*/
	public int getColour()
	{
		return colour;
	}
}

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
 * A value based on the amount of a certain colour of magic present.
 */
public class MagicPresentValue extends Value
{
	private int colour;

	public MagicPresentValue()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param colour
	 * 	A contant from {@link MagicSys.MagicColour}
	 */
	public MagicPresentValue(int colour)
	{
		this.colour = colour;
	}
	
	/*-------------------------------------------------------------------------*/
	public int compute(UnifiedActor source, int castingLevel)
	{
		// the actor accumulates magic from the tile
		int result = source.getModifier(MagicSys.MagicColour.getModifier(colour));

		result *= computeScale(source, castingLevel);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Value getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		// We actually want this ValueList to change as the magic present changes.
		MagicPresentValue result = new MagicPresentValue(colour);
		result.setValue(this.getValue());
		result.setShouldNegate(this.isShouldNegate());
		result.setScaling(this.getScaling());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return MagicSys.MagicColour.describe(colour);
	}

	/*-------------------------------------------------------------------------*/
	public int getColour()
	{
		return colour;
	}

	public void setColour(int colour)
	{
		this.colour = colour;
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
		if (!super.equals(o))
		{
			return false;
		}

		MagicPresentValue that = (MagicPresentValue)o;

		return getColour() == that.getColour();
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + getColour();
		return result;
	}
}

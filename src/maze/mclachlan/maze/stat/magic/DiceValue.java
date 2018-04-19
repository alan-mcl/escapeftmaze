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

import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A value computed from a dice.
 */
public class DiceValue extends Value
{
	private Dice dice;

	/*-------------------------------------------------------------------------*/
	public DiceValue(Dice source)
	{
		this.dice = source;
	}
	
	/*-------------------------------------------------------------------------*/
	public int compute(UnifiedActor source, int castingLevel)
	{
		int result = 0;

		int iterations = computeScale(source, castingLevel);

		for (int i=0; i< iterations; i++)
		{
			result += this.dice.roll("DiceValue");
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Dice getDice()
	{
		return dice;
	}

	/*-------------------------------------------------------------------------*/
	public Value getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		// simply return a clone of this, no snapshotting required
		DiceValue result = new DiceValue(dice);
		result.setValue(this.getValue());
		result.setNegate(this.shouldNegate());
		result.setScaling(this.getScaling());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		String result = dice.toString();

		if (getScaling() == Value.SCALE.SCALE_WITH_CASTING_LEVEL)
		{
			result += " per casting level";
		}
		else if (getScaling() == Value.SCALE.SCALE_WITH_CHARACTER_LEVEL)
		{
			result += " per character level";
		}
		else if (getScaling() == Value.SCALE.SCALE_WITH_CLASS_LEVEL)
		{
			result += " per class level ("+getReference()+")";
		}

		return result;
	}
}

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

import mclachlan.maze.data.v2.V2DataObject;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.util.MazeException;

/**
 * A composite class for expressing values, for eg: <br>
 * <ul>
 * <li> 1d6 + THOUGHT
 * <li> 7 + 2d10
 * <li> 4
 * </ul>
 * This base class just contains a constant.
 */
public class Value implements V2DataObject
{
	/**
	 * The base constant
	 */ 
	private int value;

	/**
	 * Whether or not to scale this ValueList with the spell casting level.
	 */
	private SCALE scaling;

	/**
	 * Any specific reference data for this value.
	 */
	private String reference;

	/**
	 * Whether or not to negate this value.
	 */
	private boolean negate;

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public void setName(String newName)
	{

	}

	/**
	 * Different ways of scaling this value.
	 */
	public enum SCALE
	{
		/** no scaling */
		NONE,
		/** scales with the casting level*/
		SCALE_WITH_CASTING_LEVEL,
		/** scales with the total character level of the caster*/
		SCALE_WITH_CHARACTER_LEVEL,
		/** scales with the class level. Reference property must be the class name */
		SCALE_WITH_CLASS_LEVEL,
		/** scales with a modifier. Reference property must be the modifier name */
		SCALE_WITH_MODIFIER,
		/** scales with the number of characters in the party */
		SCALE_WITH_PARTY_SIZE
	}

	/*-------------------------------------------------------------------------*/
	public Value()
	{
		this(0, SCALE.NONE);
	}

	/*-------------------------------------------------------------------------*/
	public Value(int value, SCALE scale)
	{
		this.value = value;
		this.scaling = scale;
	}

	/*-------------------------------------------------------------------------*/
	public Value(Value other)
	{
		this.value = other.getValue();
		this.scaling = other.getScaling();
		this.negate = other.isShouldNegate();
		this.reference = other.getReference();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Computes this value based on the situation of the given Actor and an
	 * assumed casting level of 1.
	 *
	 * @return
	 * 	The computed value
	 */
	public int compute(UnifiedActor source)
	{
		return compute(source, 1);
	}
	
	/*-------------------------------------------------------------------------*/

	/**
	 * Computes this value based on the situation of the given Actor and the
	 * given casting level
	 *
	 * @return
	 * 	The computed value
	 */
	public int compute(UnifiedActor source, int castingLevel)
	{
		int result;

		result = this.value * computeScale(source, castingLevel);

		if (negate)
		{
			result = -result;
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Computes the current scale of this value.
	 */
	protected int computeScale(UnifiedActor source, int castingLevel)
	{
		switch (getScaling())
		{
			case NONE:
				return 1;

			case SCALE_WITH_CASTING_LEVEL:
				return castingLevel;

			case SCALE_WITH_CHARACTER_LEVEL:
				return source.getLevel();

			case SCALE_WITH_CLASS_LEVEL:
				return source.getLevel(getReference());

			case SCALE_WITH_MODIFIER:
				return source.getModifier(Stats.Modifier.valueOf(getReference()));

			case SCALE_WITH_PARTY_SIZE:
				if (source.getActorGroup() != null)
				{
					return source.getActorGroup().getActors().size();
				}
				else
				{
					return 1;
				}

			default: throw new MazeException("invalid "+getScaling());
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns a clone of this ValueList, with as much precomputed as possible.
	 * This is used for example on values for Condition damage, where the damage
	 * ValueList must be calculated each turn based on the situation when it is
	 * cast.
	 */
	public Value getSnapShotValue(UnifiedActor source, int castingLevel)
	{
		// the default const value requires no snap-shotting
		return new Value(this.value, this.scaling);
	}

	/*-------------------------------------------------------------------------*/
	public SCALE getScaling()
	{
		return scaling;
	}

	/*-------------------------------------------------------------------------*/
	public void setScaling(SCALE scaling)
	{
		this.scaling = scaling;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The constant portion of this ValueList. For example, "2d4+3" will return "3".
	 */
	public int getValue()
	{
		return value;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setValue(int value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/

	public String getReference()
	{
		return reference;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isShouldNegate()
	{
		return negate;
	}

	/*-------------------------------------------------------------------------*/
	public void setShouldNegate(boolean negate)
	{
		this.negate = negate;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Value{");
		sb.append(value);
		sb.append(",").append(scaling);
		sb.append(",").append(reference);
		sb.append(",").append(negate);
		sb.append('}');
		return sb.toString();
	}

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

		Value value1 = (Value)o;

		if (getValue() != value1.getValue())
		{
			return false;
		}
		if (negate != value1.negate)
		{
			return false;
		}
		if (getScaling() != value1.getScaling())
		{
			return false;
		}
		return getReference() != null ? getReference().equals(value1.getReference()) : value1.getReference() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getValue();
		result = 31 * result + (getScaling() != null ? getScaling().hashCode() : 0);
		result = 31 * result + (getReference() != null ? getReference().hashCode() : 0);
		result = 31 * result + (negate ? 1 : 0);
		return result;
	}
}

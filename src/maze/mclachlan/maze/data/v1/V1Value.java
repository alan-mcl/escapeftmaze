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

package mclachlan.maze.data.v1;

import java.util.*;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Value
{
	static final String ROW_SEP = ",";
	static final String COL_SEP = "/";
	static Map<Class, Integer> types;

	private static final int CUSTOM = 0;
	private static final int VALUE = 1;
	private static final int DICE_VALUE = 2;
	private static final int MODIFIER_VALUE = 3;
	private static final int MAGIC_PRESENT_VALUE = 4;

	static
	{
		types = new HashMap<>();

		types.put(Value.class, VALUE);
		types.put(DiceValue.class, DICE_VALUE);
		types.put(ModifierValue.class, MODIFIER_VALUE);
		types.put(MagicPresentValue.class, MAGIC_PRESENT_VALUE);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(ValueList v)
	{
		return toString(v, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/

	public static String toString(ValueList value, String rowSep, String colSep)
	{
		if (value == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		for (Value v : value.getValues())
		{
			int type;
			if (types.containsKey(v.getClass()))
			{
				type = types.get(v.getClass());
			}
			else
			{
				type = CUSTOM;
			}

			s.append(type);
			s.append(colSep);
			s.append(v.getValue());
			s.append(colSep);
			s.append(v.getScaling());
			s.append(colSep);
			s.append(v.getReference()==null?"":v.getReference());
			s.append(colSep);
			s.append(v.shouldNegate());

			if (type == CUSTOM)
			{
				s.append(colSep);
				s.append(v.getClass().getName());
			}
			else if (type == DICE_VALUE)
			{
				s.append(colSep);
				s.append(V1Dice.toString(((DiceValue)v).getDice()));
			}
			else if (type == MODIFIER_VALUE)
			{
				s.append(colSep);
				s.append(((ModifierValue)v).getModifier());
			}
			else if (type == MAGIC_PRESENT_VALUE)
			{
				s.append(colSep);
				s.append(((MagicPresentValue)v).getColour());
			}

			s.append(rowSep);
		}

		if (s.lastIndexOf(rowSep)>0)
		{
			s.deleteCharAt(s.lastIndexOf(rowSep));
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static ValueList fromString(String s)
	{
		return fromString(s, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static ValueList fromString(String s, String rowSep, String colSep)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		// since hierarchy doesn't matter, treat it as flat
		String[] rows = s.split(rowSep);
		String[] cols;

		ValueList baseValue = new ValueList();

		ArrayList<Value> values = new ArrayList<>();

		for (int i=0; i<rows.length; i++)
		{
			cols = rows[i].split(colSep);
			values.add(getValue(cols));
		}
		baseValue.setValues(values);

		return baseValue;
	}

	/*-------------------------------------------------------------------------*/
	private static Value getValue(String[] cols)
	{
		int col = 0;
		
		int type = Integer.parseInt(cols[col++]);
		int constant = Integer.parseInt(cols[col++]);
		Value.SCALE scale = Value.SCALE.valueOf(cols[col++]);
		String ref = cols[col++];
		if (ref.length() == 0)
		{
			ref = null;
		}

		boolean negate = Boolean.valueOf(cols[col++]);

		Value result;

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(cols[col++]);
					return (Value)clazz.newInstance();
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
			case VALUE:
				result = new Value(constant, scale);
				break;
			case DICE_VALUE:
				Dice d = V1Dice.fromString(cols[col++]);
				result =  new DiceValue(d);
				break;
			case MODIFIER_VALUE:
				String modifier = cols[col++];
				result = new ModifierValue(Stats.Modifier.valueOf(modifier));
				break;
			case MAGIC_PRESENT_VALUE:
				int colour = Integer.parseInt(cols[col++]);
				result = new MagicPresentValue(colour);
				break;
			default: throw new MazeException("Invalid type: "+type);
		}
		
		result.setValue(constant);
		result.setNegate(negate);
		result.setScaling(scale);
		result.setReference(ref);
		
		return result;
	}
}

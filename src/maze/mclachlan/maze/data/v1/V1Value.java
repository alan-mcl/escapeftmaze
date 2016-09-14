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
import mclachlan.maze.stat.magic.DiceValue;
import mclachlan.maze.stat.magic.ManaPresentValue;
import mclachlan.maze.stat.magic.ModifierValue;
import mclachlan.maze.stat.magic.Value;
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
	private static final int MANA_PRESENT_VALUE = 4;

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(Value.class, VALUE);
		types.put(DiceValue.class, DICE_VALUE);
		types.put(ModifierValue.class, MODIFIER_VALUE);
		types.put(ManaPresentValue.class, MANA_PRESENT_VALUE);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(Value v)
	{
		return toString(v, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return a simple list of values that makes sense out of the composition
	 */
	public static List<Value> simplify(Value v)
	{
		List<Value> result = new ArrayList<Value>();

		List<Value> values = v.getValues();
		if (!v.isNullValue())
		{
			v.setValues(new ArrayList<Value>());
			result.add(v);
		}

		for (Value value : values)
		{
			result.addAll(simplify(value));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(Value value, String rowSep, String colSep)
	{
		if (value == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		List<Value> values = simplify(value);

		for (Value v : values)
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
			else if (type == MANA_PRESENT_VALUE)
			{
				s.append(colSep);
				s.append(((ManaPresentValue)v).getColour());
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
	public static Value fromString(String s)
	{
		return fromString(s, ROW_SEP, COL_SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static Value fromString(String s, String rowSep, String colSep)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		// since hierarchy doesn't matter, treat it as flat
		String[] rows = s.split(rowSep);
		String[] cols;

		// Always encapsulate in an empty base value. This avoids odd composition
		// bugs when Value subclasses override methods like compute and toString
		Value baseValue = new Value();

		ArrayList<Value> values = new ArrayList<Value>();

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
			case MANA_PRESENT_VALUE:
				int colour = Integer.parseInt(cols[col++]);
				result = new ManaPresentValue(colour);
				break;
			default: throw new MazeException("Invalid type: "+type);
		}
		
		result.setValue(constant);
		result.setNegate(negate);
		result.setScaling(scale);
		result.setReference(ref);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
		Value v = new Value(2, Value.SCALE.NONE);
		String s = toString(v);
		System.out.println("s = [" + s + "]");
		Value x = fromString(s);
		System.out.println("x = [" + x + "]");

		v.add(new DiceValue(new Dice(3,7,-2)));
		s = toString(v);
		System.out.println("s = [" + s + "]");
		x = fromString(s);
		System.out.println("x = [" + x + "]");

		v.add(new ModifierValue(Stats.Modifier.BRAWN));
		s = toString(v);
		System.out.println("s = [" + s + "]");
		x = fromString(s);
		System.out.println("x = [" + x + "]");

		v.add(new ManaPresentValue(1));
		s = toString(v);
		System.out.println("s = [" + s + "]");
		x = fromString(s);
		System.out.println("x = [" + x + "]");

		Value w = new Value();
		w.add(new Value(1, Value.SCALE.SCALE_WITH_CASTING_LEVEL));
		w.add(new DiceValue(Dice.d1000));
		x = fromString(s);
		System.out.println("x = [" + x + "]");

		v.add(w);
		s = toString(v);
		System.out.println("s = [" + s + "]");
		x = fromString(s);
		System.out.println("x = [" + x + "]");
	}
}

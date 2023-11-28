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
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.script.JagObjectVertically;
import mclachlan.crusader.script.JagObjectWithinRadius;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1CrusaderObjectScript
{
	static final String SEP = ":";
	static final String SUB_SEP = "/";

	static Map<Class, Integer> types;

	private static final int CUSTOM = 0;
	private static final int JAG_VERTICALLY = 1;
	private static final int JAG_WITHIN_RADIUS = 2;

	static
	{
		types = new HashMap<>();

		types.put(JagObjectVertically.class, JAG_VERTICALLY);
		types.put(JagObjectWithinRadius.class, JAG_WITHIN_RADIUS);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(ObjectScript script)
	{
		if (script == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(script.getClass()))
		{
			type = types.get(script.getClass());
		}
		else
		{
			type = CUSTOM;
		}
		s.append(type);
		s.append(SEP);

		switch (type)
		{
			case CUSTOM:
				s.append(script.getClass().getName());
				break;
			case JAG_VERTICALLY:
				JagObjectVertically jv = (JagObjectVertically)script;
				s.append(jv.getMinOffset());
				s.append(SEP);
				s.append(jv.getMaxOffset());
				s.append(SEP);
				s.append(jv.getMinSpeed());
				s.append(SEP);
				s.append(jv.getMaxSpeed());
				break;
			case JAG_WITHIN_RADIUS:
				JagObjectWithinRadius jwr = (JagObjectWithinRadius)script;
				s.append(jwr.getMaxRadius());
				break;
			default: throw new MazeException("invalid type "+type);
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static ObjectScript fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		// since hierarchy doesn't matter, treat it as flat
		String[] strs = s.split(SEP);

		int type = Integer.parseInt(strs[0]);

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[1]);
					return (ObjectScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
			case JAG_VERTICALLY:
				int minOffset = Integer.parseInt(strs[1]);
				int maxOffset = Integer.parseInt(strs[2]);
				int minSpeed = Integer.parseInt(strs[3]);
				int maxSpeed = Integer.parseInt(strs[4]);
				return new JagObjectVertically(minOffset, maxOffset, minSpeed, maxSpeed);
			case JAG_WITHIN_RADIUS:
				int maxRadius = Integer.parseInt(strs[1]);
				return new JagObjectWithinRadius(maxRadius);
			default: throw new MazeException("invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
	}
}

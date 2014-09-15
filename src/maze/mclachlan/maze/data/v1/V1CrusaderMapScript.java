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

import mclachlan.crusader.MapScript;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.crusader.script.SinusoidalLightingScript;
import java.util.HashMap;
import java.util.Map;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1CrusaderMapScript
{
	static final String SEP = ",";
	static final String SUB_SEP = "/";

	static Map<Class, Integer> types;

	private static final int CUSTOM = 0;
	private static final int SINE_LIGHTING = 1;
	private static final int RANDOM_LIGHTING = 2;

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(SinusoidalLightingScript.class, SINE_LIGHTING);
		types.put(RandomLightingScript.class, RANDOM_LIGHTING);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(MapScript script)
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
			case SINE_LIGHTING:
				SinusoidalLightingScript sls = (SinusoidalLightingScript)script;
				s.append(sls.getFrequency());
				s.append(SEP);
				s.append(sls.getMinLightLevel());
				s.append(SEP);
				s.append(sls.getMaxLightLevel());
				s.append(SEP);
				s.append(V1Utils.toStringInts(sls.getAffectedTiles(), SUB_SEP));
				break;
			case RANDOM_LIGHTING:
				RandomLightingScript rls = (RandomLightingScript)script;
				s.append(rls.getFrequency());
				s.append(SEP);
				s.append(rls.getMinLightLevel());
				s.append(SEP);
				s.append(rls.getMaxLightLevel());
				s.append(SEP);
				s.append(V1Utils.toStringInts(rls.getAffectedTiles(), SUB_SEP));
				break;
			default: throw new MazeException("invalid type "+type);
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static MapScript fromString(String s)
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
					return (MapScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
			case SINE_LIGHTING:
				int frequency = Integer.parseInt(strs[1]);
				int minLightLevel = Integer.parseInt(strs[2]);
				int maxLightLevel = Integer.parseInt(strs[3]);
				int[] tiles = V1Utils.fromStringInts(strs[4], SUB_SEP);
				return new SinusoidalLightingScript(tiles, frequency, minLightLevel, maxLightLevel);
			case RANDOM_LIGHTING:
				frequency = Integer.parseInt(strs[1]);
				minLightLevel = Integer.parseInt(strs[2]);
				maxLightLevel = Integer.parseInt(strs[3]);
				tiles = V1Utils.fromStringInts(strs[4], SUB_SEP);
				return new RandomLightingScript(tiles, frequency, minLightLevel, maxLightLevel);
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

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
import java.awt.Color;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.animation.ColourMagicPortraitAnimation;
import mclachlan.maze.ui.diygui.animation.ProjectileAnimation;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Animation
{
	static final String SEP = ":";
	public static Map<Class, Integer> types;

	public static final int CUSTOM = 0;
	public static final int PROJECTILE = 1;
	public static final int COLOUR_PORTRAIT = 2;
	
	public static final int MAX = 3;
	
	static V1List<String> animationImages = new V1List<String>(";")
	{
		public String typeToString(String s)
		{
			return s;
		}

		public String typeFromString(String s)
		{
			return s;
		}
	};

	/*-------------------------------------------------------------------------*/
	static
	{
		types = new HashMap<>();

		types.put(ProjectileAnimation.class, PROJECTILE);
		types.put(ColourMagicPortraitAnimation.class, COLOUR_PORTRAIT);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(Animation a)
	{
		if (a == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;

		if (types.containsKey(a.getClass()))
		{
			type = types.get(a.getClass());
		}
		else
		{
			type = CUSTOM;
		}
		s.append(type);
		s.append(SEP);

		if (type == CUSTOM)
		{
			s.append(a.getClass().getName());
		}
		else if (type == PROJECTILE)
		{
			ProjectileAnimation pa = (ProjectileAnimation)a;
			s.append(pa.getFrameDelay());
			s.append(SEP);
			s.append(animationImages.toString(pa.getProjectileImages()));
		}
		else if (type == COLOUR_PORTRAIT)
		{
			ColourMagicPortraitAnimation cmpa = (ColourMagicPortraitAnimation)a;
			Color colour = cmpa.getColour();
			s.append(V1Colour.toString(colour));
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Animation fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP);
		int type = Integer.parseInt(strs[0]);

		if (type == CUSTOM)
		{
			try
			{
				Class clazz = Class.forName(strs[1]);
				return (Animation)clazz.newInstance();
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}
		else if (type == PROJECTILE)
		{
			int frameDelay = Integer.parseInt(strs[1]);
			List<String> projectileImages = animationImages.fromString(strs[2]);
			return new ProjectileAnimation(projectileImages, frameDelay);
		}
		else if (type == COLOUR_PORTRAIT)
		{
			Color colour = V1Colour.fromString(strs[1]);
			return new ColourMagicPortraitAnimation(colour);
		}
		else
		{
			throw new MazeException("Invalid type: "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
		Animation test = new ColourMagicPortraitAnimation(Color.RED);
		String s = toString(test);
		System.out.println("s = [" + s + "]");

		test = fromString(s);
		System.out.println("test = [" + test + "]");
	}
}

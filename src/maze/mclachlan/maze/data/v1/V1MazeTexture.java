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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1MazeTexture
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, MazeTexture> load(BufferedReader reader)
	{
		try
		{
			Map <String, MazeTexture> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				MazeTexture g = fromProperties(p);
				result.put(g.getName(), g);
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, MazeTexture> map) throws Exception
	{
		for (String name : map.keySet())
		{
			MazeTexture g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(MazeTexture obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != MazeTexture.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("imageResources=");
			b.append(V1Utils.stringList.toString(obj.getImageResources()));
			b.append(V1Utils.NEWLINE);

			b.append("animationDelay=");
			b.append(obj.getAnimationDelay());
			b.append(V1Utils.NEWLINE);

			b.append("imageWidth=");
			b.append(obj.getImageWidth());
			b.append(V1Utils.NEWLINE);

			b.append("imageHeight=");
			b.append(obj.getImageHeight());
			b.append(V1Utils.NEWLINE);

			b.append("scrollBehaviour=");
			b.append(obj.getScrollBehaviour()==null ? "" : obj.getScrollBehaviour());
			b.append(V1Utils.NEWLINE);

			b.append("scrollSpeed=");
			b.append(obj.getScrollSpeed());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static MazeTexture fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom MazeTexture impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (MazeTexture)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			List<String> imageResources = V1Utils.stringList.fromString(p.getProperty("imageResources"));
			int animationDelay = Integer.parseInt(p.getProperty("animationDelay"));
			int imageWidth = Integer.parseInt(p.getProperty("imageWidth"));
			int imageHeight = Integer.parseInt(p.getProperty("imageHeight"));
			String scrollB = p.getProperty("scrollBehaviour");
			Texture.ScrollBehaviour scrollBehaviour = scrollB.length() == 0
				? null : Texture.ScrollBehaviour.valueOf(scrollB);
			int scrollSpeed = Integer.parseInt(p.getProperty("scrollSpeed"));
			return new MazeTexture(name, imageResources, imageWidth, imageHeight, animationDelay, scrollBehaviour, scrollSpeed);
		}
	}
}

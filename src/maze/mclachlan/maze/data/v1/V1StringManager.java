

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import mclachlan.maze.data.StringManager;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1StringManager implements StringManager
{
	private final File baseDir;
	private final Map<String, Properties> strings = new HashMap<>();
	private final Object mutex = new Object();

	/*-------------------------------------------------------------------------*/
	public V1StringManager(String path)
	{
		this.baseDir = getBaseDir(path);
	}

	/*-------------------------------------------------------------------------*/
	public String getString(String namespace, String key)
	{
		synchronized (mutex)
		{
			// special case for reserved keys - always map to themselves
			// this is a mechanism for deprecating keys
			if (key != null && key.contains("reserved"))
			{
				return key;
			}

			// "strings:"
			if (namespace == null)
			{
				namespace = "strings";
			}
			
			Properties p = strings.get(namespace);

			if (p == null)
			{
				p = loadProperties(namespace);
				strings.put(namespace, p);
			}

			return p.getProperty(key);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Properties loadProperties(String namespace)
	{
		try
		{
			File file = new File(baseDir, namespace+".txt");
			if (file.exists())
			{
				Properties result = new Properties();
				FileInputStream fis = new FileInputStream(file);
				result.load(fis);
				fis.close();
				return result;
			}
			else
			{
				throw new MazeException("not found ["+namespace+"] ["+file+"]");
			}
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	static void storeProperties(String path, String namespace, Properties p)
	{
		try
		{
			File dir = getBaseDir(path);
			File file = new File(dir, namespace+".txt");
			FileOutputStream fis = new FileOutputStream(file);
			p.store(fis, null);
			fis.close();
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	private static File getBaseDir(String path)
	{
		return new File(path, "strings");
	}
}

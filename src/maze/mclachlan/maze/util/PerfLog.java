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

package mclachlan.maze.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import mclachlan.maze.game.Log;

/**
 * Not thread safe
 */
public class PerfLog extends Log
{
	private String currentDir;
	private ThreadLocal<Map<String, Long>> tags;

	/*-------------------------------------------------------------------------*/
	public static final int LOUD = 1;
	public static final int MEDIUM = 5;
	public static final int DEBUG = 10;

	/*-------------------------------------------------------------------------*/
	public PerfLog()
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk.mm.ss");
		String dateTime = df.format(new Date());

		currentDir = "log/"+dateTime;

		tags = new ThreadLocal<Map<String, Long>>();
	}

	/*-------------------------------------------------------------------------*/
	public void enter(String tag)
	{
		Map<String, Long> map = tags.get();
		if (map == null)
		{
			map = new HashMap<String, Long>();
			tags.set(map);
		}
		map.put(tag, System.nanoTime());
	}

	/*-------------------------------------------------------------------------*/
	public void exit(String tag)
	{
		Map<String, Long> map = tags.get();

		long now = System.nanoTime();
		Long start = map.remove(tag);

		if (start != null)
		{
			long diffMs = (now - start) / 1000000;
			log(tag + ": " + diffMs + "ms");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String logPath()
		{
			return currentDir+"/perf_log.txt";
		}

	/*-------------------------------------------------------------------------*/
	@Override
	protected String getLoggerName()
	{
		return "maze.perflogger";
	}
}

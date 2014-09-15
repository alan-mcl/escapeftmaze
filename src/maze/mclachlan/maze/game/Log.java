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

package mclachlan.maze.game;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Log
{
	String currentDir;
	Logger logger;
	int level = LOUD;

	/*-------------------------------------------------------------------------*/
	public static final int LOUD = 1;
	public static final int MEDIUM = 5;
	public static final int DEBUG = 10;

	/*-------------------------------------------------------------------------*/
	public Log()
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk.mm.ss");
		String dateTime = df.format(new Date());

		currentDir = "log/"+dateTime;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getLogPath()
	{
		return new File(logPath()).getAbsolutePath();
	}

	/*-------------------------------------------------------------------------*/
	private String logPath()
	{
		return currentDir+"/log.txt";
	}

	/*-------------------------------------------------------------------------*/
	public void setLevel(int level)
	{
		this.level = level;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Logs the given message at the DEBUG level.
	 */
	public synchronized void log(String msg)
	{
		log(MEDIUM, msg);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Logs the given exception at the LOUD level.
	 */
	public synchronized void log(Throwable x)
	{
		log(LOUD, x);
	}

	/*-------------------------------------------------------------------------*/
	public synchronized void log(int level, String msg)
	{
		lazyInit();
		if (level <= this.level)
		{
			logger.log(Level.ALL, msg);
		}
	}

	/*-------------------------------------------------------------------------*/
	public synchronized void log(int level, Throwable x)
	{
		lazyInit();
		if (level <= this.level)
		{
			logger.log(Level.ALL, "exception", x);
		}
	}

	/*-------------------------------------------------------------------------*/
	private synchronized void lazyInit()
	{
		try
		{
			if (this.logger == null)
			{
				logger = Logger.getLogger("maze.logger");
				new File(currentDir).mkdirs();
	
				FileHandler handler = new FileHandler(getLogPath());
				logger.addHandler(handler);
				//need this to stop writing to System.out:
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.ALL);
			
				handler.setFormatter(new SimplerFormatter());
			}
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SimplerFormatter extends Formatter
	{
		static DateFormat df = new SimpleDateFormat("kk:mm.ss.SSS");

		public String format(LogRecord record)
		{
			StringBuilder s = new StringBuilder();

			String time = df.format(new Date(record.getMillis()));

			s.append(time);
			s.append(" : ");
			s.append(record.getMessage());
			s.append(System.getProperty("line.separator"));

			if (record.getThrown() != null)
			{
				Throwable x = record.getThrown();

				StringWriter trace = new StringWriter();
				x.printStackTrace(new PrintWriter(trace));

				s.append(trace.getBuffer());
			}
			
			return s.toString();
		}
	}
}

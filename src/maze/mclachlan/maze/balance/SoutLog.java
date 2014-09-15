/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.balance;

import java.util.logging.*;
import mclachlan.maze.game.Log;

/**
 *
 */
public class SoutLog extends Log
{
	private Logger logger;

	public SoutLog()
	{
		logger = Logger.getLogger("maze.logger");
	
		Handler handler = new ConsoleHandler();
		
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		handler.setFormatter(new SimplerFormatter());
	}

	@Override
	public void log(int level, String msg)
	{
		this.logger.log(Level.ALL, msg);
	}

	@Override
	public void log(int level, Throwable x)
	{
		this.logger.log(Level.ALL, "exception", x);
	}
}

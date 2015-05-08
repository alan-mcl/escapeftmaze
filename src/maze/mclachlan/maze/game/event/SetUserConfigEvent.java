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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class SetUserConfigEvent extends MazeEvent
{
	private transient String var, value;

	/*-------------------------------------------------------------------------*/
	public SetUserConfigEvent(String var, String value)
	{
		this.var = var;
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getUserConfig().setProperty(var, value);
		Maze.getInstance().saveUserConfig();

		Maze.log(Log.DEBUG, "setting user config ["+var+"]=["+value+"]");

		return null;
	}

	/*-------------------------------------------------------------------------*/

	public String getVar()
	{
		return var;
	}

	public String getValue()
	{
		return value;
	}
}

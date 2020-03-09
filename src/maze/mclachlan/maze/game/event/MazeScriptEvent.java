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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.data.Database;
import java.util.*;

/**
 * A Maze event that composes a whole script, and when executed simply returns 
 * those events.
 */
public class MazeScriptEvent extends MazeEvent
{
	String script;
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @param script
	 * 	the script to execute
	 */
	public MazeScriptEvent(String script)
	{
		this.script = script;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		MazeScript script = Database.getInstance().getMazeScript(this.script);
		return script.getEvents();
	}
	
	/*-------------------------------------------------------------------------*/
	public String getScript()
	{
		return script;
	}

	/*-------------------------------------------------------------------------*/
	public void setScript(String script)
	{
		this.script = script;
	}
}

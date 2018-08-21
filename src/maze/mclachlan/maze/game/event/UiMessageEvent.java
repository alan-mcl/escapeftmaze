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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class UiMessageEvent extends MazeEvent
{
	private String msg;

	public UiMessageEvent(String msg)
	{
		this.msg = msg;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getUi().addMessage(msg);
		return null;
	}

	@Override
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}
}

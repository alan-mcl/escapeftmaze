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
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.diygui.MessageDestination;

/**
 *
 */
public class ActorsTurnToAct extends MazeEvent
{
	private ActorEncounter actorEncounter;
	private Maze maze;
	private MessageDestination msg;

	/*-------------------------------------------------------------------------*/
	public ActorsTurnToAct(ActorEncounter actorEncounter, Maze maze,
		MessageDestination msg)
	{
		this.actorEncounter = actorEncounter;
		this.maze = maze;
		this.msg = msg;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		return actorEncounter.actorsTurnToAct(maze, msg);
	}
}

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
import mclachlan.maze.stat.SpeechUtil;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class CheckCombatStatusEvent extends MazeEvent
{
	private final Maze maze;
	private final Combat combat;

	/*-------------------------------------------------------------------------*/
	public CheckCombatStatusEvent(Maze maze, Combat combat)
	{
		this.maze = maze;
		this.combat = combat;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		if (Maze.getInstance().getCurrentCombat() == null)
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
		}
		else if (maze.getParty().numAlive()>0 && combat.getLiveFoes()==0)
		{
			if (!maze.alreadyQueued(EndCombatEvent.class))
			{
				// todo: something wonky here, shouldn't we be returning these instead of appending directly to the queue
				maze.appendEvents(
					new MazeEvent()
					{
						@Override
						public List<MazeEvent> resolve()
						{
							// speech bubble for the win
							SpeechUtil.getInstance().winCombatSpeech(
								combat.getAverageFoeLevel(), maze.getParty().getPartyLevel());
							return null;
						}
					},
					new EndCombatEvent(maze, combat, maze.getCurrentActorEncounter(), true));
			}
		}

		return result;
	}
}

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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class EndCombatRoundEvent extends MazeEvent
{
	private Maze maze;
	private Combat combat;

	/*-------------------------------------------------------------------------*/
	public EndCombatRoundEvent(Maze maze, Combat combat)
	{
		this.maze = maze;
		this.combat = combat;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		maze.getUi().setFoes(combat.getFoes());
		maze.reorderPartyIfPending();
		GameSys.getInstance().attemptManualIdentification(
			combat.getFoes(), maze.getParty(), combat.getRoundNr());

		Maze.getInstance().getUi().addMessage(
			StringUtil.getEventText("msg.combat.round.ends", combat.getRoundNr()));

		result.addAll(combat.endRound());

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				maze.getUi().refreshCharacterData();
				return null;
			}
		});

		return result;
	}
}

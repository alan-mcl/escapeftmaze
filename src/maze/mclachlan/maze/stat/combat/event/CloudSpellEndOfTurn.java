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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 *
 */
public class CloudSpellEndOfTurn extends MazeEvent
{
	private CloudSpell cloudSpell;
	private ActorGroup actorGroup;

	/*-------------------------------------------------------------------------*/
	public CloudSpellEndOfTurn(CloudSpell cloudSpell, ActorGroup actorGroup)
	{
		this.cloudSpell = cloudSpell;
		this.actorGroup = actorGroup;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		cloudSpell.endOfTurn();

		// Expire conditions
		if (cloudSpell.getDuration() < 0)
		{
			Maze.log("cloud spell expired");
			actorGroup.removeCloudSpell(cloudSpell);
			cloudSpell.expire();
		}
		else
		{
			Maze.log("duration "+cloudSpell.getDuration());
			Maze.log("strength "+cloudSpell.getStrength());
		}

		return null;
	}
}

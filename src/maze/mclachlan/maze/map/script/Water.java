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

package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ActorDiesEvent;

/**
 *
 */
public class Water extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public boolean shouldExecute(Maze maze, Point tile, Point previousTile,
		int facing, int playerAction)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		maze.getUi().addMessage("... WATER ...");

		for (UnifiedActor a : maze.getParty().getActors())
		{
			if (a.getModifier(Stats.Modifiers.AMPHIBIOUS) > 0)
			{
				continue;
			}

			if (a.getHitPoints().getCurrent() > 0)
			{
				a.getHitPoints().incSub(GameSys.getInstance().getSwimmingFatigueCost(a));
				if (a.getHitPoints().getSub() >= a.getHitPoints().getCurrent())
				{
					a.getHitPoints().setCurrent(0);
					result.add(new ActorDiesEvent(a, null));
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handlePlayerAction(Maze maze, Point tile, int facing,
		int playerAction)
	{
		return PREVENT_ACTION;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isReexecuteOnSameTile()
	{
		return true;
	}
}

/*
 * Copyright (c) 2013 Alan McLachlan
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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.PlayerTilesVisited;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ForgetEvent extends MazeEvent
{
	private UnifiedActor target;
	private int strength;

	/*-------------------------------------------------------------------------*/
	public ForgetEvent(UnifiedActor target, int strength)
	{
		this.target = target;
		this.strength = strength;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return target.getName()+" forgets something...";
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		PlayerTilesVisited ptv = Maze.getInstance().getPlayerTilesVisited();
		String zone = Maze.getInstance().getCurrentZone().getName();
		List<Point> tilesVisited = ptv.getTilesVisited(zone);

		for (Point p : tilesVisited)
		{
			if (Dice.d10.roll() <= strength)
			{
				ptv.removeTileVisited(zone, p);
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}
}

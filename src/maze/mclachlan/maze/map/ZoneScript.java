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

package mclachlan.maze.map;

import java.util.*;
import mclachlan.maze.data.v2.V2Serialisable;
import mclachlan.maze.game.MazeEvent;

/**
 * A class for controlling regular transitions to a zone, like day/night cycles
 * and weather.
 */
public abstract class ZoneScript implements V2Serialisable
{
	/*-------------------------------------------------------------------------*/
	/**
	 * Called when the zone is loaded.
	 *
	 * @param zone
	 * 	The zone to operate on
	 * @param turnNr
	 * 	The turn nr of the current turn
	 */
	public abstract List<MazeEvent> init(Zone zone, long turnNr);

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of every game turn.
	 *
	 * @param zone
	 * 	The zone to operate on
	 * @param turnNr
	 * 	The turn nr that has just ended
	 * @return
	 * 	Any events to be processed
	 */
	public abstract List<MazeEvent> endOfTurn(Zone zone, long turnNr);

	@Override
	public boolean equals(Object obj)
	{
		return this.getClass().equals(obj.getClass());
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ this.getClass().hashCode();
	}


}

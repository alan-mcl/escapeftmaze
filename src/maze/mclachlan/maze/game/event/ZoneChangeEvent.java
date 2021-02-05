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

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.journal.JournalManager;

/**
 *
 */
public class ZoneChangeEvent extends MazeEvent
{
	private String zone;
	private Point pos;
	private int facing;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param facing
	 * 	A constant from {@link Facing}
	 */
	public ZoneChangeEvent(String zone, Point pos, int facing)
	{
		this.facing = facing;
		this.pos = pos;
		this.zone = zone;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (Maze.getInstance().getCurrentZone() != null)
		{
			JournalManager.getInstance().zoneJournal(StringUtil.getUiLabel("j.depart.zone", zone));
			JournalManager.getInstance().logbook(StringUtil.getUiLabel("j.depart.zone", zone));
		}

		List<MazeEvent> mazeEvents = Maze.getInstance().changeZone(zone, pos, facing);

		JournalManager.getInstance().zoneJournal(StringUtil.getUiLabel("j.arrive.zone", zone));
		JournalManager.getInstance().logbook(StringUtil.getUiLabel("j.arrive.zone", zone));

		return mazeEvents;
	}

	/*-------------------------------------------------------------------------*/
	public int getFacing()
	{
		return facing;
	}

	/*-------------------------------------------------------------------------*/
	public Point getPos()
	{
		return pos;
	}

	/*-------------------------------------------------------------------------*/
	public String getZone()
	{
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	public static class Facing extends CrusaderEngine.Facing
	{
		public static final int UNCHANGED = 0;
	}
}

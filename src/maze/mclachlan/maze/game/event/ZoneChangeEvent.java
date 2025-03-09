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

	public ZoneChangeEvent()
	{
	}

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
		Maze maze = Maze.getInstance();

		if (maze.getCurrentZone() != null)
		{
			JournalManager.getInstance().zoneJournal(StringUtil.getUiLabel("j.depart.zone", zone));
			JournalManager.getInstance().logbook(StringUtil.getUiLabel("j.depart.zone", zone));
		}

		JournalManager.getInstance().zoneJournal(zone, StringUtil.getUiLabel("j.arrive.zone", zone));
		JournalManager.getInstance().logbook(StringUtil.getUiLabel("j.arrive.zone", zone));

		return maze.changeZone(zone, pos, facing);
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

	public void setZone(String zone)
	{
		this.zone = zone;
	}

	public void setPos(Point pos)
	{
		this.pos = pos;
	}

	public void setFacing(int facing)
	{
		this.facing = facing;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		ZoneChangeEvent that = (ZoneChangeEvent)o;

		if (getFacing() != that.getFacing())
		{
			return false;
		}
		if (getZone() != null ? !getZone().equals(that.getZone()) : that.getZone() != null)
		{
			return false;
		}
		return getPos() != null ? getPos().equals(that.getPos()) : that.getPos() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getZone() != null ? getZone().hashCode() : 0;
		result = 31 * result + (getPos() != null ? getPos().hashCode() : 0);
		result = 31 * result + getFacing();
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static class Facing extends CrusaderEngine.Facing
	{
		public static final int UNCHANGED = 0;
	}
}

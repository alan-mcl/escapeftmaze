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

package mclachlan.maze.stat.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.util.MazeException;

/**
 * TODO: add to DB and UI
 */
public class ChangeNpcFactionAttitudeEvent extends MazeEvent
{
	private int value;
	private int type;
	private String faction;

	public static final int INCREMENT = 0;
	public static final int SET = 1;

	/*-------------------------------------------------------------------------*/
	public ChangeNpcFactionAttitudeEvent(String faction, int value, int type)
	{
		this.faction = faction;
		this.value = value;
		this.type = type;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		NpcFaction nf = NpcManager.getInstance().getNpcFaction(this.faction);

		switch (type)
		{
			case INCREMENT: nf.incAttitude(value); break;
			case SET: nf.setAttitude(value); break;
			default: throw new MazeException("Invalid type: "+type);
		}

		return null;
	}
}

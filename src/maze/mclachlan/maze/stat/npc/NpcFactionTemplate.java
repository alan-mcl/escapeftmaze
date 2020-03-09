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

import mclachlan.maze.data.v1.DataObject;

/**
 * Represents an NPC faction, a group of NPC's and Foes that feel the same
 * way towards the party.  While the starting attitude of NPCs can be different
 * from their Faction, when one changes all are synced up and remain that way.
 */
public class NpcFactionTemplate extends DataObject
{
	/** name of this faction */
	private String name;

	/** starting attitide for this NPC faction */
	private NpcFaction.Attitude startingAttitude;

	/*-------------------------------------------------------------------------*/
	public NpcFactionTemplate(String name, NpcFaction.Attitude startingAttitude)
	{
		this.name = name;
		this.startingAttitude = startingAttitude;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public NpcFaction.Attitude getStartingAttitude()
	{
		return startingAttitude;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setStartingAttitude(NpcFaction.Attitude startingAttitude)
	{
		this.startingAttitude = startingAttitude;
	}
}

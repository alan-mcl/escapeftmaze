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

import java.util.Collection;

/**
 * Represents an NPC faction, a group of NPC's and Foes that feel the same
 * way towards the party.  While the starting attitude of NPCs can be different
 * from their Faction, when one changes all are synced up and remain that way.
 */
public class NpcFaction
{
	NpcFactionTemplate template;

	/** attitiude of this faction */
	int attitude;

	/*-------------------------------------------------------------------------*/
	/**
	 * Recreates an NPC faction from the given data
	 */
	public NpcFaction(NpcFactionTemplate template, int attitude)
	{
		this.attitude = attitude;
		this.template = template;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new NPC faction from the given template
	 */
	public NpcFaction(NpcFactionTemplate template)
	{
		this.template = template;
		this.attitude = this.template.startingAttitude;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return template.getName();
	}

	public int getAttitude()
	{
		return attitude;
	}

	public NpcFactionTemplate getTemplate()
	{
		return template;
	}

	public void setTemplate(NpcFactionTemplate template)
	{
		this.template = template;
	}

	/*-------------------------------------------------------------------------*/
	public void incAttitude(int value)
	{
		this.attitude += value;
		if (this.attitude > Npc.MAX_ATTITUDE)
		{
			this.attitude = Npc.MAX_ATTITUDE;
		}
		syncNpcs();
	}

	/*-------------------------------------------------------------------------*/
	public void setAttitude(int attitude)
	{
		this.attitude = attitude;
		syncNpcs();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sync the attitudes of all NPCs in this faction.
	 */
	private void syncNpcs()
	{
		Collection<Npc> npcs = NpcManager.getInstance().getNpcs();

		if (npcs != null)
		{
			for (Npc npc : npcs)
			{
				if (template.name.equals(npc.getFaction()))
				{
					npc.setAttitude(this.attitude);
				}
			}
		}
	}
}

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
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.GameSys;

/**
 * Represents an NPC faction, a group of NPC's and Foes that feel the same
 * way towards the party.  While the starting attitude of NPCs can be different
 * from their Faction, when one changes all are synced up and remain that way.
 */
public class NpcFaction extends DataObject
{
	private NpcFactionTemplate template;

	/** attitude of this faction */
	private Attitude attitude;

	/*-------------------------------------------------------------------------*/
	public static enum Attitude
	{
		ATTACKING(0),
		AGGRESSIVE(1),
		WARY(2),
		SCARED(3),
		NEUTRAL(4),
		FRIENDLY(5),
		ALLIED(6);

		final int sortOrder;

		Attitude(int sortOrder)
		{
			this.sortOrder = sortOrder;
		}

		public int getSortOrder()
		{
			return sortOrder;
		}
	}

	/*-------------------------------------------------------------------------*/
	public enum AttitudeChange
	{
		BETTER,
		NO_CHANGE,
		WORSE
	}

	public NpcFaction()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Recreates an NPC faction from the given data
	 */
	public NpcFaction(NpcFactionTemplate template, Attitude attitude)
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
		this.attitude = this.template.getStartingAttitude();
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return template.getName();
	}

	@Override
	public void setName(String newName)
	{

	}

	public Attitude getAttitude()
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
	public void changeAttitude(AttitudeChange change)
	{
		attitude = GameSys.getInstance().calcAttitudeChange(attitude, change);

		syncNpcs();
	}

	/*-------------------------------------------------------------------------*/
	public void setAttitude(Attitude attitude)
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
				if (template.getName().equals(npc.getFaction()))
				{
					npc.setAttitude(this.attitude);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof NpcFaction))
		{
			return false;
		}

		NpcFaction that = (NpcFaction)o;

		if (getTemplate() != null ? !getTemplate().equals(that.getTemplate()) : that.getTemplate() != null)
		{
			return false;
		}
		return getAttitude() == that.getAttitude();
	}

	@Override
	public int hashCode()
	{
		int result = getTemplate() != null ? getTemplate().hashCode() : 0;
		result = 31 * result + (getAttitude() != null ? getAttitude().hashCode() : 0);
		return result;
	}
}

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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.stat.npc.Npc;

/**
 *
 */
public class NpcActorGroup implements ActorGroup
{
	private Npc npc;
	private final ArrayList<UnifiedActor> actors;

	public NpcActorGroup(Npc npc)
	{
		this.npc = npc;
		actors = new ArrayList<UnifiedActor>();
		actors.add(npc);
	}

	@Override
	public int numAlive()
	{
		return npc.isAlive() ? 1 : 0;
	}

	@Override
	public String getDescription()
	{
		return npc.getDisplayName();
	}

	@Override
	public List<UnifiedActor> getActors()
	{
		return actors;
	}

	@Override
	public List<UnifiedActor> getActors(int engagementRange, int minRange,
		int maxRange)
	{
		return actors;
	}

	@Override
	public void addCloudSpell(CloudSpell cloudSpell)
	{
		// no op. in combat the NPC will be a foe group
	}

	@Override
	public void removeCloudSpell(CloudSpell cloudSpell)
	{
		// no op. in combat the NPC will be a foe group
	}

	@Override
	public List<CloudSpell> getCloudSpells()
	{
		return new ArrayList<CloudSpell>();
	}

	@Override
	public int numActive()
	{
		return numAlive();
	}

	@Override
	public int getAverageLevel()
	{
		return npc.getLevel();
	}

	public Npc getNpc()
	{
		return npc;
	}
}
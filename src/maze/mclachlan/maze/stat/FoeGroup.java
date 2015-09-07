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

import java.util.List;
import java.util.ArrayList;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 * Represents a group of foes.
 */
public class FoeGroup implements ActorGroup
{
	/** actors in thie foe group */
	List<UnifiedActor> foes;
	List<CloudSpell> cloudSpells = new ArrayList<CloudSpell>();

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new, empty foe group.
	 */
	public FoeGroup()
	{
		this(new ArrayList<UnifiedActor>());
	}

	/*-------------------------------------------------------------------------*/
	public FoeGroup(List<UnifiedActor> actors)
	{
		this.foes = actors;

		for (UnifiedActor f : foes)
		{
			if (f instanceof Foe)
			{
				((Foe)f).setFoeGroup(this);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public int numAlive()
	{
		int result = 0;

		for (UnifiedActor foe : foes)
		{
			if (GameSys.getInstance().isActorAlive(foe))
			{
				result++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The number of foes in this group that are active
	 */
	public int numActive()
	{
		int result = 0;

		for (UnifiedActor foe : foes)
		{
			if (GameSys.getInstance().isActorAlive(foe) &&
				!GameSys.getInstance().isActorImmobile(foe) &&
				!GameSys.getInstance().isActorBlinkedOut(foe))
			{
				result ++;
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		// all foes should be the same type, and at
		// the same state of identification
		Foe foe = (Foe)foes.get(0);
		if (numAlive() > 1)
		{
			return foe.getDisplayNamePlural();
		}
		else
		{
			return foe.getDisplayName();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<UnifiedActor> getActors()
	{
		return foes;
	}

	/*-------------------------------------------------------------------------*/
	public List<UnifiedActor> getActors(int engagementRange, int minRange, int maxRange)
	{
		// todo: foe groups are all at the same range.
		return foes;
	}

	/*-------------------------------------------------------------------------*/
	public void add(Foe foe)
	{
		this.foes.add(foe);
		foe.setFoeGroup(this);
	}

	/*-------------------------------------------------------------------------*/
	public List<Foe> getFoes()
	{
		ArrayList<Foe> result = new ArrayList<Foe>();

		for (UnifiedActor a : foes)
		{
			result.add((Foe)a);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void remove(Foe foe)
	{
		if (!this.foes.remove(foe))
		{
			throw new MazeException("Foe ["+foe+"] not in this group!");
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addCloudSpell(CloudSpell cloudSpell)
	{
		this.cloudSpells.add(cloudSpell);
	}

	/*-------------------------------------------------------------------------*/
	public void removeCloudSpell(CloudSpell cloudSpell)
	{
		this.cloudSpells.remove(cloudSpell);
	}

	/*-------------------------------------------------------------------------*/
	public List<CloudSpell> getCloudSpells()
	{
		return this.cloudSpells;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getAverageLevel()
	{
		int result = 0;
		for (UnifiedActor a : foes)
		{
			result += a.getLevel();
		}

		return result/foes.size();
	}
}

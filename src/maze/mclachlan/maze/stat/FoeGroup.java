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
import mclachlan.maze.util.MazeException;

/**
 * Represents a group of foes.
 */
public class FoeGroup implements ActorGroup
{
	/** actors in thie foe group */
	private final List<UnifiedActor> foes;
	private final List<CloudSpell> cloudSpells = new ArrayList<CloudSpell>();

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new, empty foe group.
	 */
	public FoeGroup()
	{
		this(new ArrayList<>());
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
		ArrayList<Foe> result = new ArrayList<>();

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

	/*-------------------------------------------------------------------------*/
	@Override
	public UnifiedActor getActorWithBestModifier(Stats.Modifier modifier)
	{
		return getActorWithBestModifier(modifier, null);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public UnifiedActor getActorWithBestModifier(Stats.Modifier modifier,
		UnifiedActor excluded)
	{
		UnifiedActor result = null;
		int cur = Integer.MIN_VALUE;

		List<UnifiedActor> actors = new ArrayList<UnifiedActor>(getActors());
		// shuffle to randomise ties
		Collections.shuffle(actors);

		for (UnifiedActor a : actors)
		{
			if (cur < a.getModifier(modifier) && a != excluded)
			{
				cur = a.getModifier(modifier);
				result = a;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<UnifiedActor> getActorsWithModifier(Stats.Modifier modifier)
	{
		List<UnifiedActor> result = new ArrayList<UnifiedActor>();
		List<UnifiedActor> actors = new ArrayList<UnifiedActor>(getActors());

		for (UnifiedActor a : actors)
		{
			if (a.getModifier(modifier) > 0)
			{
				result.add(a);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getBestModifier(Stats.Modifier modifier)
	{
		return getBestModifier(modifier, null);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getBestModifier(Stats.Modifier modifier, UnifiedActor excluded)
	{
		UnifiedActor actor = getActorWithBestModifier(modifier, excluded);

		if (actor != null)
		{
			return actor.getModifier(modifier);
		}
		else
		{
			return 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("FoeGroup{");
		sb.append("foes=").append(foes);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public String getDisplayName()
	{
		return "FoeGroup";
	}

	@Override
	public int getModifier(Stats.Modifier modifier)
	{
		return 0;
	}

	public void addAll(List<Foe> foes)
	{
		foes.forEach(this::add);
	}
}

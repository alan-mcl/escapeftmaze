/*
 * Copyright (c) 2014 Alan McLachlan
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

import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.combat.Combat;

/**
 * Action option represents something that the actor can select to do
 */
public abstract class ActorActionOption
{
	private String name;
	private String displayName;
	private UnifiedActor actor;

	/*-------------------------------------------------------------------------*/
	public static final ActorActionOption INTEND_NOTHING =
		new ActorActionOption("aao.intend.nothing", null)
		{
			@Override
			public ActorActionIntention getIntention()
			{
				return ActorActionIntention.INTEND_NOTHING;
			}
		};

	/*-------------------------------------------------------------------------*/
	public ActorActionOption(String name, String displayName)
	{
		this.name = name;
		this.displayName = displayName;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getActor()
	{
		return actor;
	}

	/*-------------------------------------------------------------------------*/
	public void setActor(UnifiedActor actor)
	{
		this.actor = actor;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		return displayName;
	}

	/*-------------------------------------------------------------------------*/
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ActorActionOption))
		{
			return false;
		}

		ActorActionOption that = (ActorActionOption)o;

		if (!name.equals(that.name))
		{
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Selects this option. Subclasses must prepare an intention and call the
	 * callback.
	 * @param actor
	 * @param combat
	 * @param callback
	 */
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		callback.selected(getIntention());
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the prepared intention of the actor
	 */
	public abstract ActorActionIntention getIntention();

	/*-------------------------------------------------------------------------*/
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		if (displayName != null)
		{
			return StringUtil.getUiLabel(displayName);
		}
		else
		{
			return name;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static interface ActionOptionCallback
	{
		void selected(ActorActionIntention intention);
	}
}

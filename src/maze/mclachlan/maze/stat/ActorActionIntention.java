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

/**
 *
 */
public class ActorActionIntention
{
	public static final ActorActionIntention INTEND_NOTHING = new ActorActionIntention("intend nothing");

	private UnifiedActor actor;
	private StatModifier statModifier;

	private final String name;

	public ActorActionIntention(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public void setStatModifier(StatModifier statModifier)
	{
		this.statModifier = statModifier;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getStatModifier()
	{
		return statModifier;
	}

	public UnifiedActor getActor()
	{
		return actor;
	}

	public void setActor(UnifiedActor actor)
	{
		this.actor = actor;
	}

	@Override
	public String toString()
	{
		return name;
	}
}

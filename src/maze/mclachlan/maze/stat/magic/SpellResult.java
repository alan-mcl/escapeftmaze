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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.TypeDescriptor;
import mclachlan.maze.stat.UnifiedActor;

/**
 * The consequences of a spell.
 */
public abstract class SpellResult
{
	/**
	 * The foe types that this spell result affects, null if all
	 */
	private TypeDescriptor foeType;

	/*-------------------------------------------------------------------------*/
	public TypeDescriptor getFoeType()
	{
		return foeType;
	}

	/*-------------------------------------------------------------------------*/
	public void setFoeType(TypeDescriptor foeType)
	{
		this.foeType = foeType;
	}

	/*-------------------------------------------------------------------------*/
	public boolean appliesTo(UnifiedActor actor)
	{
		return this.foeType == null || actor.getTypes().contains(this.foeType);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Actor.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */ 
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Item.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */
	public List<MazeEvent> apply(
		UnifiedActor source,
		Item item,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Tile.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */
	public List<MazeEvent> apply(
		UnifiedActor source,
		Tile tile,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	protected List<MazeEvent> getList(MazeEvent... events)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.addAll(Arrays.asList(events));
		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("SpellResult [").append(this.getClass()).append("]");
		sb.append("{foeType='").append(foeType).append('\'');
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		return true;
	}
}

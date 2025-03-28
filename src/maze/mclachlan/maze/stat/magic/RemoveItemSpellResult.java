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
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A spell result that removes an item from the target
 */
public class RemoveItemSpellResult extends SpellResult
{
	private String itemName;

	public RemoveItemSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/

	public RemoveItemSpellResult(String itemName)
	{
		this.itemName = itemName;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		result.add(new RemoveItemEvent(itemName, target));

		return result;
	}

	/*-------------------------------------------------------------------------*/

	public String getItemName()
	{
		return itemName;
	}

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		RemoveItemSpellResult that = (RemoveItemSpellResult)o;

		return getItemName() != null ? getItemName().equals(that.getItemName()) : that.getItemName() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getItemName() != null ? getItemName().hashCode() : 0);
		return result;
	}
}

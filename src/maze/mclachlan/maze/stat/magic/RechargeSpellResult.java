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
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A spell result that attempts to recharge an item
 */
public class RechargeSpellResult extends SpellResult
{
	private ValueList value;

	public RechargeSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public RechargeSpellResult(ValueList value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, Item item, int castingLevel, SpellEffect parent)
	{
		int v = value.compute(source, castingLevel);

		if (v >= item.getRechargeDifficulty() && item.getCharges() != null)
		{
			Maze.log(Log.DEBUG, "recharge spell result succeeds ["+v+"]");
			item.getCharges().setCurrentToMax();
		}
		else
		{
			Maze.log(Log.DEBUG, "recharge spell result fails ["+v+"] ["+
				item.getRechargeDifficulty()+"] ["+item.getCharges()+"]");
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getValue()
	{
		return value;
	}

	public void setValue(ValueList value)
	{
		this.value = value;
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

		RechargeSpellResult that = (RechargeSpellResult)o;

		return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		return result;
	}
}

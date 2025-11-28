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

package mclachlan.maze.map.script;

import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import java.util.*;

/**
 *
 */
public class GrantGoldEvent extends MazeEvent
{
	private int amount;

	public GrantGoldEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public GrantGoldEvent(int amount)
	{
		this.amount = amount;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		if (amount >= 0)
		{
			return StringUtil.getEventText("grant.gold", amount);
		}
		else
		{
			return StringUtil.getEventText("lose.gold", -amount);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getParty().incGold(amount);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

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

		GrantGoldEvent that = (GrantGoldEvent)o;

		return getAmount() == that.getAmount();
	}

	@Override
	public int hashCode()
	{
		return getAmount();
	}
}

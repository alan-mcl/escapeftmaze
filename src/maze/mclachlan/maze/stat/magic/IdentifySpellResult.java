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
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 * A spell result that attempts to identify an item
 */
public class IdentifySpellResult extends SpellResult
{
	private ValueList value;
	private boolean revealCurses;

	/*-------------------------------------------------------------------------*/
	public IdentifySpellResult(ValueList value, boolean revealCurses)
	{
		this.value = value;
		this.revealCurses = revealCurses;
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

		if (v >= item.getIdentificationDifficulty())
		{
			item.setIdentificationState(Item.IdentificationState.IDENTIFIED);
			if (revealCurses)
			{
				item.setCursedState(Item.CursedState.DISCOVERED);
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public boolean revealCurses()
	{
		return revealCurses;
	}

	public ValueList getValue()
	{
		return value;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("IdentifySpellResult");
		sb.append("{value=").append(value);
		sb.append(", revealCurses=").append(revealCurses);
		sb.append('}');
		return sb.toString();
	}
}

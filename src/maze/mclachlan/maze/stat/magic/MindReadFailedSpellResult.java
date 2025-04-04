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
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.NpcMindreadFailedEvent;

/**
 * Cast on an NPC to divine their thoughts
 */
public class MindReadFailedSpellResult extends SpellResult
{
	private ValueList value;

	public MindReadFailedSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public MindReadFailedSpellResult(ValueList value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		if (target instanceof Foe)
		{
			Foe npc = (Foe)target;
			int strength = this.value.compute(source, castingLevel);

			return getList(
				new NpcMindreadFailedEvent(npc, strength));
		}

		// todo: would be amusing to be able to cast mindread on foes in combat
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

		MindReadFailedSpellResult that = (MindReadFailedSpellResult)o;

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

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
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DrainEvent;

/**
 *
 */
public class DrainSpellResult extends SpellResult
{
	private ValueList drain;
	private Stats.Modifier modifier;

	public DrainSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public DrainSpellResult(ValueList drain, Stats.Modifier modifier)
	{
		this.drain = drain;
		this.modifier = modifier;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		int damage = this.drain.compute(source, castingLevel);
		
		return getList(
			new DrainEvent(
				target,
				source,
				damage,
				parent.getType(),
				modifier));
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getDrain()
	{
		return drain;
	}
	
	/*-------------------------------------------------------------------------*/
	public Stats.Modifier getModifier()
	{
		return modifier;
	}

	public void setDrain(ValueList drain)
	{
		this.drain = drain;
	}

	public void setModifier(Stats.Modifier modifier)
	{
		this.modifier = modifier;
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

		DrainSpellResult that = (DrainSpellResult)o;

		if (getDrain() != null ? !getDrain().equals(that.getDrain()) : that.getDrain() != null)
		{
			return false;
		}
		return getModifier() == that.getModifier();
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getDrain() != null ? getDrain().hashCode() : 0);
		result = 31 * result + (getModifier() != null ? getModifier().hashCode() : 0);
		return result;
	}
}

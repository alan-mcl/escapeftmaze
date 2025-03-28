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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageMagicEvent;
import mclachlan.maze.stat.combat.event.ForgetEvent;

/**
 *
 */
public class ForgetSpellResult extends SpellResult
{
	/** The strength of the forget */
	private ValueList strength;

	public ForgetSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ForgetSpellResult(ValueList strength)
	{
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		int str = strength.compute(source, castingLevel);
		int current = target.getMagicPoints().getCurrent();

		// PCs lose mapped tiles
		if (target instanceof PlayerCharacter)
		{
			result.add(new ForgetEvent(target, str));
		}

		// 10% of current magic points, per strength
		double perc = str/10.0;
		int dam = (int)(current*perc);

		result.add(new DamageMagicEvent(target, source, dam,
			MagicSys.SpellEffectType.MENTAL, MagicSys.SpellEffectSubType.PSYCHIC));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getStrength()
	{
		return strength;
	}

	/*-------------------------------------------------------------------------*/
	public void setStrength(ValueList strength)
	{
		this.strength = strength;
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

		ForgetSpellResult that = (ForgetSpellResult)o;

		return getStrength() != null ? getStrength().equals(that.getStrength()) : that.getStrength() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getStrength() != null ? getStrength().hashCode() : 0);
		return result;
	}
}

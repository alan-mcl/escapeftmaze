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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionIdentificationEvent;
import mclachlan.maze.stat.condition.Condition;

/**
 * A spell for identifying conditions and their strengths
 */
public class ConditionIdentificationSpellResult extends SpellResult
{
	private ValueList strength;
	private boolean canIdentifyConditionStrength;

	/*-------------------------------------------------------------------------*/
	public ConditionIdentificationSpellResult(ValueList strength,
		boolean canIdentifyConditionStrength)
	{
		this.strength = strength;
		this.canIdentifyConditionStrength = canIdentifyConditionStrength;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<Condition> list = target.getConditions();
		if (list != null)
		{
			List<Condition> conditions = new ArrayList<Condition>(list);
			for (Condition c : conditions)
			{
				int str = strength.compute(source, castingLevel);
				result.add(
					new ConditionIdentificationEvent(
						target, c, str, canIdentifyConditionStrength));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	public ValueList getStrength()
	{
		return strength;
	}

	public void setStrength(ValueList strength)
	{
		this.strength = strength;
	}

	public boolean isCanIdentifyConditionStrength()
	{
		return canIdentifyConditionStrength;
	}

	public void setCanIdentifyConditionStrength(
		boolean canIdentifyConditionStrength)
	{
		this.canIdentifyConditionStrength = canIdentifyConditionStrength;
	}
}

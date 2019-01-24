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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.ConditionRemovalEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;

/**
 * Transfers a condition
 */
public class ConditionTransferSpellResult extends SpellResult
{
	private List<ConditionEffect> effects;

	/**
	 * True if this delivers the condition from the caster to the victim. False
	 * if this steals the condition from the victim to the caster.
	 */
	private boolean deliver;

	/*-------------------------------------------------------------------------*/
	public ConditionTransferSpellResult(List<ConditionEffect> effects,
		boolean deliver)
	{
		this.deliver = deliver;
		this.effects = effects;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		return transferConditions(target, source, castingLevel);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, Tile tile, int castingLevel, SpellEffect parent)
	{
		return transferConditions(tile, source, castingLevel);
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> transferConditions(
		ConditionBearer target,
		UnifiedActor source,
		int castingLevel)
	{
		System.out.println("ConditionTransferSpellResult.transferConditions");

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<Condition> list = source.getConditions();
		System.out.println("list = [" + list + "]");

		if (list != null)
		{
			List<Condition> conditions = new ArrayList<Condition>(list);
			for (Condition c : conditions)
			{
				if (deliver)
				{
					// can only deliver identified conditions
					if (c.isIdentified() && this.effects.contains(c.getEffect()))
					{
						result.add(new ConditionRemovalEvent(source, c));
						result.add(new ConditionEvent(target, c));
					}
				}
				else
				{
					if (c.isIdentified() && this.effects.contains(c.getEffect()))
					{
						result.add(new ConditionRemovalEvent(target, c));
						result.add(new ConditionEvent(source, c));
					}
				}
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/

	public List<ConditionEffect> getEffects()
	{
		return effects;
	}

	public void setEffects(List<ConditionEffect> effects)
	{
		this.effects = effects;
	}

	public boolean isDeliver()
	{
		return deliver;
	}

	public void setDeliver(boolean deliver)
	{
		this.deliver = deliver;
	}
}

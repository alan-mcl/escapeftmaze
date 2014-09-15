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

package mclachlan.maze.stat.condition;

import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.BlinkAction;
import mclachlan.maze.stat.combat.CombatAction;

/**
 *
 */
public class BlinkEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public boolean isBlinkedOut(UnifiedActor actor, Condition condition)
	{
		return true;
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isPresent(UnifiedActor actor, Condition condition)
	{
		return false;
	}
	
	/*-------------------------------------------------------------------------*/

	public boolean canBeAttacked(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		// can't do anything except blink
		if (action instanceof BlinkAction)
		{
			return action;
		}
		else
		{
			return CombatAction.DO_NOTHING;
		}
	}
}

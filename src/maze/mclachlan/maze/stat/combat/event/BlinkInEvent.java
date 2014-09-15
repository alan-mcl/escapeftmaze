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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.BlinkEffect;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionManager;

/**
 *
 */
public class BlinkInEvent extends MazeEvent
{
	UnifiedActor actor;

	/*-------------------------------------------------------------------------*/
	public BlinkInEvent(UnifiedActor actor)
	{
		this.actor = actor;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<Condition> toRemove = new ArrayList<Condition>();
		for (Condition c : actor.getConditions())
		{
			if (c.getEffect() instanceof BlinkEffect)
			{
				toRemove.add(c);
			}
		}
		for (Condition c : toRemove)
		{
			ConditionManager.getInstance().removeCondition(actor, c);
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getActor()
	{
		return actor;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getActor().getDisplayName()+" blinks in";
	}
}

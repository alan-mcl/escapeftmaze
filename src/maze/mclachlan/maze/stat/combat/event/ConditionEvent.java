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
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;

/**
 *
 */
public class ConditionEvent extends MazeEvent
{
	private ConditionBearer target;
	private Condition condition;

	/*-------------------------------------------------------------------------*/
	public ConditionEvent(
		ConditionBearer target,
		Condition condition)
	{
		this.condition = condition;
		this.target = target;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = target.addCondition(condition);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Condition getCondition()
	{
		return condition;
	}

	public ConditionBearer getTarget()
	{
		return target;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getTarget().getDisplayName()+" "+getCondition().getAdjective();
	}
}

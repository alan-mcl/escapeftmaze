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
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ParalyseEffect;
import mclachlan.maze.stat.condition.WebEffect;
import mclachlan.maze.stat.condition.impl.Berserk;

/**
 *
 */
public class BerserkEvent extends MazeEvent
{
	UnifiedActor target;

	private static String[] msgs =
		{
			"%s goes berserk!!",
			"%s is possessed by the blood madness!",
			"%s feels the warp spasm take hold!",
			"%s sees the red mist descend!",
			"%s snaps into battle frenzy!",
		};

	/*-------------------------------------------------------------------------*/
	public BerserkEvent(UnifiedActor target)
	{
		this.target = target;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = target.addCondition(new Berserk());

		// Breaking of Bonds
		if (target.getModifier(Stats.Modifier.BERSERK_POWERS) >= 1)
		{
			for (Condition c : target.getConditions())
			{
				if (c.getEffect() instanceof WebEffect ||
					c.getEffect() instanceof ParalyseEffect)
				{
					result.add(new ConditionRemovalEvent(target, c));
				}
			}
		}

		// Bewildering of Witches
		if (target.getModifier(Stats.Modifier.BERSERK_POWERS) >= 4)
		{
			// todo: get an Eye for and Eye condition
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
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
		Dice d = new Dice(1, msgs.length, -1);
		String s = msgs[d.roll()];
		Formatter f = new Formatter();
		f.format(s, getTarget().getDisplayName());
		return f.toString();
	}
}
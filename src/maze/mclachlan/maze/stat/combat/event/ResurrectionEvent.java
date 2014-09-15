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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.SpeechUtil;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ResurrectionEvent extends MazeEvent
{
	private UnifiedActor target;
	private int level;

	/*-------------------------------------------------------------------------*/
	public ResurrectionEvent(UnifiedActor target, int level)
	{
		this.target = target;
		this.level = level;
	}

	/*-------------------------------------------------------------------------*/
	public int getLevel()
	{
		return level;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getTarget()
	{
		return target;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (target.getHitPoints().getCurrent() <= 0)
		{
			target.getHitPoints().incMaximum(-1);
			target.getHitPoints().incCurrent(new Dice(level, 6, level).roll());
			if (target instanceof PlayerCharacter)
			{
				return SpeechUtil.getInstance().resurrectionSpeech((PlayerCharacter)target);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getTarget().getDisplayName()+" lives!";
	}
}
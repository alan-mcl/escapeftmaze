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

package mclachlan.maze.map.script;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class LoseExperienceEvent extends MazeEvent
{
	private int amount;
	private PlayerCharacter pc;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param amount
	 * 	the amount of XP
	 * @param pc
	 * 	the character to lose them, null for all live members of the party
	 */
	public LoseExperienceEvent(int amount, PlayerCharacter pc)
	{
		this.amount = amount;
		this.pc = pc;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		if (pc == null)
				{
					return StringUtil.getEventText("msg.lose.experience.all", amount);
				}
				else
				{
					return StringUtil.getEventText("msg.lose.experience.single", pc.getDisplayName(), amount);
				}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		// remove the experience
		if (pc != null)
		{
			pc.incExperience(-amount);
		}
		else
		{
			// otherwise, remove from the party
			Maze.getInstance().getParty().grantExperience(-amount);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	public int getAmount()
	{
		return amount;
	}
}

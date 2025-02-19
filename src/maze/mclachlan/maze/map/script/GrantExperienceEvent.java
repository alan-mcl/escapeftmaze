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
public class GrantExperienceEvent extends MazeEvent
{
	private int amount;
	private PlayerCharacter pc;

	public GrantExperienceEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param amount
	 * 	the amount of XP
	 * @param pc
	 * 	the character to get them, null for all live members of the party
	 */
	public GrantExperienceEvent(int amount, PlayerCharacter pc)
	{
		this.amount = amount;
		this.pc = pc;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		if (pc == null)
		{
			return StringUtil.getEventText("msg.grant.experience.all", amount);
		}
		else
		{
			return StringUtil.getEventText("msg.grant.experience.single", pc.getDisplayName(), amount);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		// check which characters are already due for a level up event
		List<PlayerCharacter> playerCharacters = Maze.getInstance().getParty().getPlayerCharacters();
		boolean[] preXpLvlUpPending = new boolean[playerCharacters.size()];

		for (int i = 0; i < playerCharacters.size(); i++)
		{
			preXpLvlUpPending[i] = playerCharacters.get(i).isLevelUpPending();
		}

		// grant the experience
		if (pc != null)
		{
			pc.incExperience(amount);
		}
		else
		{
			// otherwise, grant to the party
			Maze.getInstance().getParty().grantExperience(amount);
		}

		// check if any characters have suddenly levelled up
		for (int i = 0; i < playerCharacters.size(); i++)
		{
			if (!preXpLvlUpPending[i] && playerCharacters.get(i).isLevelUpPending())
			{
				// this character is now able to level up
				result.add(new FlavourTextEvent(
					playerCharacters.get(i).getName()+" gains a level!",
					Maze.getInstance().getUserConfig().getCombatDelay(), false));
			}
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

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public PlayerCharacter getPc()
	{
		return pc;
	}

	public void setPc(PlayerCharacter pc)
	{
		this.pc = pc;
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

		GrantExperienceEvent that = (GrantExperienceEvent)o;

		if (getAmount() != that.getAmount())
		{
			return false;
		}
		return getPc() != null ? getPc().equals(that.getPc()) : that.getPc() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getAmount();
		result = 31 * result + (getPc() != null ? getPc().hashCode() : 0);
		return result;
	}
}

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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.CharacterSelection;
import mclachlan.maze.stat.PlayerCharacter;

/**
 * Forces a party split: selected characters remain in the field party; everyone
 * else is moved into a camp at the current tile.
 */
public class ForcePartySplitEvent extends MazeEvent
{
	private CharacterSelection characterSelection;
	private transient List<PlayerCharacter> retainInParty;

	public ForcePartySplitEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ForcePartySplitEvent(List<PlayerCharacter> retainInParty)
	{
		this.retainInParty = retainInParty;
	}

	/*-------------------------------------------------------------------------*/
	public ForcePartySplitEvent(CharacterSelection characterSelection)
	{
		this.characterSelection = characterSelection;
	}

	public CharacterSelection getCharacterSelection()
	{
		return characterSelection;
	}

	public void setCharacterSelection(CharacterSelection characterSelection)
	{
		this.characterSelection = characterSelection;
	}

	@Override
	public int getDelay()
	{
		return Delay.NONE;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze maze = Maze.getInstance();
		if (maze.getParty() == null)
		{
			return null;
		}

		if (retainInParty != null)
		{
			applySplit(maze, retainInParty);
			return null;
		}

		if (characterSelection == null)
		{
			return null;
		}

		return characterSelection.select(
			maze.getParty().getPlayerCharacters(),
			selected -> applySplit(maze, selected));
	}

	/*-------------------------------------------------------------------------*/
	static void applySplit(Maze maze, List<PlayerCharacter> retainInParty)
	{
		if (retainInParty == null || retainInParty.isEmpty())
		{
			return;
		}

		Set<String> retainNames = new HashSet<>();
		for (PlayerCharacter pc : retainInParty)
		{
			retainNames.add(pc.getName());
		}

		List<PlayerCharacter> leavers = new ArrayList<>();
		boolean anyRetainInParty = false;
		for (PlayerCharacter pc : maze.getParty().getPlayerCharacters())
		{
			if (retainNames.contains(pc.getName()))
			{
				anyRetainInParty = true;
			}
			else
			{
				leavers.add(pc);
			}
		}

		if (!anyRetainInParty || leavers.isEmpty())
		{
			return;
		}

		maze.forceTransferPlayerCharactersToCamp(leavers);
	}
}

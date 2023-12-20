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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class EndCombatRoundEvent extends MazeEvent
{
	private final Maze maze;
	private final Combat combat;

	/*-------------------------------------------------------------------------*/
	public EndCombatRoundEvent(Maze maze, Combat combat)
	{
		this.maze = maze;
		this.combat = combat;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		List<FoeGroup> foes = combat.getFoes();
		ListIterator<FoeGroup> foeGroupListIterator = foes.listIterator();
		while (foeGroupListIterator.hasNext())
		{
			FoeGroup fg = (FoeGroup)foeGroupListIterator.next();
			if (fg.numAlive() == 0)
			{
				foeGroupListIterator.remove();
				combat.getDeadFoeGroups().add(fg);
			}
		}

		// foe groups can advance or retreat
		FoeGroup[] strongestAndWeakestFoeGroups = combat.getStrongestAndWeakestFoeGroups();
		FoeGroup strongest = strongestAndWeakestFoeGroups[0];
		FoeGroup weakest = strongestAndWeakestFoeGroups[1];

		if (foes.size() > 1)
		{
			if (strongest != weakest && strongest != null && weakest != null)
			{
				if (foes.indexOf(strongest) > 0)
				{
					String verb = "advance";
					if (strongest.getFoes().size() == 1)
					{
						verb += "s";
					}
					maze.getUi().addMessage(strongest.getDescription() + " " + verb + "!");
					combat.advanceFoeGroup(strongest);
				}

				if (foes.size() > 2 && foes.indexOf(weakest) < foes.size()-1)
				{
					String verb = "retreat";
					if (weakest.getFoes().size() == 1)
					{
						verb += "s";
					}
					maze.getUi().addMessage(weakest.getDescription()+ " " + verb + "!");
					combat.retreatFoeGroup(weakest);
				}
			}
		}

		// todo: reorg foe sprites
		maze.getUi().rebalanceFoeSprites(combat);

		maze.reorderPartyIfPending();
		GameSys.getInstance().attemptManualIdentification(
			foes, maze.getParty(), combat.getRoundNr());

		Maze.getInstance().incTurn(false);

		Maze.getInstance().getUi().addMessage(
			StringUtil.getEventText("msg.combat.round.ends", combat.getRoundNr()));

		result.addAll(combat.endRound());

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				maze.getUi().refreshCharacterData();
				return null;
			}
		});

		return result;
	}
}

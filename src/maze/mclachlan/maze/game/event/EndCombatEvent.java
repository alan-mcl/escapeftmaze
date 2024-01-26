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
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class EndCombatEvent extends MazeEvent
{
	private Maze maze;
	private Combat combat;
	private ActorEncounter encounter;
	private boolean partyWins;

	/*-------------------------------------------------------------------------*/
	public EndCombatEvent(Maze maze, Combat combat, ActorEncounter encounter,
		boolean partyWins)
	{
		this.maze = maze;
		this.combat = combat;
		this.encounter = encounter;
		this.partyWins = partyWins;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		//
		// Clear the UI
		//
		maze.getUi().setFoes(null, false);
		maze.getUi().setAllies(null);
		maze.getUi().clearDialog();

		//
		// Party status check
		//
		maze.checkPartyStatus();
		maze.reorderPartyToCompensateForDeadCharacters();

		//
		// End the combat
		//
		combat.endCombat();
		maze.setCurrentCombat(null);
		maze.setCurrentActorEncounter(null);
		maze.setState(Maze.State.MOVEMENT);

		//
		// process party victory
		//
		if (partyWins)
		{
			//
			// Set the encounter maze var so that this encounter does not
			// happen again.
			//
			if (encounter.getMazeVar() != null)
			{
				MazeVariables.set(encounter.getMazeVar(), "true");
			}

			//
			// Generate any loot
			//
			List<Item> loot = combat.getLoot();
			int totalGold = TileScript.extractGold(loot);
			int totalExperience = combat.getTotalExperience();
			int xp = totalExperience / maze.getParty().numAlive();

			int extraPercent = 0;
			// calculate extra gold
			for (UnifiedActor pc : maze.getParty().getActors())
			{
				extraPercent += pc.getModifier(Stats.Modifier.EXTRA_GOLD);
			}
			totalGold += (totalGold*extraPercent/100);

			List<MazeEvent> result = new ArrayList<MazeEvent>();
			result.add(new UiMessageEvent(StringUtil.getEventText("msg.victory")));
			result.add(new GrantExperienceEvent(xp, null));
			if (totalGold > 0)
			{
				result.add(new GrantGoldEvent(totalGold));
			}

			if (loot.size() > 0)
			{
				result.add(new GrantItemsEvent(loot));
			}

			return result;
		}
		else
		{
			return null;
		}
	}
}

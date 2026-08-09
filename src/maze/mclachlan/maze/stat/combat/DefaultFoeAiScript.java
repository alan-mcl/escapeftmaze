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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.StartCombatEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 *
 */
public class DefaultFoeAiScript extends NpcScript
{
	private final ActorEncounter actorEncounter;

	/*-------------------------------------------------------------------------*/
	public DefaultFoeAiScript(ActorEncounter actorEncounter)
	{
		this.actorEncounter = actorEncounter;
		this.npc = actorEncounter.getLeader();
	}

	@Override
	public List<MazeEvent> firstGreeting()
	{
		FoeInteraction foeInteraction = npc.getFoeTemplate().getFoeInteraction();
		if (foeInteraction != null)
		{
			switch (npc.getAttitude())
			{
				case NEUTRAL -> { return FoeInteraction.eventsOrNull(foeInteraction.getNeutralGreeting()); }
				case FRIENDLY, ALLIED -> { return FoeInteraction.eventsOrNull(foeInteraction.getFriendlyGreeting()); }
			}
		}

		return null;
	}

	@Override
	public List<MazeEvent> friendlyGreeting()
	{
		return firstGreeting();
	}

	@Override
	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> attacksParty(Combat.AmbushStatus fAmbushStatus)
	{
		return startCombatWithOptionalPrologue();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> attackedByParty()
	{
		return startCombatWithOptionalPrologue();
	}

	private List<MazeEvent> startCombatWithOptionalPrologue()
	{
		Maze maze = Maze.getInstance();
		List<MazeEvent> result = new ArrayList<>();

		FoeInteraction foeInteraction = npc.getFoeTemplate().getFoeInteraction();
		if (foeInteraction != null)
		{
			List<MazeEvent> prologue = FoeInteraction.eventsOrNull(foeInteraction.getAttacksParty());
			if (prologue != null)
			{
				result.addAll(prologue);
			}
		}

		result.add(new StartCombatEvent(
			maze,
			maze.getParty(),
			actorEncounter));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		FoeInteraction foeInteraction = npc.getFoeTemplate().getFoeInteraction();
		if (foeInteraction != null
			&& foeInteraction.getGivenItemName() != null
			&& foeInteraction.getGivenItemName().equals(item.getName()))
		{
			List<MazeEvent> result = FoeInteraction.eventsOrNull(foeInteraction.getGivenItemScript());
			if (result != null)
			{
				return result;
			}
		}

		return super.givenItemByParty(owner, item);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		List<MazeEvent> result = new ArrayList<>();
		FoeInteraction foeInteraction = npc.getFoeTemplate().getFoeInteraction();
		if (foeInteraction != null)
		{
			List<MazeEvent> farewell = FoeInteraction.eventsOrNull(foeInteraction.getFriendlyFarewell());
			if (farewell != null)
			{
				result.addAll(farewell);
			}
		}
		if (actorEncounter.getPartyLeavesFriendlyScript() != null)
		{
			result.addAll(actorEncounter.getPartyLeavesFriendlyScript());
		}
		result.add(new ActorsLeaveEvent());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		List<MazeEvent> result = new ArrayList<>();
		FoeInteraction foeInteraction = npc.getFoeTemplate().getFoeInteraction();
		if (foeInteraction != null)
		{
			List<MazeEvent> farewell = FoeInteraction.eventsOrNull(foeInteraction.getNeutralFarewell());
			if (farewell != null)
			{
				result.addAll(farewell);
			}
		}
		if (actorEncounter.getPartyLeavesNeutralScript() != null)
		{
			result.addAll(actorEncounter.getPartyLeavesNeutralScript());
		}
		result.add(new ActorsLeaveEvent());
		return result;
	}
}

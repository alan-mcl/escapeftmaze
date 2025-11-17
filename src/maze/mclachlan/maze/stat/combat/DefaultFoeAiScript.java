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
import mclachlan.maze.stat.npc.FoeSpeech;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.npc.PartyLeavesEvent;

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
		FoeSpeech foeSpeech = npc.getFoeTemplate().getFoeSpeech();
		if (foeSpeech != null)
		{
			switch (npc.getAttitude())
			{
				case NEUTRAL ->
				{
					if (foeSpeech.getNeutralGreeting() != null)
					{
						return getList(new NpcSpeechEvent(foeSpeech.getNeutralGreeting(), npc));
					}
				}
				case FRIENDLY, ALLIED ->
				{
					if (foeSpeech.getFriendlyGreeting() != null)
					{
						return getList(new NpcSpeechEvent(foeSpeech.getFriendlyGreeting(), npc));
					}
				}
			}
		}

		return null;
	}

	@Override
	public List<MazeEvent> subsequentGreeting()
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
		Maze maze = Maze.getInstance();

		return getList(new StartCombatEvent(
			maze,
			maze.getParty(),
			actorEncounter));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> attackedByParty()
	{
		Maze maze = Maze.getInstance();

		return getList(new StartCombatEvent(
			maze,
			maze.getParty(),
			actorEncounter));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		List<MazeEvent> result = new ArrayList<>();
		if (actorEncounter.getPartyLeavesFriendlyScript() != null)
		{
			result.addAll(actorEncounter.getPartyLeavesFriendlyScript());
		}
		FoeSpeech foeSpeech = npc.getFoeTemplate().getFoeSpeech();
		if (foeSpeech != null && foeSpeech.getFriendlyFarewell() != null)
		{
			result.add(new NpcSpeechEvent(foeSpeech.getFriendlyFarewell(), npc));
		}
		result.add(new PartyLeavesEvent());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		List<MazeEvent> result = new ArrayList<>();
		if (actorEncounter.getPartyLeavesNeutralScript() != null)
		{
			result.addAll(actorEncounter.getPartyLeavesNeutralScript());
		}
		FoeSpeech foeSpeech = npc.getFoeTemplate().getFoeSpeech();
		if (foeSpeech != null && foeSpeech.getNeutralFarewell() != null)
		{
			result.add(new NpcSpeechEvent(foeSpeech.getNeutralFarewell(), npc));
		}
		result.add(new PartyLeavesEvent());
		return result;
	}
}

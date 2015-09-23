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

package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.event.StartCombatEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.ui.diygui.MessageDestination;

/**
 *
 */
public class ActorEncounter
{
	private List<FoeGroup> actors;
	private String mazeVar;
	private NpcFaction.Attitude encounterAttitude;
	private Combat.AmbushStatus ambushStatus;
	private final UnifiedActor leader;

	/*-------------------------------------------------------------------------*/
	public ActorEncounter(
		List<FoeGroup> actors,
		String mazeVar,
		NpcFaction.Attitude encounterAttitude,
		Combat.AmbushStatus ambushStatus)
	{
		this.encounterAttitude = encounterAttitude;
		this.actors = actors;
		this.mazeVar = mazeVar;
		this.ambushStatus = ambushStatus;
		leader = GameSys.getInstance().getLeader(this.actors);
	}

	/*-------------------------------------------------------------------------*/
	public String describe()
	{
		StringBuilder actorsDesc = new StringBuilder();
		Set<String> uniqueNames = new HashSet<String>();

		for (ActorGroup ag : actors)
		{
			uniqueNames.add(ag.getDescription());
		}

		for (String s : uniqueNames)
		{
			actorsDesc.append(s);
			actorsDesc.append(", ");
		}

		actorsDesc.deleteCharAt(actorsDesc.lastIndexOf(","));

		return actorsDesc.toString();
	}

	/*-------------------------------------------------------------------------*/
	public String describeLeader()
	{
		boolean leaderGroupPlural = isLeaderGroupPlural();

		if (leaderGroupPlural)
		{
			return leader.getDisplayNamePlural();
		}
		else
		{
			return leader.getDisplayName();
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean isLeaderGroupPlural()
	{
		ActorGroup leaderGroup = null;

		for (ActorGroup ag : actors)
		{
			if (ag.getActors().contains(leader))
			{
				leaderGroup = ag;
				break;
			}
		}

		return leaderGroup.getActors().size() > 1;
	}

	/*-------------------------------------------------------------------------*/
	public void actorsTurnToAct(Maze maze, MessageDestination msg)
	{
		switch (encounterAttitude)
		{
			case ATTACKING:
				attacks(maze, msg);
				break;
			case AGGRESSIVE:
				if (Dice.d100.roll() <= 80)
				{
					attacks(maze, msg);
				}
				else
				{
					waits(msg);
				}
				break;
			case WARY:
				int roll = Dice.d100.roll();
				if (roll <= 20)
				{
					attacks(maze, msg);
				}
				else if (roll <= 80)
				{
					waits(msg);
				}
				else
				{
					flees(maze, msg);
				}
				break;
			case SCARED:
				if (Dice.d100.roll() <= 80)
				{
					flees(maze, msg);
				}
				else
				{
					waits(msg);
				}
				break;
			case NEUTRAL:
				waits(msg);
				break;
			case FRIENDLY:
				waits(msg);
				break;
			case ALLIED:
				waits(msg);
				break;
		}

	}

	/*-------------------------------------------------------------------------*/
	private void flees(Maze maze, MessageDestination msg)
	{
		if (isLeaderGroupPlural())
		{
			msg.addMessage(StringUtil.getEventText("msg.actors.flee", describeLeader()));
		}
		else
		{
			msg.addMessage(StringUtil.getEventText("msg.actor.flees", describeLeader()));
		}
		maze.appendEvents(new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	private void waits(MessageDestination msg)
	{
		if (isLeaderGroupPlural())
		{
			msg.addMessage(StringUtil.getEventText("msg.actors.wait", describeLeader()));
		}
		else
		{
			msg.addMessage(StringUtil.getEventText("msg.actor.waits", describeLeader()));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void attacks(Maze maze, MessageDestination msg)
	{
		if (isLeaderGroupPlural())
		{
			msg.addMessage(StringUtil.getEventText("msg.actors.attack", describeLeader()));
		}
		else
		{
			msg.addMessage(StringUtil.getEventText("msg.actor.attacks", describeLeader()));
		}
		maze.appendEvents(new StartCombatEvent(
			maze, maze.getParty(), this));
	}

	/*-------------------------------------------------------------------------*/

	public List<FoeGroup> getActors()
	{
		return actors;
	}

	public List<ActorGroup> getActorGroups()
	{
		List<ActorGroup> result = new ArrayList<ActorGroup>();
		result.addAll(actors);
		return result;
	}

	public void setActors(List<FoeGroup> actors)
	{
		this.actors = actors;
	}

	public String getMazeVar()
	{
		return mazeVar;
	}

	public void setMazeVar(String mazeVar)
	{
		this.mazeVar = mazeVar;
	}

	public NpcFaction.Attitude getEncounterAttitude()
	{
		return encounterAttitude;
	}

	public void setEncounterAttitude(NpcFaction.Attitude encounterAttitude)
	{
		this.encounterAttitude = encounterAttitude;
	}

	public Combat.AmbushStatus getAmbushStatus()
	{
		return ambushStatus;
	}

	public void setAmbushStatus(Combat.AmbushStatus ambushStatus)
	{
		this.ambushStatus = ambushStatus;
	}

	public Foe getLeader()
	{
		return GameSys.getInstance().getLeader(actors);
	}

}

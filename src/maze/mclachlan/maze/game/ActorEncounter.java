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
import mclachlan.maze.util.MazeException;

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
	private final List<MazeEvent> preScript, postAppearanceScript;

	/*-------------------------------------------------------------------------*/
	public ActorEncounter(
		List<FoeGroup> actors,
		String mazeVar,
		NpcFaction.Attitude encounterAttitude,
		Combat.AmbushStatus ambushStatus,
		List<MazeEvent> preScript,
		List<MazeEvent> postAppearanceScript)
	{
		this.encounterAttitude = encounterAttitude;
		this.actors = actors;
		this.mazeVar = mazeVar;
		this.ambushStatus = ambushStatus;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
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
	public List<MazeEvent> actorsTurnToAct(Maze maze, MessageDestination msg)
	{
		switch (encounterAttitude)
		{
			case ATTACKING:
				return attacks(maze, msg);
			case AGGRESSIVE:
				if (Dice.d100.roll("actor encounter: aggro") <= 80)
				{
					return attacks(maze, msg);
				}
				else
				{
					return waits(msg);
				}
			case WARY:
				int roll = Dice.d100.roll("actor encounter: wary");
				if (roll <= 20)
				{
					return attacks(maze, msg);
				}
				else if (roll <= 80)
				{
					return waits(msg);
				}
				else
				{
					return flees(maze, msg);
				}
			case SCARED:
				if (Dice.d100.roll("actor encounter: scared") <= 80)
				{
					return flees(maze, msg);
				}
				else
				{
					return waits(msg);
				}
			case NEUTRAL:
				return waits(msg);
			case FRIENDLY:
				return waits(msg);
			case ALLIED:
				return waits(msg);
			default:
				throw new MazeException("Invalid: "+encounterAttitude);
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> flees(Maze maze, MessageDestination msg)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (isLeaderGroupPlural())
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actors.flee", describeLeader())));
		}
		else
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actor.flees", describeLeader())));
		}
		result.add(new ActorsLeaveEvent());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> waits(MessageDestination msg)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (isLeaderGroupPlural())
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actors.wait", describeLeader())));
		}
		else
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actor.waits", describeLeader())));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> attacks(Maze maze, MessageDestination msg)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (isLeaderGroupPlural())
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actors.attack", describeLeader())));
		}
		else
		{
			result.add(new MsgDestinationEvent(msg, StringUtil.getEventText("msg.actor.attacks", describeLeader())));
		}
		result.add(new StartCombatEvent(maze, maze.getParty(), this));

		return result;
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
		return (Foe)leader;
	}

	public List<MazeEvent> evade()
	{
		return null;
	}

	public List<MazeEvent> getPreScript()
	{
		return preScript;
	}

	public List<MazeEvent> getPostAppearanceScript()
	{
		return postAppearanceScript;
	}

	private static class MsgDestinationEvent extends MazeEvent
	{
		private final MessageDestination msg;
		private final String eventText;

		public MsgDestinationEvent(MessageDestination msg, String eventText)
		{
			this.msg = msg;
			this.eventText = eventText;
		}

		@Override
		public List<MazeEvent> resolve()
		{

			msg.addMessage(eventText);
			return null;
		}
	}
}

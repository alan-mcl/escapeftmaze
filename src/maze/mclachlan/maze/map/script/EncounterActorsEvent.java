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
import mclachlan.maze.game.*;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 * Begins an encounter with some actors
 */
public class EncounterActorsEvent extends MazeEvent
{
	private String mazeVariable;
	private String encounterTable;
	private NpcFaction.Attitude attitude;
	private Combat.AmbushStatus ambushStatus;
	private MazeScript preScript, postAppearanceScript,
		partyLeavesNeutralScript, partyLeavesFriendlyScript;

	// volatile
	private EncounterTable encounterTableRef;

	/*-------------------------------------------------------------------------*/
	public EncounterActorsEvent(
		String mazeVariable,
		String encounterTable,
		NpcFaction.Attitude attitude,
		Combat.AmbushStatus ambushStatus,
		MazeScript preScript,
		MazeScript postAppearanceScript,
		MazeScript partyLeavesNeutralScript,
		MazeScript partyLeavesFriendlyScript)
	{
		this.mazeVariable = mazeVariable;
		this.encounterTable = encounterTable;
		this.attitude = attitude;
		this.ambushStatus = ambushStatus;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
		this.partyLeavesNeutralScript = partyLeavesNeutralScript;
		this.partyLeavesFriendlyScript = partyLeavesFriendlyScript;
	}

	/*-------------------------------------------------------------------------*/
	public EncounterActorsEvent(
		String mazeVariable,
		EncounterTable encounterTable,
		NpcFaction.Attitude attitude,
		Combat.AmbushStatus ambushStatus,
		MazeScript preScript,
		MazeScript postAppearanceScript,
		MazeScript partyLeavesNeutralScript,
		MazeScript partyLeavesFriendlyScript)
	{
		this.mazeVariable = mazeVariable;
		this.encounterTable = encounterTable.getName();
		this.attitude = attitude;
		this.ambushStatus = ambushStatus;

		this.encounterTableRef = encounterTable;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
		this.partyLeavesNeutralScript = partyLeavesNeutralScript;
		this.partyLeavesFriendlyScript = partyLeavesFriendlyScript;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (Boolean.valueOf(Maze.getInstance().getAppConfig().get(Maze.AppConfig.NO_ENCOUNTERS)))
		{
			return null;
		}

		if (this.mazeVariable != null)
		{
			if (MazeVariables.getBoolean(this.mazeVariable))
			{
				return null;
			}
		}

		EncounterTable table = encounterTableRef;
		if (table == null)
		{
			table = Database.getInstance().getEncounterTable(encounterTable);
		}

		FoeEntry foeEntry = table.getEncounterTable().getRandomItem();
		List<FoeGroup> allFoes = foeEntry.generate();

		if (ambushStatus == null)
		{
			ambushStatus = GameSys.getInstance().determineAmbushStatus(
				Maze.getInstance().getParty(),
				attitude,
				allFoes);
		}

		List<MazeEvent> preScriptEvents = preScript == null ? new ArrayList<>() : preScript.getEvents();
		List<MazeEvent> postAppearanceScriptEvents = postAppearanceScript == null ? new ArrayList<>() : postAppearanceScript.getEvents();
		List<MazeEvent> partyLeavesNeutralScriptEvents = partyLeavesNeutralScript == null ? new ArrayList<>() : partyLeavesNeutralScript.getEvents();
		List<MazeEvent> partyLeavesFriendlyScriptEvents = partyLeavesFriendlyScript == null ? new ArrayList<>() : partyLeavesFriendlyScript.getEvents();

		return Maze.getInstance().encounterActors(
			new ActorEncounter(allFoes, mazeVariable, attitude, ambushStatus,
				preScriptEvents, postAppearanceScriptEvents,
				partyLeavesNeutralScriptEvents, partyLeavesFriendlyScriptEvents));
	}

	/*-------------------------------------------------------------------------*/
	public String getEncounterTable()
	{
		return encounterTable;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public NpcFaction.Attitude getAttitude()
	{
		return attitude;
	}

	public Combat.AmbushStatus getAmbushStatus()
	{
		return ambushStatus;
	}

	public MazeScript getPreScript()
	{
		return preScript;
	}

	public MazeScript getPostAppearanceScript()
	{
		return postAppearanceScript;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setEncounterTable(String encounterTable)
	{
		this.encounterTable = encounterTable;
	}

	public void setAttitude(NpcFaction.Attitude attitude)
	{
		this.attitude = attitude;
	}

	public void setAmbushStatus(
		Combat.AmbushStatus ambushStatus)
	{
		this.ambushStatus = ambushStatus;
	}

	public void setPreScript(MazeScript preScript)
	{
		this.preScript = preScript;
	}

	public void setPostAppearanceScript(MazeScript postAppearanceScript)
	{
		this.postAppearanceScript = postAppearanceScript;
	}

	public MazeScript getPartyLeavesNeutralScript()
	{
		return partyLeavesNeutralScript;
	}

	public void setPartyLeavesNeutralScript(
		MazeScript partyLeavesNeutralScript)
	{
		this.partyLeavesNeutralScript = partyLeavesNeutralScript;
	}

	public MazeScript getPartyLeavesFriendlyScript()
	{
		return partyLeavesFriendlyScript;
	}

	public void setPartyLeavesFriendlyScript(
		MazeScript partyLeavesFriendlyScript)
	{
		this.partyLeavesFriendlyScript = partyLeavesFriendlyScript;
	}
}

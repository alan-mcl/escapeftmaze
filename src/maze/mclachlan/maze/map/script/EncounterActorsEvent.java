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
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
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
	private final String mazeVariable;
	private final String encounterTable;
	private final NpcFaction.Attitude attitude;
	private Combat.AmbushStatus ambushStatus;
	private final String preScript, postAppearanceScript;

	// volatile
	private EncounterTable encounterTableRef;

	/*-------------------------------------------------------------------------*/
	public EncounterActorsEvent(
		String mazeVariable,
		String encounterTable,
		NpcFaction.Attitude attitude,
		Combat.AmbushStatus ambushStatus,
		String preScript,
		String postAppearanceScript)
	{
		this.mazeVariable = mazeVariable;
		this.encounterTable = encounterTable;
		this.attitude = attitude;
		this.ambushStatus = ambushStatus;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
	}

	/*-------------------------------------------------------------------------*/
	public EncounterActorsEvent(
		String mazeVariable,
		EncounterTable encounterTable,
		NpcFaction.Attitude attitude,
		Combat.AmbushStatus ambushStatus,
		String preScript,
		String postAppearanceScript)
	{
		this.mazeVariable = mazeVariable;
		this.encounterTable = encounterTable.getName();
		this.attitude = attitude;
		this.ambushStatus = ambushStatus;

		this.encounterTableRef = encounterTable;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
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
				allFoes);
		}

		List<MazeEvent> preScriptEvents;
		if (preScript != null)
		{
			preScriptEvents = Database.getInstance().getMazeScript(preScript).getEvents();
		}
		else
		{
			preScriptEvents = new ArrayList<>();
		}

		List<MazeEvent> postAppearanceScriptEvents;
		if (postAppearanceScript != null)
		{
			postAppearanceScriptEvents = Database.getInstance().getMazeScript(postAppearanceScript).getEvents();
		}
		else
		{
			postAppearanceScriptEvents = new ArrayList<>();
		}


		Maze.getInstance().encounterActors(
			new ActorEncounter(allFoes, mazeVariable, attitude, ambushStatus, preScriptEvents, postAppearanceScriptEvents));

		return null;
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

	public String getPreScript()
	{
		return preScript;
	}

	public String getPostAppearanceScript()
	{
		return postAppearanceScript;
	}
}

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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 * Initiates an encounter.
 */
public class Encounter extends TileScript
{
	private EncounterTable encounterTable;
	private String mazeVariable;
	private NpcFaction.Attitude attitude;
	private String preScript, postAppearanceScript;
	private Combat.AmbushStatus ambushStatus;

	public Encounter()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param encounterTable
	 * 	The foes to encounter
	 * @param mazeVariable
	 * 	The maze variable associated with this encounter.  This encounter will
	 * 	only execute if this variable has not been set yet, and when complete
	 * @param attitude
	 * @param ambushStatus
	 */ 
	public Encounter(
		EncounterTable encounterTable,
		String mazeVariable,
		NpcFaction.Attitude attitude,
		Combat.AmbushStatus ambushStatus,
		String preScript,
		String postAppearanceScript)
	{
		this.encounterTable = encounterTable;
		this.mazeVariable = mazeVariable;
		this.attitude = attitude;
		this.ambushStatus = ambushStatus;
		this.preScript = preScript;
		this.postAppearanceScript = postAppearanceScript;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (isRovingSpritesMode(maze))
		{
/*			// copied from EncounterActorsEvent

			if (this.mazeVariable != null)
			{
				if (MazeVariables.getBoolean(this.mazeVariable))
				{
					return;
				}
			}

			FoeEntry foeEntry = encounterTable.getEncounterTable().getRandomItem();
			List<FoeGroup> allFoes = foeEntry.generate();

			if (ambushStatus == null)
			{
				ambushStatus = GameSys.getInstance().determineAmbushStatus(
					Maze.getInstance().getParty(),
					allFoes);
			}

			List<MazeEvent> events;
			if (preScript != null)
			{
				events = Database.getInstance().getMazeScript(preScript).getEvents();
			}
			else
			{
				events = new ArrayList<>();
				events.add(new FlavourTextEvent("BLAH")); // todo remove
			}

			Maze.getInstance().encounterActors(
				new ActorEncounter(allFoes, mazeVariable, attitude, ambushStatus, events));*/
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (!isRovingSpritesMode(maze))
		{
			return getList(
				new EncounterActorsEvent(
					mazeVariable,
					encounterTable,
					attitude,
					ambushStatus,
					preScript,
					postAppearanceScript));
		}
		else
		{
			return null;
		}
	}

	public boolean isRovingSpritesMode(Maze maze)
	{
		return false; //Boolean.getBoolean(maze.getAppConfig().get(Maze.AppConfig.ROVING_SPRITES_MODE));
	}

	/*-------------------------------------------------------------------------*/
	public EncounterTable getEncounterTable()
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

	public void setEncounterTable(EncounterTable encounterTable)
	{
		this.encounterTable = encounterTable;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setAttitude(NpcFaction.Attitude attitude)
	{
		this.attitude = attitude;
	}

	public void setPreScript(String preScript)
	{
		this.preScript = preScript;
	}

	public void setPostAppearanceScript(String postAppearanceScript)
	{
		this.postAppearanceScript = postAppearanceScript;
	}

	public void setAmbushStatus(
		Combat.AmbushStatus ambushStatus)
	{
		this.ambushStatus = ambushStatus;
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
		if (!super.equals(o))
		{
			return false;
		}

		Encounter encounter = (Encounter)o;

		if (getEncounterTable() != null ? !getEncounterTable().equals(encounter.getEncounterTable()) : encounter.getEncounterTable() != null)
		{
			return false;
		}
		if (getMazeVariable() != null ? !getMazeVariable().equals(encounter.getMazeVariable()) : encounter.getMazeVariable() != null)
		{
			return false;
		}
		if (getAttitude() != encounter.getAttitude())
		{
			return false;
		}
		if (getPreScript() != null ? !getPreScript().equals(encounter.getPreScript()) : encounter.getPreScript() != null)
		{
			return false;
		}
		if (getPostAppearanceScript() != null ? !getPostAppearanceScript().equals(encounter.getPostAppearanceScript()) : encounter.getPostAppearanceScript() != null)
		{
			return false;
		}
		return getAmbushStatus() == encounter.getAmbushStatus();
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getEncounterTable() != null ? getEncounterTable().hashCode() : 0);
		result = 31 * result + (getMazeVariable() != null ? getMazeVariable().hashCode() : 0);
		result = 31 * result + (getAttitude() != null ? getAttitude().hashCode() : 0);
		result = 31 * result + (getPreScript() != null ? getPreScript().hashCode() : 0);
		result = 31 * result + (getPostAppearanceScript() != null ? getPostAppearanceScript().hashCode() : 0);
		result = 31 * result + (getAmbushStatus() != null ? getAmbushStatus().hashCode() : 0);
		return result;
	}
}

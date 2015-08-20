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

/**
 * Initiates an encounter.
 */
public class Encounter extends TileScript
{
	private EncounterTable encounterTable;
	private String mazeVariable;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param encounterTable
	 * 	The foes to encounter
	 * @param mazeVariable
	 * 	The maze variable associated with this encounter.  This encounter will
	 * 	only execute if this variable has not been set yet, and when complete
	 * 	will set the variable to true.
	 */ 
	public Encounter(
		EncounterTable encounterTable,
		String mazeVariable)
	{
		this.encounterTable = encounterTable;
		this.mazeVariable = mazeVariable;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		return getList(
			new EncounterActorsEvent(mazeVariable, encounterTable.getName()));
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
}

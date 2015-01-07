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

import java.util.List;
import mclachlan.maze.map.Zone;
import java.awt.*;

/**
 *
 */
public class GameState
{
	private Zone currentZone;
	private DifficultyLevel difficultyLevel;
	private Point playerPos;
	private int partyGold;
	private int partySupplies;
	private List<String> partyNames;
	private int formation;
	private int facing;
	private long turnNr;

	/*-------------------------------------------------------------------------*/
	public GameState(
		Zone currentZone,
		DifficultyLevel difficultyLevel,
		Point playerPos,
		int facing,
		int partyGold,
		int partySupplies,
		List<String> partyNames,
		int formation,
		long turnNr)
	{
		this.currentZone = currentZone;
		this.difficultyLevel = difficultyLevel;
		this.facing = facing;
		this.playerPos = playerPos;
		this.partyGold = partyGold;
		this.partySupplies = partySupplies;
		this.partyNames = partyNames;
		this.formation = formation;
		this.turnNr = turnNr;
	}

	/*-------------------------------------------------------------------------*/
	public long getTurnNr()
	{
		return turnNr;
	}

	public void setTurnNr(long turnNr)
	{
		this.turnNr = turnNr;
	}

	public Zone getCurrentZone()
	{
		return currentZone;
	}

	public void setCurrentZone(Zone currentZone)
	{
		this.currentZone = currentZone;
	}

	public int getFacing()
	{
		return facing;
	}

	public void setFacing(int facing)
	{
		this.facing = facing;
	}

	public Point getPlayerPos()
	{
		return playerPos;
	}

	public void setPlayerPos(Point playerPos)
	{
		this.playerPos = playerPos;
	}

	public int getPartyGold()
	{
		return partyGold;
	}

	public void setPartyGold(int partyGold)
	{
		this.partyGold = partyGold;
	}

	public int getFormation()
	{
		return formation;
	}

	public void setFormation(int formation)
	{
		this.formation = formation;
	}

	public List<String> getPartyNames()
	{
		return partyNames;
	}

	public void setPartyNames(List<String> partyNames)
	{
		this.partyNames = partyNames;
	}

	public DifficultyLevel getDifficultyLevel()
	{
		return difficultyLevel;
	}

	public void setDifficultyLevel(DifficultyLevel difficultyLevel)
	{
		this.difficultyLevel = difficultyLevel;
	}

	public int getPartySupplies()
	{
		return partySupplies;
	}

	public void setPartySupplies(int partySupplies)
	{
		this.partySupplies = partySupplies;
	}
}


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

import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.Foe;

/**
 *
 */
public class DifficultyLevel extends DataObject
{
	private String name;
	private int sortOrder;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public int getSortOrder()
	{
		return sortOrder;
	}

	/*-------------------------------------------------------------------------*/
	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Called each time the engine spawns a foe. This difficulty implementation
	 * can make whatever modifications to each foe are required.
	 */
	public void foeIsSpawned(Foe foe)
	{
		// by default no modifications
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Called each time random encounters are checked. This difficulty
	 * implementation can modify that chance as required.
	 *
	 * @param t the Tile encountered by the player party
	 * @return the chance of a random encounter, expressed as a value out of 1000
	 * (or, if you prefer, in tenths of a percent). For example 10 represents a
	 * 1% chance of a random encounter
	 */
	public int getRandomEncounterChance(Tile t)
	{
		return t.getRandomEncounterChance();
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Called by each combat instance to return foe AI to use. This difficulty
	 * implementation can return a custom foe AI implementation as required.
	 */
	public FoeCombatAi getFoeCombatAi()
	{
		return new BasicFoeAi();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && getClass() == o.getClass();
	}
}

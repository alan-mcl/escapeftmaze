/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.balance;

import mclachlan.maze.game.GameState;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.stat.Dice;

/**
 *
 */
public class EncounterListener extends MazeWalker.Listener
{
	private int fixedEncounters, randomEncounters;
	// todo: more

	@Override
	public void walk(GameState gs)
	{
		// todo: difficulty level
		Tile tile = gs.getCurrentZone().getTile(gs.getPlayerPos());

		if (tile.getScripts() != null)
		{
			for (TileScript ts : tile.getScripts())
			{
				if (ts instanceof Encounter)
				{
					fixedEncounters++;
					return;
				}
			}
		}

		if (Dice.d1000.roll() <= tile.getRandomEncounterChance())
		{
			randomEncounters++;
		}
	}

	@Override
	public String describe()
	{
		int total = fixedEncounters + randomEncounters;
		return "Number of encounters: "+total+" ("+
			fixedEncounters+" set, "+randomEncounters+" random)";
	}
}

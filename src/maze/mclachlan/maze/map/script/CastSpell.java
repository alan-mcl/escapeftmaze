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
import mclachlan.maze.map.TileScript;

/**
 * A tile script that fires a spell at the party.
 */
public class CastSpell extends TileScript
{
	private String spellName;
	private int castingLevel;
	private int casterLevel;

	/*-------------------------------------------------------------------------*/
	public CastSpell(String spell, int castingLevel, int casterLevel)
	{
		this.spellName = spell;
		this.castingLevel = castingLevel;
		this.casterLevel = casterLevel;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.add(new CastSpellEvent(spellName, casterLevel, castingLevel));
		return result;
	}

	public int getCasterLevel()
	{
		return casterLevel;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public String getSpellName()
	{
		return spellName;
	}
}

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

package mclachlan.maze.campaign.def.game;

import mclachlan.maze.game.DifficultyLevel;

/**
 * Normal difficulty is simply the data as it stands in the db
 */
public class NormalDifficulty extends DifficultyLevel
{
	@Override
	public boolean isDefaultSelection()
	{
		return true;
	}

	@Override
	public String getDisplayName()
	{
		return "Adventurer Mode";
	}

	@Override
	public String getDescription()
	{
		return "\"Get yer gear and let's away wi'out delay. Though the way be testing, together we'll yet prevail!\"\n~Gurney, captain of the West Wind";
	}

	@Override
	public String getImage()
	{
		return "screen/normal_difficulty";
	}
}

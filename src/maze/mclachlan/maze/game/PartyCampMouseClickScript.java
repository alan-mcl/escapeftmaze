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

import mclachlan.crusader.Map;
import mclachlan.crusader.MouseClickScript;
import mclachlan.maze.game.event.InitiatePartyCampEvent;

/**
 * Opens the party camp dialog when the camp object is clicked.
 */
public class PartyCampMouseClickScript implements MouseClickScript
{
	@Override
	public void initialise(Map map)
	{
	}

	@Override
	public void execute(Map map)
	{
		Maze.getInstance().appendEvents(new InitiatePartyCampEvent());
	}

	@Override
	public int getMaxDist()
	{
		// same tile as the party; match Lever/Chest click range
		return 1;
	}

	@Override
	public MouseClickScript copyScript()
	{
		return new PartyCampMouseClickScript();
	}
}

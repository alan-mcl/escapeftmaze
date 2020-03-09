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

package mclachlan.maze.stat;

import mclachlan.maze.data.v1.DataObject;

/**
 * A table describing the experience required for a character to advance a
 * level.
 */
public abstract class ExperienceTable extends DataObject

{
	/*-------------------------------------------------------------------------*/
	public abstract String getName();

	public abstract void setName(String name);

	/*-------------------------------------------------------------------------*/
	/**
	 * @param currentLevel
	 * 	The characters current level
	 * @return
	 * 	The experience total at which the character will advance to a new
	 * 	level.
	 */
	public abstract int getNextLevelUp(int currentLevel);

	/*-------------------------------------------------------------------------*/
	/**
	 * @param currentLevel
	 * 	The characters current level
	 * @return
	 * 	The experience total at which the character last advanced to a new
	 * 	level.
	 */
	public abstract int getLastLevelUp(int currentLevel);
}

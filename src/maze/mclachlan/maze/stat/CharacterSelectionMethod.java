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

import java.util.List;

/**
 * One step in a {@link CharacterSelection}. Automatic methods implement
 * {@link #select(List)}; {@link PlayerCharacterSelection} is driven by the
 * runtime Who dialog instead.
 */
public abstract class CharacterSelectionMethod
{
	/*-------------------------------------------------------------------------*/
	/**
	 * @param candidates
	 * 	Eligible party members for this step.
	 * @return
	 * 	Selected characters (may be empty). Player selection must not call this.
	 */
	public List<PlayerCharacter> select(List<PlayerCharacter> candidates)
	{
		throw new UnsupportedOperationException(
			"PlayerCharacterSelection is resolved via Who dialog");
	}

	/*-------------------------------------------------------------------------*/
	public boolean isPlayerSelection()
	{
		return this instanceof PlayerCharacterSelection;
	}

	/*-------------------------------------------------------------------------*/
	public String describe()
	{
		return getClass().getSimpleName();
	}

	public String toString()
	{
		return describe();
	}
}

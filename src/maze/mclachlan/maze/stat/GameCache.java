/*
 * Copyright (c) 2013 Alan McLachlan
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

import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;

public interface GameCache
{
	/**
	 * Loads item caches state.
	 */
	void loadGame(String name, Loader loader) throws Exception;

	/*-------------------------------------------------------------------------*/
	void saveGame(String saveGameName, Saver saver) throws Exception;

	/*-------------------------------------------------------------------------*/
	void endOfTurn(long turnNr);
}

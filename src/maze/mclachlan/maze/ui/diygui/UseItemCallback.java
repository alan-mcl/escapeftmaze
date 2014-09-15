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

package mclachlan.maze.ui.diygui;

import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.SpellTarget;

/**
 *
 */
public interface UseItemCallback
{
	/**
	 * @param item
	 * 	The item to use
	 * @param user
	 * 	The player character using this item
	 * @param userIndex
	 * 	The PC using it
	 * @param target
	 * 	The target of this item invocation
	 * @return
	 * 	true if this class handled the item use, false if the default 
	 * 	out of combat behaviour must take place.
	 */
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target);
}

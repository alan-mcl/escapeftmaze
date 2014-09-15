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

/**
 * Created by IntelliJ IDEA. User: Alan Date: 2007/11/13 Time: 6:34:23 PM To
 * change this template use File | Settings | File Templates.
 */
public interface TheftCallback
{
	void grabAndAttack(Item item, PlayerCharacter pc);

	void stealItem(Item item, PlayerCharacter pc);
}

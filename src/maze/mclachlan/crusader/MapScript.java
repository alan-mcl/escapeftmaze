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

package mclachlan.crusader;

/**
 * Base class for scripts that dynamically alter state of the map.
 */
public abstract class MapScript
{
	/**
	 * This method is invoked every frame, giving subclasses a chance to
	 * change the state of the map.
	 * 
	 * @param framecount
	 * 	An indication of the framecount
	 * @param map
	 * 	The map instance on which this script must operate
	 */ 
	public abstract void execute(int framecount, Map map);
}

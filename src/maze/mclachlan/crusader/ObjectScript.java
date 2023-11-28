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
 * Base class for scripts that dynamically alter state of an EngineObject.
 */
public abstract class ObjectScript
{
	/**
	 * Create a new instance of this script related to the given object.
	 */	public abstract ObjectScript spawnNewInstance(EngineObject object, CrusaderEngine engine);

	/**
	 * Initialise this to operate on the given EngineObject
	 */
	public abstract void init(EngineObject obj, CrusaderEngine engine);

	public abstract void execute(long framecount, EngineObject obj);
}

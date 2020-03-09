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

package mclachlan.maze.editor.swing;

import java.util.*;
import mclachlan.maze.data.v1.DataObject;

public interface IEditorPanel
{
	Vector<DataObject> loadData();

	void refresh(String name);

	/**
	 * Instantiates a new data object for this editor pane, adds it to the
	 * in-memory database cache, and returns it
	 * @param name
	 * 	The key of the new data object
	 * @return
	 * 	The newly created data object, already added to the cache
	 */
	DataObject newItem(String name);

	void renameItem(String newName);

	/**
	 * Copies the current data object into a new instance and adds it to the
	 * in-memory database cache under a new key.
	 * @param newName
	 * 	The key of the copied instance
	 * @return
	 * 	The newly created data object, already added to the cache
	 */
	DataObject copyItem(String newName);

	void deleteItem();

	/**
	 * Writes the current UI values to the in-memory database cache.
	 * @param name
	 * 	key of the current data object
	 * @return
	 * 	the data object committed to the cache
	 */
	DataObject commit(String name);

	String getCurrentName();

	void refreshNames(String name);

	int getDirtyFlag();

	void initForeignKeys();

	void reload();
}

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

package mclachlan.maze.editor.swing.map;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/redo stack for {@link MapEditor} using {@link MapZoneSnapshot}s.
 */
public class MapEditHistory
{
	private static final int MAX_DEPTH = 50;

	private final Deque<Entry> undoStack = new ArrayDeque<>();
	private final Deque<Entry> redoStack = new ArrayDeque<>();

	/*-------------------------------------------------------------------------*/
	public void record(String label, MapZoneSnapshot before, MapZoneSnapshot after)
	{
		if (before.sameContentAs(after))
		{
			return;
		}

		undoStack.addLast(new Entry(label, before, after));
		while (undoStack.size() > MAX_DEPTH)
		{
			undoStack.removeFirst();
		}
		redoStack.clear();
	}

	/*-------------------------------------------------------------------------*/
	public boolean canUndo()
	{
		return !undoStack.isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public boolean canRedo()
	{
		return !redoStack.isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public String getUndoLabel()
	{
		return undoStack.isEmpty() ? null : undoStack.peekLast().label;
	}

	/*-------------------------------------------------------------------------*/
	public String getRedoLabel()
	{
		return redoStack.isEmpty() ? null : redoStack.peekLast().label;
	}

	/*-------------------------------------------------------------------------*/
	public MapZoneSnapshot undo()
	{
		if (undoStack.isEmpty())
		{
			return null;
		}

		Entry entry = undoStack.removeLast();
		redoStack.addLast(entry);
		return entry.before;
	}

	/*-------------------------------------------------------------------------*/
	public MapZoneSnapshot redo()
	{
		if (redoStack.isEmpty())
		{
			return null;
		}

		Entry entry = redoStack.removeLast();
		undoStack.addLast(entry);
		return entry.after;
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		undoStack.clear();
		redoStack.clear();
	}

	/*-------------------------------------------------------------------------*/
	private static final class Entry
	{
		private final String label;
		private final MapZoneSnapshot before;
		private final MapZoneSnapshot after;

		private Entry(String label, MapZoneSnapshot before, MapZoneSnapshot after)
		{
			this.label = label;
			this.before = before;
			this.after = after;
		}
	}
}

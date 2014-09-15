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

import javax.swing.*;
import java.util.Vector;
import java.util.Collections;
import java.awt.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.FoeEntryRow;
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class FoeEntryPanel extends EditorPanel
{
	FoeEntryRowGroupOfPossibiltiesPanel foeEntryRows;

	/*-------------------------------------------------------------------------*/
	public FoeEntryPanel()
	{
		super(SwingEditor.Tab.FOE_ENTRIES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT));

		foeEntryRows = new FoeEntryRowGroupOfPossibiltiesPanel(dirtyFlag, 1.0);
		result.add(foeEntryRows);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getFoeEntries().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		foeEntryRows.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		FoeEntry fe = Database.getInstance().getFoeEntry(name);
		foeEntryRows.refresh(fe.getContains());
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		FoeEntry fe = new FoeEntry(name, new GroupOfPossibilities<FoeEntryRow>());
		Database.getInstance().getFoeEntries().put(name, fe);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeEntry fe = Database.getInstance().getFoeEntries().remove(currentName);
		fe.setName(newName);
		Database.getInstance().getFoeEntries().put(newName, fe);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		FoeEntry current = Database.getInstance().getFoeEntry(currentName);
		FoeEntry fe = new FoeEntry(newName, new GroupOfPossibilities<FoeEntryRow>(current.getContains()));
		Database.getInstance().getFoeEntries().put(newName, fe);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeEntries().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		FoeEntry fe = Database.getInstance().getFoeEntry(name);
		fe.setContains(foeEntryRows.getGroupOfPossibilties());
	}
}

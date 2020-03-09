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

import java.awt.FlowLayout;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class EncounterTablePanel extends EditorPanel
{
	FoeEntryPercentageTablePanel foeEntries;

	/*-------------------------------------------------------------------------*/
	public EncounterTablePanel()
	{
		super(SwingEditor.Tab.ENCOUNTER_TABLES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT));

		foeEntries = new FoeEntryPercentageTablePanel("Foe Entries", dirtyFlag, 1, 1);
		result.add(foeEntries);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getEncounterTables().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		foeEntries.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		EncounterTable et = Database.getInstance().getEncounterTable(name);
		foeEntries.refresh(et.getEncounterTable());
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		EncounterTable et = new EncounterTable(name, new PercentageTable<>());
		Database.getInstance().getEncounterTables().put(name, et);

		return et;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		EncounterTable et = Database.getInstance().getEncounterTables().remove(currentName);
		et.setName(newName);
		Database.getInstance().getEncounterTables().put(newName, et);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		EncounterTable current = Database.getInstance().getEncounterTable(currentName);

		EncounterTable et = new EncounterTable(
			newName,
			new PercentageTable<FoeEntry>(current.getEncounterTable()));

		Database.getInstance().getEncounterTables().put(newName, et);

		return et;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getEncounterTables().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		EncounterTable et = Database.getInstance().getEncounterTable(name);
		et.setEncounterTable(foeEntries.getPercentageTable());

		return et;
	}
}

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

import javax.swing.table.AbstractTableModel;
import mclachlan.maze.stat.Item;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ItemsTableModel extends AbstractTableModel
{
	private static final int MAX_ROWS = 32;
	String[] itemTemplates = new String[MAX_ROWS];
	int[] cursedStates = new int[MAX_ROWS];
	int[] identifiedStates = new int[MAX_ROWS];
	int[] currentStack = new int[MAX_ROWS];
	int[] currentCharges = new int[MAX_ROWS];
	String[] slots = new String[MAX_ROWS];
	
	/*-------------------------------------------------------------------------*/
	public ItemsTableModel()
	{
		slots[0] = "Primary Weapon";
		slots[1] = "Secondary Weapon";
		slots[2] = "Alt Primary Weapon";
		slots[3] = "Alt Sec Weapon";
		slots[4] = "Helm";
		slots[5] = "Torso Armour";
		slots[6] = "Leg Armour";
		slots[7] = "Gloves";
		slots[8] = "Boots";
		slots[9] = "Banner Item";
		slots[10] = "Misc Item #1";
		slots[11] = "Misc Item #2";

		for (int i=12; i<MAX_ROWS; i++)
		{
			slots[i] = "Inventory "+i;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getColumnCount()
	{
		return 6;
	}

	/*-------------------------------------------------------------------------*/
	public int getRowCount()
	{
		return MAX_ROWS;
	}
	
	/*-------------------------------------------------------------------------*/
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0: return String.class;
			case 1: return String.class;
			case 2: return Boolean.TYPE;
			case 3: return String.class;
			case 4: return String.class;
			case 5: return String.class;
			default: throw new MazeException("invalid column: "+columnIndex);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
			case 0: return slots[rowIndex];
			case 1: return itemTemplates[rowIndex];
			case 2: return cursedStates[rowIndex];
			case 3: return identifiedStates[rowIndex] == Item.IdentificationState.IDENTIFIED;
			case 4: return currentStack[rowIndex];
			case 5: return currentCharges[rowIndex];
			default: throw new MazeException("invalid column: "+columnIndex);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0: return "Slot";
			case 1: return "Item Template";
			case 2: return "Cursed State";
			case 3: return "Identified?";
			case 4: return "Current Stack";
			case 5: return "Current Charges";
			default: throw new MazeException("invalid column: "+column);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex != 0;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
			case 1: itemTemplates[rowIndex] = (String)aValue;
			case 2: cursedStates[rowIndex] = (Integer)aValue;
			case 3: identifiedStates[rowIndex] = ((Boolean)aValue?Item.IdentificationState.IDENTIFIED:Item.IdentificationState.UNIDENTIFIED);
			case 4: currentStack[rowIndex] = (Integer)aValue;
			case 5: currentCharges[rowIndex] = (Integer)aValue;
			default: throw new MazeException("invalid column: "	+columnIndex);
		}
	}
}

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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootEntryRow;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class LootEntryPanel extends EditorPanel
{
	private JTable table;
	private JButton add, remove, spread;
	private JComboBox itemTemplates;
	private LootEntryTableModel dataModel;

	/*-------------------------------------------------------------------------*/
	public LootEntryPanel()
	{
		super(SwingEditor.Tab.LOOT_ENTRIES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel editControls = new JPanel(new GridBagLayout());

		dataModel = new LootEntryTableModel(dirtyFlag);
		itemTemplates = new JComboBox();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.setDefaultEditor(Integer.TYPE, new DefaultCellEditor(new JTextField()));
		table.setDefaultEditor(ItemTemplate.class, new DefaultCellEditor(itemTemplates));
		table.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(0).setPreferredWidth(10);

		JScrollPane scroller = new JScrollPane(table);

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);

		spread = new JButton("Spread %'s");
		spread.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls.add(new JLabel("Loot Entry:"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 3;
		editControls.add(new JLabel("This is a % table, one of the below will be generated"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		editControls.add(add, gbc);
		gbc.gridx++;
		editControls.add(remove, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(spread, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth = 3;
		editControls.add(scroller, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getLootEntries().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(vec);
		itemTemplates.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		LootEntry le = Database.getInstance().getLootEntry(name);
		List<Integer> percentages = le.getContains().getPercentages();
		List<LootEntryRow> items = le.getContains().getItems();

		dataModel.clear();
		for (int i=0; i<percentages.size(); i++)
		{
			dataModel.add(percentages.get(i), items.get(i).getItemName(), V1Dice.toString(items.get(i).getQuantity()));
		}
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		LootEntry le = new LootEntry(name, new PercentageTable<LootEntryRow>());
		Database.getInstance().getLootEntries().put(name, le);

		return le;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		LootEntry le = Database.getInstance().getLootEntries().remove(currentName);
		le.setName(newName);
		Database.getInstance().getLootEntries().put(newName, le);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		LootEntry current = Database.getInstance().getLootEntries().get(currentName);
		LootEntry le = new LootEntry(newName, new PercentageTable<LootEntryRow>(current.getContains()));
		Database.getInstance().getLootEntries().put(newName, le);

		return le;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getLootEntries().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		LootEntry le = Database.getInstance().getLootEntries().get(name);
		
		if (le == null)
		{
			return null;
		}

		// we're basically just committing the percentage table
		PercentageTable<LootEntryRow> pt = new PercentageTable<LootEntryRow>(false);
		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			int precent = dataModel.percentages.get(i);
			String itemName = dataModel.items.get(i);
			Dice quantity = V1Dice.fromString(dataModel.quantities.get(i));
			pt.add(new LootEntryRow(itemName, quantity), precent);
		}

		le.setContains(pt);

		return le;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(0, (String)itemTemplates.getItemAt(0), "1d1");
		}
		else if (e.getSource() == remove)
		{
			if (table.getSelectedRow() > -1)
			{
				dataModel.remove(table.getSelectedRow());
			}
		}
		else if (e.getSource() == spread)
		{
			dataModel.spread();
		}
		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/
	static class LootEntryTableModel extends AbstractTableModel
	{
		int dirtyFlag;

		List<Integer> percentages = new ArrayList<Integer>();
		List<String> items = new ArrayList<String>();
		List<String> quantities = new ArrayList<String>();

		/*----------------------------------------------------------------------*/
		public LootEntryTableModel(int dirtyFlag)
		{
			this.dirtyFlag = dirtyFlag;
		}

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "%";
				case 1: return "Item";
				case 2: return "Quantity";
				default: throw new MazeException("Invalid column "+column);
			}
		}

		/*----------------------------------------------------------------------*/
		public int getColumnCount()
		{
			return 3;
		}

		/*----------------------------------------------------------------------*/
		public int getRowCount()
		{
			return percentages.size();
		}

		/*----------------------------------------------------------------------*/
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return percentages.get(rowIndex);
				case 1: return items.get(rowIndex);
				case 2: return quantities.get(rowIndex);
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			switch (columnIndex)
			{
				case 0: percentages.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 1: items.set(rowIndex, (String)aValue); break;
				case 2: quantities.set(rowIndex, (String)aValue); break;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return Integer.TYPE;
				case 1: return ItemTemplate.class;
				case 2: return String.class;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		/*----------------------------------------------------------------------*/
		public void clear()
		{
			percentages.clear();
			items.clear();
			quantities.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, String item, String qty)
		{
			percentages.add(perc);
			items.add(item);
			quantities.add(qty);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			items.remove(index);
			quantities.remove(index);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void spread()
		{
			int base = 100/percentages.size();
			int mod = 100%percentages.size();

			for (int i=0; i<percentages.size(); i++)
			{
				percentages.set(i, i<mod?base+1:base);
			}
			fireTableDataChanged();
		}
	}
}

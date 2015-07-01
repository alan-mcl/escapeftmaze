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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.*;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class LootTablePanel extends EditorPanel
{
	private JTable table;
	private JButton add, remove, inline;
	private JComboBox lootEntriesCombo;
	private LootTableTableModel dataModel;

	/*-------------------------------------------------------------------------*/
	public LootTablePanel()
	{
		super(SwingEditor.Tab.LOOT_TABLES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel editControls = new JPanel(new GridBagLayout());

		dataModel = new LootTableTableModel(dirtyFlag);
		lootEntriesCombo = new JComboBox();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.setDefaultEditor(Integer.TYPE, new DefaultCellEditor(new JTextField()));
		table.setDefaultEditor(LootEntry.class, new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		table.addKeyListener(this);

		JScrollPane scroller = new JScrollPane(table);

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		inline = new JButton("Inline Loot Entry");
		inline.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls.add(new JLabel("Loot Table:"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 3;
		editControls.add(new JLabel("Each loot entry is checked in turn"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		editControls.add(add, gbc);
		gbc.gridx++;
		editControls.add(remove, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(inline, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth = 3;
		editControls.add(scroller, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getLootTables().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getLootEntries().keySet());
		Collections.sort(vec);
		lootEntriesCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		LootTable le = Database.getInstance().getLootTable(name);

		GroupOfPossibilities<ILootEntry> possibilities = le.getLootEntries();
		if (possibilities == null)
		{
			return;
		}
		
		List<Integer> percentages = possibilities.getPercentages();
		List<ILootEntry> possiblities = le.getLootEntries().getPossibilities();

		dataModel.clear();
		for (int i=0; i<percentages.size(); i++)
		{
			dataModel.add(percentages.get(i), possiblities.get(i));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		LootTable le = new LootTable(name, new GroupOfPossibilities<ILootEntry>());
		Database.getInstance().getLootTables().put(name, le);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		LootTable le = Database.getInstance().getLootTables().remove(currentName);
		le.setName(newName);
		Database.getInstance().getLootTables().put(newName, le);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		LootTable current = Database.getInstance().getLootTables().get(currentName);
		LootTable le = new LootTable(newName, new GroupOfPossibilities<ILootEntry>(current.getLootEntries()));
		Database.getInstance().getLootTables().put(newName, le);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getLootTables().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		LootTable le = Database.getInstance().getLootTables().get(name);

		// we're basically just committing the GOP
		GroupOfPossibilities<ILootEntry> gop = new GroupOfPossibilities<ILootEntry>();
		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			int precent = dataModel.percentages.get(i);
			ILootEntry entry = dataModel.lootEntries.get(i);
			gop.add(entry, precent);
		}

		le.setLootEntries(gop);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			LootEntryDialog dialog = new LootEntryDialog();
			dialog.setVisible(true);

			if (dialog.result != null)
			{
				dataModel.add(100, dialog.result);
			}
		}
		else if (e.getSource() == remove)
		{
			if (table.getSelectedRow() > -1)
			{
				dataModel.remove(table.getSelectedRow());
			}
		}
		else if (e.getSource() == inline)
		{
			if (table.getSelectedRow() > -1)
			{
				dataModel.inline(table.getSelectedRow());
			}
		}
		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getSource() == table)
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_DELETE:
					if (table.getSelectedRow() > -1)
					{
						dataModel.remove(table.getSelectedRow());
					}
					break;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	class LootEntryDialog extends JDialog implements ActionListener
	{
		ILootEntry result;
		JRadioButton singleItem, lootEntry;
		JButton ok, cancel;
		JComboBox itemsCombo;

		LootEntryDialog()
		{
			super(SwingEditor.instance, "Loot Entry", true);

			ok = new JButton("OK");
			ok.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);

			lootEntry = new JRadioButton("Loot Entry");
			lootEntry.addActionListener(this);
			singleItem = new JRadioButton("Single Item");
			singleItem.addActionListener(this);
			ButtonGroup bg = new ButtonGroup();
			bg.add(singleItem);
			bg.add(lootEntry);

			Vector<String> vec = new Vector<String>(Database.getInstance().getItemList());
			Collections.sort(vec);
			itemsCombo = new JComboBox(vec);

			lootEntry.setSelected(true);
			itemsCombo.setEnabled(false);

			this.setLayout(new GridLayout(3,2));

			this.add(lootEntry);
			this.add(lootEntriesCombo);
			this.add(singleItem);
			this.add(itemsCombo);
			this.add(ok);
			this.add(cancel);

			pack();
			setLocationRelativeTo(LootTablePanel.this);
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == singleItem)
			{
				itemsCombo.setEnabled(true);
				lootEntriesCombo.setEnabled(false);
			}
			else if (e.getSource() == lootEntry)
			{
				itemsCombo.setEnabled(false);
				lootEntriesCombo.setEnabled(true);
			}
			else if (e.getSource() == ok)
			{
				if (singleItem.isSelected())
				{
					result = new SingleItemLootEntry((String)itemsCombo.getSelectedItem());
				}
				else
				{
					result = Database.getInstance().getLootEntry((String)lootEntriesCombo.getSelectedItem());
				}
				setVisible(false);
			}
			else if (e.getSource() == cancel)
			{
				result = null;
				setVisible(false);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static class LootTableTableModel extends AbstractTableModel
	{
		int dirtyFlag;

		List<Integer> percentages = new ArrayList<Integer>();
		List<ILootEntry> lootEntries = new ArrayList<ILootEntry>();

		/*----------------------------------------------------------------------*/
		public LootTableTableModel(int dirtyFlag)
		{
			this.dirtyFlag = dirtyFlag;
		}

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "%";
				case 1: return "Loot Entry";
				default: throw new MazeException("Invalid column "+column);
			}
		}

		/*----------------------------------------------------------------------*/
		public int getColumnCount()
		{
			return 2;
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
				case 1: return lootEntries.get(rowIndex).getName();
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
				case 1: lootEntries.set(rowIndex, (ILootEntry)aValue); break;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return Integer.TYPE;
				case 1: return LootEntry.class;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex != 1;
		}

		/*----------------------------------------------------------------------*/
		public void clear()
		{
			percentages.clear();
			lootEntries.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, ILootEntry item)
		{
			percentages.add(perc);
			lootEntries.add(item);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			lootEntries.remove(index);
			fireTableDataChanged();
		}

		/*-------------------------------------------------------------------------*/
		public void inline(int index)
		{
			ILootEntry lootEntry = lootEntries.get(index);

			remove(index);

			PercentageTable<LootEntryRow> percentageTable = lootEntry.getPercentageTable();

			for (LootEntryRow ler : percentageTable.getItems())
			{
				add(percentageTable.getPercentage(ler),
					new SingleItemLootEntry(ler.getItemName()));
			}
		}
	}
}

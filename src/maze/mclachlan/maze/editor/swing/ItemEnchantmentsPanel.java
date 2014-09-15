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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1StatModifier;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.stat.ItemEnchantment;
import mclachlan.maze.stat.ItemEnchantments;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ItemEnchantmentsPanel extends EditorPanel
{
	private JTable table;
	private JButton add, edit, remove, spread, quickFill, clear;
	private static EnchantmentTableModel dataModel = new EnchantmentTableModel(SwingEditor.Tab.ITEM_ENCHANTMENTS);
	private ItemEnchantmentScheme scheme = new ItemEnchantmentScheme();

	/*-------------------------------------------------------------------------*/
	public ItemEnchantmentsPanel()
	{
		super(SwingEditor.Tab.ITEM_ENCHANTMENTS);
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<ItemEnchantment> getEnchantments()
	{
		if (dataModel.enchantments.isEmpty())
		{
			return null;
		}
		else
		{
			return new PercentageTable<ItemEnchantment>(
				new ArrayList<ItemEnchantment>(dataModel.enchantments),
				new ArrayList<Integer>(dataModel.percentages),
				true);
		}
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());

		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.setDefaultEditor(Integer.TYPE, new DefaultCellEditor(new JTextField()));
		table.setDefaultEditor(LootEntry.class, new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(0).setPreferredWidth(10);

		JScrollPane scroller = new JScrollPane(table);

		add = new JButton("Add");
		add.addActionListener(this);
		edit = new JButton("Edit");
		edit.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		spread = new JButton("Spread %'s");
		spread.addActionListener(this);
		quickFill = new JButton("Quick Fill");
		quickFill.addActionListener(this);
		clear = new JButton("Clear");
		clear.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridwidth = 6;
		result.add(new JLabel("Enchantments:"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 6;
		result.add(new JLabel("Only one will be generated:"), gbc);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth = 1;
		result.add(add, gbc);
		gbc.gridx++;
		result.add(edit, gbc);
		gbc.gridx++;
		result.add(remove, gbc);
		gbc.gridx++;
		result.add(spread, gbc);
		gbc.gridx++;
		result.add(quickFill, gbc);

		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(clear, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 6;
		result.add(scroller, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			ItemEnchantmentDialog dialog = new ItemEnchantmentDialog(null);
			dialog.setVisible(true);

			if (dialog.result != null)
			{
				dataModel.add(0, dialog.result);
			}
		}
		else if (e.getSource() == edit)
		{
			int row = table.getSelectedRow();
			if (row != -1)
			{
				ItemEnchantment ie = dataModel.enchantments.get(row);
				ItemEnchantmentDialog dialog = new ItemEnchantmentDialog(ie);
				dialog.setVisible(true);

				if (dialog.result != null)
				{
					dataModel.set(row, dialog.result);
				}
			}
		}
		else if (e.getSource() == remove)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			if (table.getSelectedRow() > -1)
			{
				dataModel.remove(table.getSelectedRow());
			}
		}
		else if (e.getSource() == spread)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			dataModel.spread();
		}
		else if (e.getSource() == quickFill)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			quickFillEnchantments();
		}
		else if (e.getSource() == clear)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			dataModel.clear();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickFillEnchantments()
	{
		dataModel.clear();
		List<ItemEnchantment> enchantments = scheme.quickFillEnchantments();
		for (ItemEnchantment ie : enchantments)
		{
			dataModel.add(0, ie);
		}
		dataModel.spread();
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemEnchantments().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		ItemEnchantments ie = Database.getInstance().getItemEnchantments().get(name);

		PercentageTable<ItemEnchantment> enchantments = ie.getEnchantments();

		dataModel.clear();

		if (enchantments == null)
		{
			return;
		}

		List<Integer> percentages = enchantments.getPercentages();

		for (int i=0; i<percentages.size(); i++)
		{
			Integer perc = percentages.get(i);
			List<ItemEnchantment> list = enchantments.getItems();
			ItemEnchantment cur = list.get(i);
			dataModel.add(perc, cur);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		ItemEnchantments ie = new ItemEnchantments(name, new PercentageTable<ItemEnchantment>());
		Database.getInstance().getItemEnchantments().put(name, ie);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		ItemEnchantments ie = Database.getInstance().getItemEnchantments().remove(currentName);
		ie.setName(newName);
		Database.getInstance().getItemEnchantments().put(newName, ie);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		ItemEnchantments current = Database.getInstance().getItemEnchantments().get(currentName);

		ItemEnchantments ie = new ItemEnchantments(
			newName,
			new PercentageTable<ItemEnchantment>(current.getEnchantments()));

		Database.getInstance().getItemEnchantments().put(newName, ie);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getItemEnchantments().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		ItemEnchantments ie = Database.getInstance().getItemEnchantments().get(currentName);

		ie.setEnchantments(getEnchantments());
	}

	/*-------------------------------------------------------------------------*/
	class ItemEnchantmentDialog extends JDialog implements ActionListener
	{
		ItemEnchantment result;
		JTextField name, prefix, suffix;
		StatModifierComponent modifiers;
		JButton ok, cancel;
		JSpinner costModifier;

		ItemEnchantmentDialog(ItemEnchantment enchantment)
		{
			super(SwingEditor.instance, "Item Enchantment", true);

			ok = new JButton("OK");
			ok.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);

			name = new JTextField(20);
			prefix = new JTextField(20);
			suffix = new JTextField(20);
			modifiers = new StatModifierComponent(ItemEnchantmentsPanel.this.dirtyFlag);
			costModifier = new JSpinner(new SpinnerNumberModel(0,-99999,99999,1));

			this.setLayout(new GridLayout(6,2));

			this.add(new JLabel("Name:"));
			this.add(name);
			this.add(new JLabel("Prefix:"));
			this.add(prefix);
			this.add(new JLabel("Suffix:"));
			this.add(suffix);
			this.add(new JLabel("Modifier:"));
			this.add(modifiers);
			this.add(new JLabel("Cost Modifier:"));
			this.add(costModifier);
			this.add(ok);
			this.add(cancel);

			if (enchantment != null)
			{
				name.setText(enchantment.getName());
				prefix.setText(enchantment.getPrefix());
				suffix.setText(enchantment.getSuffix());
				modifiers.refresh(enchantment.getModifiers());
				costModifier.setValue(enchantment.getCostModifier());
			}

			pack();
			setLocationRelativeTo(ItemEnchantmentsPanel.this);
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == ok)
			{
				result = new ItemEnchantment(
					name.getText(),
					prefix.getText().length()==0?null:prefix.getText(),
					suffix.getText().length()==0?null:suffix.getText(),
					modifiers.getModifier(),
					(Integer)costModifier.getValue());
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
	static class EnchantmentTableModel extends AbstractTableModel
	{
		int dirtyFlag;

		List<Integer> percentages = new ArrayList<Integer>();
		List<ItemEnchantment> enchantments = new ArrayList<ItemEnchantment>();

		/*----------------------------------------------------------------------*/
		public EnchantmentTableModel(int dirtyFlag)
		{
			this.dirtyFlag = dirtyFlag;
		}

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "%";
				case 1: return "Name";
				case 2: return "Prefix";
				case 3: return "Suffix";
				case 4: return "StatModifier";
				case 5: return "Cost";
				default: throw new MazeException("Invalid column "+column);
			}
		}

		/*----------------------------------------------------------------------*/
		public int getColumnCount()
		{
			return 6;
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
				case 1: return enchantments.get(rowIndex).getName();
				case 2: return enchantments.get(rowIndex).getPrefix();
				case 3: return enchantments.get(rowIndex).getSuffix();
				case 4: return V1StatModifier.toString(enchantments.get(rowIndex).getModifiers());
				case 5: return enchantments.get(rowIndex).getCostModifier();
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
				case 1: enchantments.get(rowIndex).setName((String)aValue); break;
				case 2: enchantments.get(rowIndex).setPrefix((String)aValue); break;
				case 3: enchantments.get(rowIndex).setSuffix((String)aValue); break;
				case 5: enchantments.get(rowIndex).setCostModifier(Integer.parseInt((String)aValue)); break;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return Integer.TYPE;
				case 1: return String.class;
				case 2: return String.class;
				case 3: return String.class;
				case 4: return String.class;
				case 5: return Integer.TYPE;
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex != 4;
		}

		/*----------------------------------------------------------------------*/
		public void clear()
		{
			percentages.clear();
			enchantments.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, ItemEnchantment item)
		{
			percentages.add(perc);
			enchantments.add(item);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			enchantments.remove(index);
			fireTableDataChanged();
		}

		/*-------------------------------------------------------------------------*/
		public void set(int index, ItemEnchantment ie)
		{
			// unchanged percentage
			enchantments.set(index, ie);
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

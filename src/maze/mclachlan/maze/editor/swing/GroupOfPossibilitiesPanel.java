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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class GroupOfPossibilitiesPanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	private JTable table;
	private JButton add, remove;
	private MyTableModel dataModel;
	private JComboBox combo;

	/*-------------------------------------------------------------------------*/
	protected GroupOfPossibilitiesPanel(int dirtyFlag, double scale)
	{
		this.dirtyFlag = dirtyFlag;

		combo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(combo));

		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		Dimension d = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(
			new Dimension((int)(d.width*scale), (int)(d.height*scale)));

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(remove);

		this.setLayout(new BorderLayout(3,3));
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createTitledBorder("Spell Effects"));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys(List<String> items)
	{
		Vector vec = new Vector(items);
		Collections.sort(vec);
		combo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities<String> getGroupOfPossibilties()
	{
		GroupOfPossibilities<String> result = new GroupOfPossibilities<String>();

		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			result.add(
				dataModel.spellEffects.get(i),
				dataModel.percentages.get(i));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(GroupOfPossibilities<String> pt)
	{
		dataModel.clear();

		if (pt != null)
		{
			List<Integer> percentages = pt.getPercentages();
			List<String> items = pt.getPossibilities();

			for (int i=0; i<percentages.size(); i++)
			{
				dataModel.add(percentages.get(i), items.get(i));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(100, (String)combo.getItemAt(0));
		}
		else if (e.getSource() == remove)
		{
			int index = table.getSelectedRow();
			if (index > -1)
			{
				dataModel.remove(index);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	class MyTableModel extends AbstractTableModel
	{
		List<Integer> percentages = new ArrayList<Integer>();
		List<String> spellEffects = new ArrayList<String>();

		/*----------------------------------------------------------------------*/
		public MyTableModel()
		{
		}

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "%";
				case 1: return "Spell Effect";
				default: throw new MazeException("Invalid column: "+column);
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
				case 1: return spellEffects.get(rowIndex);
				default: throw new MazeException("Invalid column: "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			switch (columnIndex)
			{
				case 0: percentages.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 1: spellEffects.set(rowIndex, (String)aValue); break;
				default: throw new MazeException("Invalid column: "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return Integer.TYPE;
				default: return Object.class;
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
			spellEffects.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, String s)
		{
			percentages.add(perc);
			spellEffects.add(s);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			spellEffects.remove(index);
			fireTableDataChanged();
		}
	}
}

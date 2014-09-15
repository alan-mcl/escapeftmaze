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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.map.FoeEntry;

/**
 *
 */
public class FoeEntryPercentageTablePanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	JTable table;
	JButton add, remove, spread;
	MyTableModel dataModel;
	JComboBox foeEntryCombo;

	/*-------------------------------------------------------------------------*/
	protected FoeEntryPercentageTablePanel(String title, int dirtyFlag, double scaleX, double scaleY)
	{
		this.dirtyFlag = dirtyFlag;

		foeEntryCombo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(foeEntryCombo));

		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		Dimension d = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(
			new Dimension((int)(d.width*scaleX), (int)(d.height*scaleY)));

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		spread = new JButton("Spread %'s");
		spread.addActionListener(this);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(remove);
		buttons.add(spread);

		this.setLayout(new BorderLayout(3,3));
		this.add(new JLabel("This is a % table, one of the below will be generated"), BorderLayout.NORTH);
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector vec = new Vector(Database.getInstance().getFoeEntries().keySet());
		Collections.sort(vec);
		foeEntryCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<FoeEntry> getPercentageTable()
	{
		PercentageTable<FoeEntry> result = new PercentageTable<FoeEntry>();

		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			result.add(
				Database.getInstance().getFoeEntry(dataModel.foeEntries.get(i)),
				dataModel.percentages.get(i));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(PercentageTable<FoeEntry> pt)
	{
		dataModel.clear();

		if (pt != null)
		{
			List<Integer> percentages = pt.getPercentages();
			List<FoeEntry> items = pt.getItems();

			for (int i=0; i<percentages.size(); i++)
			{
				dataModel.add(percentages.get(i), items.get(i).getName());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(0, (String)foeEntryCombo.getItemAt(0));
		}
		else if (e.getSource() == remove)
		{
			int index = table.getSelectedRow();
			if (index > -1)
			{
				dataModel.remove(index);
			}
		}
		else if (e.getSource() == spread)
		{
			dataModel.spread();
		}
	}

	/*-------------------------------------------------------------------------*/
	class MyTableModel extends AbstractTableModel
	{
		List<Integer> percentages = new ArrayList<Integer>();
		List<String> foeEntries = new ArrayList<String>();

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "%";
				case 1: return "Foe Entry";
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
				case 1: return foeEntries.get(rowIndex);
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
				case 1: foeEntries.set(rowIndex, (String)aValue); break;
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
			foeEntries.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, String bp)
		{
			percentages.add(perc);
			foeEntries.add(bp);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			foeEntries.remove(index);
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

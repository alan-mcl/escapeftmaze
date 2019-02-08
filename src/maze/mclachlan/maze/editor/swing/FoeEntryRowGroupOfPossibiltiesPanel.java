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
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.map.FoeEntryRow;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class FoeEntryRowGroupOfPossibiltiesPanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	private JTable table;
	private JButton add, remove, spread;
	private MyTableModel dataModel;
	private JComboBox foeTemplateCombo;

	/*-------------------------------------------------------------------------*/
	protected FoeEntryRowGroupOfPossibiltiesPanel(int dirtyFlag, double scale)
	{
		this.dirtyFlag = dirtyFlag;

		foeTemplateCombo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(foeTemplateCombo));
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new DiceField()));

		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		Dimension d = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(
			new Dimension((int)(d.width*scale), (int)(d.height*scale)));

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
		this.add(new JLabel("Each foe entry row is checked in turn"), BorderLayout.NORTH);
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createTitledBorder("Foe Entry Rows"));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getFoeTemplates().keySet());
		Collections.sort(vec);
		foeTemplateCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities<FoeEntryRow> getGroupOfPossibilties()
	{
		GroupOfPossibilities<FoeEntryRow> result = new GroupOfPossibilities<FoeEntryRow>();

		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			result.add(
				new FoeEntryRow(dataModel.foeNames.get(i), V1Dice.fromString(dataModel.quantities.get(i))),
				dataModel.percentages.get(i));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(GroupOfPossibilities<FoeEntryRow> pt)
	{
		dataModel.clear();

		if (pt != null)
		{
			List<Integer> percentages = pt.getPercentages();
			List<FoeEntryRow> items = pt.getPossibilities();

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
			dataModel.add(0, new FoeEntryRow((String)foeTemplateCombo.getItemAt(0), Dice.d1));
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
		List<String> foeNames = new ArrayList<String>();
		List<String> quantities = new ArrayList<String>();

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
				case 1: return "Foe Template";
				case 2: return "Quantity";
				default: throw new MazeException("Invalid column: "+column);
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
				case 1: return foeNames.get(rowIndex);
				case 2: return quantities.get(rowIndex);
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
				case 1: foeNames.set(rowIndex, (String)aValue); break;
				case 2: quantities.set(rowIndex, (String)aValue); break;
				default: throw new MazeException("Invalid column: "+columnIndex);
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
			foeNames.clear();
			quantities.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, FoeEntryRow fer)
		{
			percentages.add(perc);
			foeNames.add(fer.getFoeName());
			quantities.add(V1Dice.toString(fer.getQuantity()));
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			foeNames.remove(index);
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

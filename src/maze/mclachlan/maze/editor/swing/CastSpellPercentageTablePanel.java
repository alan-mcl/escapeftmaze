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
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.FoeAttack;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class CastSpellPercentageTablePanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	JTable table;
	JButton add, remove;
	MyTableModel dataModel;
	JComboBox spellCombo;

	/*-------------------------------------------------------------------------*/
	protected CastSpellPercentageTablePanel(int dirtyFlag, double scale)
	{
		this.dirtyFlag = dirtyFlag;

		spellCombo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(spellCombo));
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));

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
		this.setBorder(BorderFactory.createTitledBorder("Foe Attack Spells"));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(vec);
		spellCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<FoeAttack.FoeAttackSpell> getPercentageTable()
	{
		PercentageTable<FoeAttack.FoeAttackSpell> result = new PercentageTable<FoeAttack.FoeAttackSpell>();

		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			result.add(
				new FoeAttack.FoeAttackSpell(
					Database.getInstance().getSpell(dataModel.spells.get(i)),
					V1Dice.fromString(dataModel.castingLevels.get(i))),
				dataModel.percentages.get(i));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(PercentageTable<FoeAttack.FoeAttackSpell> pt)
	{
		dataModel.clear();

		if (pt != null)
		{
			List<Integer> percentages = pt.getPercentages();
			List<FoeAttack.FoeAttackSpell> items = pt.getItems();

			for (int i=0; i<percentages.size(); i++)
			{
				dataModel.add(percentages.get(i), items.get(i).getName(), items.get(i).getCastingLevel());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(0, (String)spellCombo.getItemAt(0), Dice.d1);
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
		List<String> spells = new ArrayList<String>();
		List<String> castingLevels = new ArrayList<String>();

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
				case 1: return "Spell";
				case 2: return "Casting Level";
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
				case 1: return spells.get(rowIndex);
				case 2: return castingLevels.get(rowIndex);
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
				case 1: spells.set(rowIndex, (String)aValue); break;
				case 2: castingLevels.set(rowIndex, (String)aValue); break;
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
			spells.clear();
			castingLevels.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int perc, String spell, Dice castingLevel)
		{
			percentages.add(perc);
			spells.add(spell);
			castingLevels.add(V1Dice.toString(castingLevel));
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			percentages.remove(index);
			spells.remove(index);
			fireTableDataChanged();
		}
	}
}

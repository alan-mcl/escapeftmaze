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
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.condition.RepeatedSpellEffect;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class RepeatedSpellEffectListPanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	private JTable table;
	private JButton add, remove;
	private MyTableModel dataModel;
	private JComboBox spellEffectCombo;

	/*-------------------------------------------------------------------------*/
	protected RepeatedSpellEffectListPanel(
		String title,
		int dirtyFlag,
		double horizScale,
		double vertScale)
	{
		this.dirtyFlag = dirtyFlag;

		spellEffectCombo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(spellEffectCombo));

		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		table.getColumnModel().getColumn(1).setPreferredWidth(10);
		table.getColumnModel().getColumn(2).setPreferredWidth(10);
		table.getColumnModel().getColumn(3).setPreferredWidth(10);
		Dimension d = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(
			new Dimension((int)(d.width*horizScale), (int)(d.height*vertScale)));

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
		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector vec = new Vector(Database.getInstance().getSpellEffects().keySet());
		Collections.sort(vec);
		spellEffectCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public List<RepeatedSpellEffect> getRepeatedSpellEffects()
	{
		List<RepeatedSpellEffect> result = new ArrayList<RepeatedSpellEffect>();

		for (int i=0; i<dataModel.percentages.size(); i++)
		{
			result.add(
				new RepeatedSpellEffect(
					dataModel.startTurns.get(i),
					dataModel.endTurns.get(i),
					dataModel.turnMods.get(i),
					dataModel.percentages.get(i),
					dataModel.spellEffects.get(i)));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<RepeatedSpellEffect> effects)
	{
		dataModel.clear();

		if (effects != null)
		{
			for (RepeatedSpellEffect rse : effects)
			{
				dataModel.add(
					rse.getStartTurn(),
					rse.getEndTurn(),
					rse.getTurnMod(),
					rse.getProbability(),
					rse.getSpellEffect());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(1,-1,1,100,
				(String)spellEffectCombo.getItemAt(0));
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
		List<Integer> startTurns = new ArrayList<Integer>();
		List<Integer> endTurns = new ArrayList<Integer>();
		List<Integer> turnMods = new ArrayList<Integer>();
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
				case 0: return "Start Turn";
				case 1: return "End Turn";
				case 2: return "Turn Mod";
				case 3: return "%";
				case 4: return "Spell Effect";
				default: throw new MazeException("Invalid column: "+column);
			}
		}

		/*----------------------------------------------------------------------*/
		public int getColumnCount()
		{
			return 5;
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
				case 0: return startTurns.get(rowIndex);
				case 1: return endTurns.get(rowIndex);
				case 2: return turnMods.get(rowIndex);
				case 3: return percentages.get(rowIndex);
				case 4: return spellEffects.get(rowIndex);
				default: throw new MazeException("Invalid column: "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			switch (columnIndex)
			{
				case 0: startTurns.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 1: endTurns.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 2: turnMods.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 3: percentages.set(rowIndex, Integer.parseInt((String)aValue)); break;
				case 4: spellEffects.set(rowIndex, (String)aValue); break;
				default: throw new MazeException("Invalid column: "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0:
				case 1:
				case 2:
				case 3: return Integer.TYPE;
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
			startTurns.clear();
			endTurns.clear();
			turnMods.clear();
			percentages.clear();
			spellEffects.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(int startTurn, int endTurn, int turnMod, int perc, String se)
		{
			startTurns.add(startTurn);
			endTurns.add(endTurn);
			turnMods.add(turnMod);
			percentages.add(perc);
			spellEffects.add(se);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			startTurns.remove(index);
			endTurns.remove(index);
			turnMods.remove(index);
			percentages.remove(index);
			spellEffects.remove(index);
			fireTableDataChanged();
		}
	}
}

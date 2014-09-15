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
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.ExperienceTable;
import mclachlan.maze.stat.ExperienceTableArray;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ExperienceTablePanel extends EditorPanel
{
	JSpinner postGygaxIncrement;
	JTable levels;
	JButton addLevel, deleteLevel;
	private ExperienceTableModel tableModel;

	/*-------------------------------------------------------------------------*/
	public ExperienceTablePanel()
	{
		super(SwingEditor.Tab.EXPERIENCE_TABLE);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		JPanel editControls = new JPanel(new GridBagLayout());

		postGygaxIncrement = new JSpinner(
			new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		postGygaxIncrement.addChangeListener(this);

		tableModel = new ExperienceTableModel(new ArrayList<Integer>());
		levels = new JTable(tableModel);
		levels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		levels.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		levels.setDefaultEditor(Integer.TYPE, new DefaultCellEditor(new JTextField()));
		levels.getColumnModel().getColumn(0).setPreferredWidth(10);
		JScrollPane scroller = new JScrollPane(levels);

		addLevel = new JButton("Add Level");
		addLevel.addActionListener(this);
		deleteLevel = new JButton("Delete Level");
		deleteLevel.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx++;
		editControls.add(new JLabel("Post Gygax Increment:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(postGygaxIncrement, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Levels:"), gbc);
		gbc.weightx = 0.0;
		gbc.gridx++;
		editControls.add(addLevel, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(deleteLevel, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=1;
		gbc.gridwidth = 3;
		editControls.add(scroller, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getExperienceTables().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		ExperienceTable g = new ExperienceTableArray(
			name,
			new int[]{0},
			0);
		Database.getInstance().getExperienceTables().put(name, g);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		ExperienceTable current = Database.getInstance().getExperienceTable((String)names.getSelectedValue());
		Database.getInstance().getExperienceTables().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getExperienceTables().put(current.getName(), current);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		ExperienceTableArray current = (ExperienceTableArray)
			Database.getInstance().getExperienceTable((String)names.getSelectedValue());

		ExperienceTable g = new ExperienceTableArray(
			newName,
			current.getLevels(),
			current.getPostGygaxIncrement());
		Database.getInstance().getExperienceTables().put(newName, g);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		String name = (String)names.getSelectedValue();
		Database.getInstance().getExperienceTables().remove(name);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		ExperienceTable et = Database.getInstance().getExperienceTable(name);

		ExperienceTableArray eta = (ExperienceTableArray)et;

		postGygaxIncrement.removeChangeListener(this);
		postGygaxIncrement.setValue(eta.getPostGygaxIncrement());
		postGygaxIncrement.addChangeListener(this);

		tableModel.clear();
		for (int i = 0; i < eta.getLevels().length; i++)
		{
			tableModel.add(eta.getLevels()[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == addLevel)
		{
			tableModel.add(0);
		}
		else if (e.getSource() == deleteLevel)
		{
			tableModel.remove();
		}
		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		if (!Database.getInstance().getExperienceTables().containsKey(name))
		{
			return;
		}

		ExperienceTableArray eta = (ExperienceTableArray)
			Database.getInstance().getExperienceTable(name);

		eta.setPostGygaxIncrement((Integer)postGygaxIncrement.getValue());

		int[] levels = new int[this.tableModel.levelList.size()];
		for (int i = 0; i < levels.length; i++)
		{
			levels[i] = this.tableModel.levelList.get(i);
		}

		eta.setLevels(levels);
	}

	/*-------------------------------------------------------------------------*/
	static class ExperienceTableModel extends AbstractTableModel
	{
		java.util.List<Integer> levelList;

		public ExperienceTableModel(java.util.List<Integer> levels)
		{
			this.levelList = levels;
		}

		public Class<?> getColumnClass(int columnIndex)
		{
			return Integer.TYPE;
		}

		public int getColumnCount()
		{
			return 2;
		}

		public String getColumnName(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return "Level";
				case 1: return "Experience";
				default: throw new MazeException("invalid column "+columnIndex);
			}
		}

		public int getRowCount()
		{
			return levelList.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return rowIndex;
				case 1: return levelList.get(rowIndex);
				default: throw new MazeException("invalid column "+columnIndex);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex==1;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			SwingEditor.instance.setDirty(SwingEditor.Tab.EXPERIENCE_TABLE);
			switch (columnIndex)
			{
				case 0: throw new MazeException("invalid column "+columnIndex);
				case 1:
					try
					{
						levelList.set(rowIndex, Integer.parseInt((String)aValue));
						fireTableCellUpdated(rowIndex, columnIndex);
					}
					catch (NumberFormatException e)
					{
						// do nothing
					}
					return;
				default: throw new MazeException("invalid column "+columnIndex);
			}
		}

		public void add(int xp)
		{
			this.levelList.add(xp);
			fireTableDataChanged();
		}

		public void remove()
		{
			if (levelList.isEmpty())
			{
				return;
			}
			int i = this.levelList.size()-1;
			this.levelList.remove(i);
			fireTableDataChanged();
		}

		public void clear()
		{
			this.levelList.clear();
			fireTableDataChanged();
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("test");
		frame.add(new JTable(5,5));
		frame.pack();
		frame.setVisible(true);
	}
}

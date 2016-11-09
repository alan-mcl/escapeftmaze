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

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import mclachlan.maze.data.Database;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.PlayerSpellBook;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 */
public class PlayerSpellBooksPanel extends EditorPanel
{
	private JTable table;
	private MyTableModel tableModel;
	private JTextArea description;
	private JButton add, remove, sort;
	private JComboBox spellCombo;

	/*-------------------------------------------------------------------------*/
	public PlayerSpellBooksPanel()
	{
		super(SwingEditor.Tab.PLAYER_SPELL_BOOKS);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		result.add(new JLabel("Adding, renaming or deleting player spell " +
			"books would be a bad idea."), gbc);

		description = new JTextArea(7, 35);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.addKeyListener(this);
		gbc.gridy++;
		JScrollPane scroller = new JScrollPane(description);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		result.add(scroller, gbc);

		spellCombo = new JComboBox();
		tableModel = new MyTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(spellCombo));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(1).setMaxWidth(100);

		gbc.gridy++;
		result.add(new JScrollPane(table), gbc);

		JPanel buttons = new JPanel();
		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		sort = new JButton("Sort");
		sort.addActionListener(this);

		buttons.add(add);
		buttons.add(remove);
		buttons.add(sort);

		gbc.gridy++;
		gbc.weighty = 1.0;
		result.add(buttons, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getPlayerSpellBooks().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector vec = new Vector(Database.getInstance().getSpellList());
		Collections.sort(vec);
		spellCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		tableModel.clear();
		PlayerSpellBook psb = Database.getInstance().getPlayerSpellBook(name);

		if (psb.getSpellNames() != null)
		{
			for (String s : psb.getSpellNames())
			{
				tableModel.add(s);
			}
			tableModel.sort();
		}

		description.setText(psb.getDescription());
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		// do not allow
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		// do not allow
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		// do not allow
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		// do not allow
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		PlayerSpellBook psb = Database.getInstance().getPlayerSpellBook(name);
		psb.setSpellNames(new HashSet<String>(tableModel.spellNames));
		psb.setDescription(description.getText());
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			tableModel.add((String)spellCombo.getItemAt(0));
		}
		else if (e.getSource() == remove)
		{
			tableModel.remove(table.getSelectedRow());
		}
		else if (e.getSource() == sort)
		{
			tableModel.sort();
		}

		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/
	static class MyTableModel extends AbstractTableModel
	{
		List<String> spellNames = new ArrayList<String>();

		public int getColumnCount()
		{
			return 2;
		}

		public int getRowCount()
		{
			return spellNames.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch(columnIndex)
			{
				case 0: return spellNames.get(rowIndex);
				case 1: return Database.getInstance().getSpell(spellNames.get(rowIndex)).getLevel();
				default: throw new MazeException("Invalid column "+columnIndex);
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (columnIndex != 0)
			{
				throw new MazeException("Invalid column "+columnIndex);
			}

			spellNames.set(rowIndex, (String)aValue);
		}

		public String getColumnName(int columnIndex)
		{
			switch(columnIndex)
			{
				case 0: return "Spell";
				case 1: return "Level";
				default: throw new MazeException("Invalid column "+columnIndex);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex != 1;
		}

		void clear()
		{
			spellNames.clear();
			fireTableDataChanged();
		}

		void add(String spellName)
		{
			spellNames.add(spellName);
			fireTableDataChanged();
		}

		void remove(int index)
		{
			spellNames.remove(index);
			fireTableDataChanged();
		}

		void sort()
		{
			Collections.sort(spellNames, comparator);
			fireTableDataChanged();
		}
	}

	static PlayerSpellBookComparator comparator = new PlayerSpellBookComparator();

	/*-------------------------------------------------------------------------*/
	static class PlayerSpellBookComparator implements Comparator<String>
	{
		public int compare(String spellName1, String spellName2)
		{
			// sort first on spell level, then on name

			Spell spell1 = Database.getInstance().getSpell(spellName1);
			Spell spell2 = Database.getInstance().getSpell(spellName2);

			if (spell1.getLevel() != spell2.getLevel())
			{
				return spell1.getLevel() - spell2.getLevel();
			}
			else
			{
				return spell1.getName().compareTo(spell2.getName());
			}
		}
	}
}

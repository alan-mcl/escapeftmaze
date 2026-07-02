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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.journal.Journal;
import mclachlan.maze.game.journal.JournalEntry;
import mclachlan.maze.game.journal.JournalManager;

/**
 * Editor for save-game journal files.
 */
public class SaveGameJournalsPanel extends JPanel
	implements ActionListener, ListSelectionListener, TableModelListener
{
	private JComboBox<JournalManager.JournalType> journalType;
	private JList<String> sectionKeys;
	private String currentSectionKey;
	private JTable entriesTable;
	private EntriesTableModel entriesModel;
	private Map<JournalManager.JournalType, Journal> journals;

	/*-------------------------------------------------------------------------*/
	public SaveGameJournalsPanel()
	{
		journalType = new JComboBox<>(JournalManager.JournalType.values());
		journalType.addActionListener(this);

		sectionKeys = new JList<>();
		sectionKeys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sectionKeys.addListSelectionListener(this);

		entriesModel = new EntriesTableModel();
		entriesTable = new JTable(entriesModel);
		entriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entriesModel.addTableModelListener(this);

		JButton addSection = new JButton("Add Section");
		addSection.setActionCommand("addSection");
		addSection.addActionListener(this);
		JButton deleteSection = new JButton("Delete Section");
		deleteSection.setActionCommand("deleteSection");
		deleteSection.addActionListener(this);
		JButton addEntry = new JButton("Add Entry");
		addEntry.setActionCommand("addEntry");
		addEntry.addActionListener(this);
		JButton deleteEntry = new JButton("Delete Entry");
		deleteEntry.setActionCommand("deleteEntry");
		deleteEntry.addActionListener(this);

		JPanel sectionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sectionButtons.add(addSection);
		sectionButtons.add(deleteSection);

		JPanel entryButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		entryButtons.add(addEntry);
		entryButtons.add(deleteEntry);

		JPanel left = new JPanel(new BorderLayout(3, 3));
		JPanel journalHeader = new JPanel();
		journalHeader.setLayout(new BoxLayout(journalHeader, BoxLayout.Y_AXIS));
		journalHeader.add(new JLabel("Journal:"));
		journalHeader.add(journalType);
		left.add(journalHeader, BorderLayout.NORTH);
		JPanel sections = new JPanel(new BorderLayout(3, 3));
		sections.add(new JLabel("Section Key:"), BorderLayout.NORTH);
		sections.add(new JScrollPane(sectionKeys), BorderLayout.CENTER);
		sections.add(sectionButtons, BorderLayout.SOUTH);
		left.add(sections, BorderLayout.CENTER);

		JPanel right = new JPanel(new BorderLayout(3, 3));
		right.add(entryButtons, BorderLayout.NORTH);
		right.add(new JScrollPane(entriesTable), BorderLayout.CENTER);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.3;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(left, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.7;
		add(right, gbc);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Loader loader, String saveGameName) throws Exception
	{
		journals = new EnumMap<>(JournalManager.JournalType.class);
		for (JournalManager.JournalType type : JournalManager.JournalType.values())
		{
			Journal journal = loader.loadJournal(saveGameName, type.getJournalName());
			if (journal == null)
			{
				journal = new Journal(type.getJournalName());
			}
			journals.put(type, journal);
		}
		refreshSectionKeys(null);
	}

	/*-------------------------------------------------------------------------*/
	public void save(String saveGameName, Saver saver) throws Exception
	{
		commitCurrentSection();
		for (JournalManager.JournalType type : JournalManager.JournalType.values())
		{
			saver.saveJournal(saveGameName, journals.get(type));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String ignored)
	{
		commitCurrentSection();
	}

	/*-------------------------------------------------------------------------*/
	private Journal getCurrentJournal()
	{
		return journals.get((JournalManager.JournalType)journalType.getSelectedItem());
	}

	/*-------------------------------------------------------------------------*/
	private void refreshSectionKeys(String toBeSelected)
	{
		currentSectionKey = null;
		sectionKeys.removeListSelectionListener(this);

		Journal journal = getCurrentJournal();
		if (journal.getContents() == null)
		{
			journal.setContents(new HashMap<>());
		}
		Vector<String> keys = new Vector<>(journal.getContents().keySet());
		Collections.sort(keys);
		sectionKeys.setListData(keys);
		if (toBeSelected != null)
		{
			sectionKeys.setSelectedValue(toBeSelected, true);
		}
		else if (!keys.isEmpty())
		{
			sectionKeys.setSelectedIndex(0);
		}
		currentSectionKey = sectionKeys.getSelectedValue();
		if (currentSectionKey != null)
		{
			refreshEntries(currentSectionKey);
		}
		else
		{
			entriesModel.setEntries(new ArrayList<>());
		}

		sectionKeys.addListSelectionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void refreshEntries(String sectionKey)
	{
		Journal journal = getCurrentJournal();
		List<JournalEntry> entries = journal.getContents().get(sectionKey);
		entriesModel.setEntries(entries == null ? new ArrayList<>() : entries);
	}

	/*-------------------------------------------------------------------------*/
	private void commitCurrentSection()
	{
		if (currentSectionKey == null || journals == null)
		{
			return;
		}

		Journal journal = getCurrentJournal();
		List<JournalEntry> entries = entriesModel.getEntriesCopy();
		journal.getContents().put(currentSectionKey, entries);
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == journalType)
		{
			commitCurrentSection();
			refreshSectionKeys(null);
			return;
		}

		markDirty();
		commitCurrentSection();

		if ("addSection".equals(e.getActionCommand()))
		{
			String key = JOptionPane.showInputDialog(this, "Section key:");
			if (key == null || key.trim().isEmpty())
			{
				return;
			}
			key = key.trim();
			Journal journal = getCurrentJournal();
			if (journal.getContents().containsKey(key))
			{
				JOptionPane.showMessageDialog(this, "Section already exists.");
				return;
			}
			journal.getContents().put(key, new ArrayList<>());
			refreshSectionKeys(key);
		}
		else if ("deleteSection".equals(e.getActionCommand()))
		{
			if (currentSectionKey == null)
			{
				return;
			}
			getCurrentJournal().getContents().remove(currentSectionKey);
			refreshSectionKeys(null);
		}
		else if ("addEntry".equals(e.getActionCommand()))
		{
			if (currentSectionKey == null)
			{
				return;
			}
			entriesModel.addEntry(new JournalEntry(0, ""));
		}
		else if ("deleteEntry".equals(e.getActionCommand()))
		{
			int row = entriesTable.getSelectedRow();
			if (row >= 0)
			{
				entriesModel.removeEntry(row);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
		{
			return;
		}

		commitCurrentSection();
		currentSectionKey = sectionKeys.getSelectedValue();
		if (currentSectionKey != null)
		{
			refreshEntries(currentSectionKey);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		markDirty();
	}

	/*-------------------------------------------------------------------------*/
	static class EntriesTableModel extends AbstractTableModel
	{
		private List<JournalEntry> entries = new ArrayList<>();

		public void setEntries(List<JournalEntry> entries)
		{
			this.entries = entries;
			fireTableDataChanged();
		}

		public List<JournalEntry> getEntriesCopy()
		{
			return new ArrayList<>(entries);
		}

		public void addEntry(JournalEntry entry)
		{
			entries.add(entry);
			fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
		}

		public void removeEntry(int row)
		{
			entries.remove(row);
			fireTableRowsDeleted(row, row);
		}

		@Override
		public int getRowCount()
		{
			return entries.size();
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public String getColumnName(int column)
		{
			return column == 0 ? "Turn" : "Text";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return columnIndex == 0 ? Long.class : String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			JournalEntry entry = entries.get(rowIndex);
			return columnIndex == 0 ? entry.getTurnNr() : entry.getText();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			JournalEntry entry = entries.get(rowIndex);
			if (columnIndex == 0)
			{
				entry.setTurnNr(aValue instanceof Number ?
					((Number)aValue).longValue() : Long.parseLong(String.valueOf(aValue)));
			}
			else
			{
				entry.setText(aValue == null ? "" : String.valueOf(aValue));
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;

/**
 *
 */
public class LootTableDisplayWidget extends JPanel
	implements ActionListener
{
	private int dirtyFlag;
	private JComboBox lootTable;
	private JTextArea display;

	/*-------------------------------------------------------------------------*/
	protected LootTableDisplayWidget(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		this.setLayout(new BorderLayout(3,3));

		lootTable = new JComboBox();
		lootTable.addActionListener(this);

		JPanel top = new JPanel();
		top.add(lootTable);

		display = new JTextArea(3,30);
		display.setEditable(false);
		display.setWrapStyleWord(true);
		display.setLineWrap(true);

		this.add(top, BorderLayout.NORTH);
		this.add(display, BorderLayout.CENTER);

		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> loot = new Vector<String>(Database.getInstance().getLootTables().keySet());
		Collections.sort(loot);
		lootTable.setModel(new DefaultComboBoxModel(loot));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(LootTable table)
	{
		this.lootTable.removeActionListener(this);

		this.lootTable.setSelectedItem(table.getName());

		StringBuilder sb = new StringBuilder();
		for (ILootEntry le : table.getLootEntries().getPossibilities())
		{
			sb.append(le.getName()).append(", ");
		}
		display.setText(sb.toString());

		this.lootTable.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public String getDefault()
	{
		return (String)lootTable.getItemAt(0);
	}

	/*-------------------------------------------------------------------------*/
	public String getSelectedLootTable()
	{
		return (String)lootTable.getSelectedItem();
	}
}

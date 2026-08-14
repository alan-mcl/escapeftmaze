/*
 * Copyright (c) 2026 Alan McLachlan
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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Simple key/value editor for zone (or other) string metadata maps.
 */
public class StringMapTablePanel extends JPanel
{
	private final int dirtyFlag;
	private final DefaultTableModel model;
	private final JTable table;

	/*-------------------------------------------------------------------------*/
	public StringMapTablePanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		model = new DefaultTableModel(new String[]{"Key", "Value"}, 0);
		table = new JTable(model);
		table.getModel().addTableModelListener(e -> SwingEditor.instance.setDirty(dirtyFlag));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JScrollPane(table));
		JPanel buttons = new JPanel();
		JButton add = new JButton("Add");
		add.addActionListener(e ->
		{
			model.addRow(new String[]{"", ""});
			SwingEditor.instance.setDirty(dirtyFlag);
		});
		JButton remove = new JButton("Remove");
		remove.addActionListener(e ->
		{
			int[] rows = table.getSelectedRows();
			for (int i = rows.length - 1; i >= 0; i--)
			{
				model.removeRow(rows[i]);
			}
			SwingEditor.instance.setDirty(dirtyFlag);
		});
		buttons.add(add);
		buttons.add(remove);
		add(buttons);
		add(new JLabel(
			"Example temple fragment keys: fragment=true, fragment.role, "
				+ "fragment.depthMin, fragment.depthMax, fragment.weight, "
				+ "fragment.maxPerFloor"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Map<String, String> metadata)
	{
		model.setRowCount(0);
		if (metadata == null || metadata.isEmpty())
		{
			return;
		}

		for (Map.Entry<String, String> entry : metadata.entrySet())
		{
			model.addRow(new String[]{entry.getKey(), entry.getValue()});
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, String> getMetadata()
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (int row = 0; row < model.getRowCount(); row++)
		{
			Object keyObj = model.getValueAt(row, 0);
			Object valObj = model.getValueAt(row, 1);
			String key = keyObj == null ? "" : keyObj.toString().trim();
			if (key.isEmpty())
			{
				continue;
			}
			String value = valObj == null ? "" : valObj.toString();
			result.put(key, value);
		}
		return result;
	}
}

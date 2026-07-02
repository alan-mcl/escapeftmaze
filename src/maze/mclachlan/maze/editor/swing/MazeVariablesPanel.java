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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import mclachlan.maze.util.MazeException;

/**
 * Editor for save-game maze variables (quest flags, portal states, etc.).
 */
public class MazeVariablesPanel extends JPanel
	implements ActionListener, ListSelectionListener, KeyListener
{
	private JList<String> names;
	private String currentName;
	private JTextField filter;
	private JTextField key;
	private JTextField value;
	private Map<String, String> map;
	private Vector<String> filteredNames;

	/*-------------------------------------------------------------------------*/
	public MazeVariablesPanel()
	{
		filteredNames = new Vector<>();

		names = new JList<>(filteredNames);
		names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		names.addListSelectionListener(this);
		names.setFixedCellWidth(150);
		JScrollPane nameScroller = new JScrollPane(names);

		JPanel detail = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		detail.add(new JLabel(
			"Quest and NPC progress (e.g. *.quest.manager.state) is stored here."), gbc);

		gbc.gridy++;
		detail.add(new JLabel("Filter:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		filter = new JTextField(20);
		filter.addKeyListener(this);
		detail.add(filter, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		detail.add(new JLabel("Key:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		key = new JTextField(20);
		key.addKeyListener(this);
		detail.add(key, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		detail.add(new JLabel("Value:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		value = new JTextField(20);
		value.addKeyListener(this);
		detail.add(value, gbc);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton add = new JButton("Add");
		add.addActionListener(this);
		add.setActionCommand("add");
		JButton delete = new JButton("Delete");
		delete.addActionListener(this);
		delete.setActionCommand("delete");
		buttons.add(add);
		buttons.add(delete);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		detail.add(buttons, gbc);

		JPanel detailWrapper = new JPanel(new BorderLayout());
		detailWrapper.add(detail, BorderLayout.NORTH);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			true,
			nameScroller,
			new JScrollPane(detailWrapper));
		add(splitPane, gbc);
		splitPane.setDividerLocation(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Map<String, String> vars)
	{
		map = vars == null ? new HashMap<>() : vars;
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, String> getMazeVariables()
	{
		return map;
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		if (map == null || currentName == null)
		{
			return;
		}

		String newKey = key.getText().trim();
		String newValue = value.getText();

		if (newKey.isEmpty())
		{
			return;
		}
		if (newKey.contains(" "))
		{
			throw new MazeException("Invalid maze variable name: [" + newKey + "]");
		}

		if (!newKey.equals(currentName))
		{
			map.remove(currentName);
		}
		map.put(newKey, newValue);
		currentName = newKey;
	}

	/*-------------------------------------------------------------------------*/
	private void refreshNames(String toBeSelected)
	{
		currentName = null;
		names.removeListSelectionListener(this);

		filteredNames.clear();
		String filterText = filter == null ? "" : filter.getText().trim().toLowerCase();
		Vector<String> all = new Vector<>(map.keySet());
		Collections.sort(all);
		for (String s : all)
		{
			if (filterText.isEmpty() || s.toLowerCase().contains(filterText))
			{
				filteredNames.add(s);
			}
		}
		names.setListData(filteredNames);

		if (toBeSelected != null && filteredNames.contains(toBeSelected))
		{
			names.setSelectedValue(toBeSelected, true);
		}
		else if (!filteredNames.isEmpty())
		{
			names.setSelectedIndex(0);
		}
		currentName = names.getSelectedValue();
		if (currentName != null)
		{
			refreshDetail(currentName);
		}
		else
		{
			key.setText("");
			value.setText("");
		}

		names.addListSelectionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void refreshDetail(String name)
	{
		key.removeKeyListener(this);
		value.removeKeyListener(this);
		key.setText(name);
		value.setText(map.get(name));
		key.addKeyListener(this);
		value.addKeyListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ("add".equals(e.getActionCommand()))
		{
			String newKey = JOptionPane.showInputDialog(this, "New maze variable key:");
			if (newKey == null)
			{
				return;
			}
			newKey = newKey.trim();
			if (newKey.isEmpty() || newKey.contains(" "))
			{
				JOptionPane.showMessageDialog(this,
					"Key must be non-empty and contain no whitespace.",
					"Invalid key",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (map.containsKey(newKey))
			{
				JOptionPane.showMessageDialog(this,
					"Key already exists.",
					"Invalid key",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			map.put(newKey, "");
			markDirty();
			refreshNames(newKey);
		}
		else if ("delete".equals(e.getActionCommand()))
		{
			if (currentName == null)
			{
				return;
			}
			map.remove(currentName);
			markDirty();
			refreshNames(null);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
		{
			return;
		}

		if (currentName != null)
		{
			commit(currentName);
		}

		currentName = names.getSelectedValue();
		if (currentName != null)
		{
			refreshDetail(currentName);
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		markDirty();
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (e.getSource() == filter && (e.getKeyCode() == KeyEvent.VK_ENTER ||
			e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isLetterOrDigit(e.getKeyChar())))
		{
			String selected = currentName;
			if (selected != null)
			{
				commit(selected);
			}
			refreshNames(selected);
		}
	}
}

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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.TextRepository;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.util.MazeException;

/**
 * Editor for HotString bundles (strings-*.json).
 */
public class HotStringsPanel extends JPanel implements IEditorPanel, ListSelectionListener, KeyListener
{
	private final int dirtyFlag;
	private JComboBox<String> namespaceBox;
	private JList<String> keys;
	private JTextField keyField;
	private JTextArea valueArea;
	private String currentKey;
	private Map<String, String> currentBundle = new LinkedHashMap<>();

	public HotStringsPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		namespaceBox = new JComboBox<>(TextRepository.getHotNamespaces().toArray(new String[0]));
		namespaceBox.addActionListener(e -> loadNamespace());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(new JLabel("Namespace:"));
		top.add(namespaceBox);

		keys = new JList<>();
		keys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		keys.addListSelectionListener(this);
		JScrollPane keyScroller = new JScrollPane(keys);
		keyScroller.setPreferredSize(new Dimension(180, 400));

		keyField = new JTextField(40);
		keyField.addKeyListener(this);
		valueArea = new JTextArea(20, 60);
		valueArea.setLineWrap(true);
		valueArea.setWrapStyleWord(true);
		valueArea.addKeyListener(this);

		JPanel edit = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		edit.add(new JLabel("Key:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		edit.add(keyField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		edit.add(new JLabel("Value:"), gbc);
		gbc.gridx = 1;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		edit.add(new JScrollPane(valueArea), gbc);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton add = new JButton("Add");
		add.addActionListener(e -> addKey());
		JButton delete = new JButton("Delete");
		delete.addActionListener(e -> deleteKey());
		buttons.add(add);
		buttons.add(delete);

		JPanel right = new JPanel(new BorderLayout());
		right.add(edit, BorderLayout.CENTER);
		right.add(buttons, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, keyScroller, right);
		add(top, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);

		loadNamespace();
	}

	private Campaign getCampaign()
	{
		try
		{
			return Database.getCampaigns().get(SwingEditor.instance.getCurrentCampaign());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	private void loadNamespace()
	{
		commitCurrentKey();
		String namespace = (String)namespaceBox.getSelectedItem();
		Map<String, String> loaded = Database.getInstance()
			.getTextRepository(getCampaign())
			.getHotNamespace(namespace);
		currentBundle = loaded != null ? new LinkedHashMap<>(loaded) : new LinkedHashMap<>();
		refreshKeyList(null);
	}

	private void refreshKeyList(String selectKey)
	{
		Vector<String> list = new Vector<>(currentBundle.keySet());
		list.sort(String::compareTo);
		keys.setListData(list);
		if (selectKey != null)
		{
			keys.setSelectedValue(selectKey, true);
		}
		else if (!list.isEmpty())
		{
			keys.setSelectedIndex(0);
		}
	}

	private void commitCurrentKey()
	{
		if (currentKey != null && currentBundle.containsKey(currentKey))
		{
			currentBundle.put(currentKey, valueArea.getText());
		}
	}

	private void addKey()
	{
		String key = JOptionPane.showInputDialog(this, "New key:");
		if (key == null || key.isBlank())
		{
			return;
		}
		currentBundle.put(key.trim(), "");
		SwingEditor.instance.setDirty(dirtyFlag);
		refreshKeyList(key.trim());
	}

	private void deleteKey()
	{
		if (currentKey == null)
		{
			return;
		}
		currentBundle.remove(currentKey);
		currentKey = null;
		SwingEditor.instance.setDirty(dirtyFlag);
		refreshKeyList(null);
	}

	public void saveToDisk() throws Exception
	{
		commitCurrentKey();
		String namespace = (String)namespaceBox.getSelectedItem();
		Database.getInstance().saveHotStrings(namespace, currentBundle, getCampaign());
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
		{
			return;
		}
		commitCurrentKey();
		currentKey = keys.getSelectedValue();
		if (currentKey == null)
		{
			keyField.setText("");
			valueArea.setText("");
			return;
		}
		keyField.setText(currentKey);
		valueArea.setText(currentBundle.get(currentKey));
		valueArea.setCaretPosition(0);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public Vector<DataObject> loadData()
	{
		return new Vector<>();
	}

	@Override
	public void refresh(String name)
	{
		loadNamespace();
		refreshKeyList(name);
	}

	@Override
	public DataObject newItem(String name)
	{
		throw new MazeException("Use Add on Hot Strings panel");
	}

	@Override
	public void renameItem(String newName)
	{
	}

	@Override
	public DataObject copyItem(String newName)
	{
		throw new MazeException("Not supported");
	}

	@Override
	public void deleteItem()
	{
		deleteKey();
	}

	@Override
	public DataObject commit(String name)
	{
		commitCurrentKey();
		return null;
	}

	@Override
	public String getCurrentName()
	{
		return currentKey;
	}

	@Override
	public void refreshNames(String name)
	{
		refreshKeyList(name);
	}

	@Override
	public int getDirtyFlag()
	{
		return dirtyFlag;
	}

	@Override
	public void initForeignKeys()
	{
	}

	@Override
	public void reload()
	{
		loadNamespace();
	}
}

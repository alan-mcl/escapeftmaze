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
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.ColdString;
import mclachlan.maze.data.ColdStringManifestEntry;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.TextRepository;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.util.MazeException;

/**
 * Editor for ColdStrings shards (strings/cold/*.json).
 */
public class ColdStringsPanel extends EditorPanel
{
	private JComboBox<String> shardBox;
	private JTextArea body;
	private Map<String, ColdString> currentShard;
	private String currentShardName;
	private List<ColdStringManifestEntry> manifestEntries;
	private Set<String> deletedShards;
	private Map<String, Map<String, ColdString>> shardCache;

	public ColdStringsPanel()
	{
		super(SwingEditor.Tab.COLD_STRINGS);
	}

	@Override
	protected boolean scrollEditControls()
	{
		return false;
	}

	@Override
	public Container getEditControls()
	{
		JPanel panel = new JPanel(new BorderLayout(5, 5));

		shardBox = new JComboBox<>();
		shardBox.addActionListener(e ->
		{
			if (shardBox.getSelectedItem() != null)
			{
				loadShard();
			}
		});

		JButton addShard = new JButton("Add Shard");
		addShard.addActionListener(e -> addShard());
		JButton deleteShard = new JButton("Delete Shard");
		deleteShard.addActionListener(e -> deleteShard());

		JPanel shardBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		shardBar.add(new JLabel("Shard:"));
		shardBar.add(shardBox);
		shardBar.add(addShard);
		shardBar.add(deleteShard);

		body = new JTextArea();
		body.setLineWrap(true);
		body.setWrapStyleWord(true);
		body.addKeyListener(this);

		JButton addKey = new JButton("Add Key");
		addKey.addActionListener(e -> addKey());
		JButton deleteKey = new JButton("Delete Key");
		deleteKey.addActionListener(e -> deleteKey());

		JPanel keyBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		keyBar.add(addKey);
		keyBar.add(deleteKey);

		panel.add(shardBar, BorderLayout.NORTH);
		panel.add(new JScrollPane(body), BorderLayout.CENTER);
		panel.add(keyBar, BorderLayout.SOUTH);

		return panel;
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

	private void ensureState()
	{
		if (manifestEntries == null)
		{
			manifestEntries = new ArrayList<>();
		}
		if (deletedShards == null)
		{
			deletedShards = new LinkedHashSet<>();
		}
		if (shardCache == null)
		{
			shardCache = new LinkedHashMap<>();
		}
	}

	private void loadManifestFromDisk()
	{
		ensureState();
		manifestEntries = new ArrayList<>(
			Database.getInstance().getTextRepository(getCampaign()).getColdManifest());
		deletedShards.clear();
		shardCache.clear();
	}

	private void initShardList()
	{
		String previous = currentShardName;
		shardBox.removeAllItems();
		for (ColdStringManifestEntry entry : manifestEntries)
		{
			shardBox.addItem(entry.getShard());
		}
		if (shardBox.getItemCount() == 0)
		{
			currentShardName = null;
			currentShard = new LinkedHashMap<>();
			refreshNames(null);
			return;
		}
		if (previous != null)
		{
			for (int i = 0; i < shardBox.getItemCount(); i++)
			{
				if (previous.equals(shardBox.getItemAt(i)))
				{
					shardBox.setSelectedIndex(i);
					loadShard();
					return;
				}
			}
		}
		if (shardBox.getItemCount() > 0)
		{
			shardBox.setSelectedIndex(0);
			loadShard();
		}
	}

	private void loadShard()
	{
		ensureState();
		if (currentName != null)
		{
			commit(currentName);
		}
		if (currentShardName != null && currentShard != null)
		{
			shardCache.put(currentShardName, currentShard);
		}

		currentShardName = (String)shardBox.getSelectedItem();
		if (currentShardName == null)
		{
			currentShard = new LinkedHashMap<>();
			refreshNames(null);
			return;
		}

		Map<String, ColdString> cached = shardCache.get(currentShardName);
		if (cached != null)
		{
			currentShard = new LinkedHashMap<>(cached);
		}
		else
		{
			Map<String, ColdString> loaded = Database.getInstance()
				.getTextRepository(getCampaign())
				.getColdShard(currentShardName);
			currentShard = loaded != null ? new LinkedHashMap<>(loaded) : new LinkedHashMap<>();
		}
		refreshNames(null);
	}

	private void addShard()
	{
		ensureState();
		String shard = JOptionPane.showInputDialog(this, "Shard name (file name without .json):", "New Shard",
			JOptionPane.QUESTION_MESSAGE);
		if (shard == null || shard.isBlank())
		{
			return;
		}
		shard = shard.trim();
		String prefix = JOptionPane.showInputDialog(this,
			"Key prefix (e.g. ichiba.library.ref.):", shard + ".");
		if (prefix == null || prefix.isBlank())
		{
			return;
		}
		prefix = prefix.trim();
		if (!prefix.endsWith("."))
		{
			prefix = prefix + ".";
		}

		for (ColdStringManifestEntry entry : manifestEntries)
		{
			if (entry.getShard().equals(shard))
			{
				JOptionPane.showMessageDialog(this, "Shard already exists.", "Add Shard", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		manifestEntries.add(new ColdStringManifestEntry(prefix, shard));
		currentShard = new LinkedHashMap<>();
		currentShardName = shard;
		initShardList();
		shardBox.setSelectedItem(shard);
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	private void deleteShard()
	{
		if (currentShardName == null)
		{
			return;
		}
		int option = JOptionPane.showConfirmDialog(this,
			"Delete shard [" + currentShardName + "] and all its keys?",
			"Delete Shard", JOptionPane.YES_NO_OPTION);
		if (option != JOptionPane.YES_OPTION)
		{
			return;
		}

		String removing = currentShardName;
		manifestEntries.removeIf(e -> e.getShard().equals(removing));
		deletedShards.add(removing);
		shardCache.remove(removing);
		currentShardName = null;
		currentShard = new LinkedHashMap<>();
		initShardList();
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	private void addKey()
	{
		if (currentShardName == null)
		{
			JOptionPane.showMessageDialog(this, "Add a shard first.", "Add Key", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String name = JOptionPane.showInputDialog(this, "Cold string key:", "New Key", JOptionPane.QUESTION_MESSAGE);
		if (name == null || name.isBlank())
		{
			return;
		}
		name = name.trim();
		if (currentShard == null)
		{
			currentShard = new LinkedHashMap<>();
		}
		if (currentShard.containsKey(name))
		{
			JOptionPane.showMessageDialog(this, "Key already exists.", "Add Key", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ColdString coldString = new ColdString(name, "");
		coldString.setCampaign(SwingEditor.instance.getCurrentCampaign());
		currentShard.put(name, coldString);
		refreshNames(name);
		refresh(name);
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	private void deleteKey()
	{
		if (currentName == null)
		{
			return;
		}
		int option = JOptionPane.showConfirmDialog(this,
			"Delete key [" + currentName + "]?", "Delete Key", JOptionPane.YES_NO_OPTION);
		if (option != JOptionPane.YES_OPTION)
		{
			return;
		}
		deleteItem();
		refreshNames(null);
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	@Override
	public Vector<DataObject> loadData()
	{
		Vector<DataObject> result = new Vector<>();
		if (currentShard != null)
		{
			result.addAll(currentShard.values());
		}
		return result;
	}

	@Override
	public void refresh(String name)
	{
		if (body == null || currentShard == null)
		{
			return;
		}
		ColdString coldString = currentShard.get(name);
		if (coldString == null)
		{
			body.setText("");
			return;
		}
		body.setText(coldString.getBody());
		body.setCaretPosition(0);
	}

	@Override
	public DataObject newItem(String name)
	{
		if (currentShard == null)
		{
			currentShard = new LinkedHashMap<>();
		}
		ColdString coldString = new ColdString(name, "");
		coldString.setCampaign(SwingEditor.instance.getCurrentCampaign());
		currentShard.put(name, coldString);
		return coldString;
	}

	@Override
	public void renameItem(String newName)
	{
		if (currentShard == null)
		{
			return;
		}
		ColdString existing = currentShard.remove(currentName);
		if (existing != null)
		{
			existing.setName(newName);
			currentShard.put(newName, existing);
		}
	}

	@Override
	public DataObject copyItem(String newName)
	{
		if (currentShard == null)
		{
			throw new MazeException("No shard selected");
		}
		ColdString source = currentShard.get(currentName);
		if (source == null)
		{
			throw new MazeException("Nothing selected");
		}
		ColdString copy = new ColdString(newName, source.getBody());
		copy.setCampaign(SwingEditor.instance.getCurrentCampaign());
		currentShard.put(newName, copy);
		return copy;
	}

	@Override
	public void deleteItem()
	{
		if (currentShard != null && currentName != null)
		{
			currentShard.remove(currentName);
		}
	}

	@Override
	public DataObject commit(String name)
	{
		if (currentShard == null)
		{
			return null;
		}
		ColdString coldString = currentShard.get(name);
		if (coldString == null)
		{
			return null;
		}
		coldString.setBody(body.getText());
		return coldString;
	}

	@Override
	public void initForeignKeys()
	{
		loadManifestFromDisk();
		initShardList();
	}

	@Override
	public void reload()
	{
		loadManifestFromDisk();
		initShardList();
	}

	public void saveToDisk() throws Exception
	{
		ensureState();
		if (currentName != null)
		{
			commit(currentName);
		}
		if (currentShardName != null && currentShard != null)
		{
			shardCache.put(currentShardName, currentShard);
		}

		TextRepository repo = Database.getInstance().getTextRepository(getCampaign());
		Database.getInstance().saveColdStringsManifest(manifestEntries, getCampaign());

		for (String deleted : deletedShards)
		{
			repo.deleteColdShardFile(deleted);
			shardCache.remove(deleted);
		}
		deletedShards.clear();

		for (Map.Entry<String, Map<String, ColdString>> entry : shardCache.entrySet())
		{
			Database.getInstance().saveColdStringsShard(
				entry.getKey(), entry.getValue(), getCampaign());
		}
		shardCache.clear();

		repo.resetCaches();
		loadManifestFromDisk();
		initShardList();
	}
}

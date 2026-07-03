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
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.ColdString;
import mclachlan.maze.data.ColdStringManifestEntry;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.TextRepository;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.util.MazeException;

/**
 * Picker for ColdStrings: shard combo plus key combo scoped to that shard.
 */
public class ColdStringKeyPicker extends JPanel implements ActionListener
{
	private final int dirtyFlag;
	private final JComboBox<String> shards;
	private final JComboBox<String> keys;
	private List<ColdStringManifestEntry> manifest = new ArrayList<>();
	private boolean updating;

	public ColdStringKeyPicker(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		add(new JLabel("Cold String Shard:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		shards = new JComboBox<>();
		shards.addActionListener(this);
		add(shards, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		add(new JLabel("Cold String Key:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		keys = new JComboBox<>();
		keys.addActionListener(this);
		add(keys, gbc);

		refreshKeys(null);
	}

	public void refreshKeys(String selectKey)
	{
		updating = true;
		try
		{
			manifest = loadManifest();
			String previousShard = getSelectedShard();
			String previousKey = getSelectedKey();

			shards.removeAllItems();
			shards.addItem(EditorPanel.NONE);
			for (ColdStringManifestEntry entry : manifest)
			{
				shards.addItem(entry.getShard());
			}

			String shardToSelect = EditorPanel.NONE;
			String keyToSelect = EditorPanel.NONE;
			if (selectKey != null && selectKey.length() > 0)
			{
				String shard = findShardForKey(selectKey);
				if (shard != null)
				{
					shardToSelect = shard;
					keyToSelect = selectKey;
				}
			}
			else if (previousShard != null && !EditorPanel.NONE.equals(previousShard))
			{
				shardToSelect = previousShard;
				keyToSelect = previousKey != null ? previousKey : EditorPanel.NONE;
			}

			shards.setSelectedItem(shardToSelect);
			if (shards.getSelectedItem() == null)
			{
				shards.setSelectedItem(EditorPanel.NONE);
			}
			populateKeys(keyToSelect);
		}
		finally
		{
			updating = false;
		}
	}

	public String getColdStringKey()
	{
		String shard = getSelectedShard();
		String key = getSelectedKey();
		if (EditorPanel.NONE.equals(shard) || EditorPanel.NONE.equals(key))
		{
			return null;
		}
		return key;
	}

	public void setColdStringKey(String key)
	{
		refreshKeys(key);
	}

	private void populateKeys(String selectKey)
	{
		String shard = getSelectedShard();
		keys.removeAllItems();
		keys.addItem(EditorPanel.NONE);

		if (EditorPanel.NONE.equals(shard))
		{
			keys.setEnabled(false);
			keys.setSelectedItem(EditorPanel.NONE);
			return;
		}

		keys.setEnabled(true);
		TextRepository repo = getTextRepository();
		Map<String, ColdString> coldShard = repo.getColdShard(shard);
		if (coldShard != null)
		{
			List<String> keyList = new ArrayList<>(coldShard.keySet());
			keyList.sort(String::compareTo);
			for (String coldKey : keyList)
			{
				keys.addItem(coldKey);
			}
		}

		if (selectKey != null && selectKey.length() > 0 && !EditorPanel.NONE.equals(selectKey))
		{
			if (findComboIndex(keys, selectKey) < 0)
			{
				keys.addItem(selectKey);
			}
			keys.setSelectedItem(selectKey);
		}
		else
		{
			keys.setSelectedItem(EditorPanel.NONE);
		}
		if (keys.getSelectedItem() == null)
		{
			keys.setSelectedItem(EditorPanel.NONE);
		}
	}

	private String getSelectedShard()
	{
		Object selected = shards.getSelectedItem();
		return selected != null ? selected.toString() : EditorPanel.NONE;
	}

	private String getSelectedKey()
	{
		Object selected = keys.getSelectedItem();
		return selected != null ? selected.toString() : EditorPanel.NONE;
	}

	private String findShardForKey(String key)
	{
		String bestShard = null;
		int bestLen = -1;
		for (ColdStringManifestEntry entry : manifest)
		{
			String prefix = entry.getPrefix();
			if (prefix != null && key.startsWith(prefix) && prefix.length() > bestLen)
			{
				bestLen = prefix.length();
				bestShard = entry.getShard();
			}
		}
		if (bestShard != null)
		{
			return bestShard;
		}

		TextRepository repo = getTextRepository();
		for (ColdStringManifestEntry entry : manifest)
		{
			Map<String, ColdString> coldShard = repo.getColdShard(entry.getShard());
			if (coldShard != null && coldShard.containsKey(key))
			{
				return entry.getShard();
			}
		}
		return null;
	}

	private static int findComboIndex(JComboBox<String> combo, String value)
	{
		for (int i = 0; i < combo.getItemCount(); i++)
		{
			if (value.equals(combo.getItemAt(i)))
			{
				return i;
			}
		}
		return -1;
	}

	private static List<ColdStringManifestEntry> loadManifest()
	{
		try
		{
			Campaign campaign = Database.getCampaigns().get(SwingEditor.instance.getCurrentCampaign());
			return Database.getInstance().getTextRepository(campaign).getColdManifest();
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	private static TextRepository getTextRepository()
	{
		try
		{
			Campaign campaign = Database.getCampaigns().get(SwingEditor.instance.getCurrentCampaign());
			return Database.getInstance().getTextRepository(campaign);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (updating)
		{
			return;
		}
		if (e.getSource() == shards)
		{
			updating = true;
			try
			{
				populateKeys(EditorPanel.NONE);
			}
			finally
			{
				updating = false;
			}
		}
		if (dirtyFlag >= 0)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}
}

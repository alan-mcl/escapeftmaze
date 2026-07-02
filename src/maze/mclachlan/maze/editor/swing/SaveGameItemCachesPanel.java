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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Item;

/**
 * Editor for save-game item caches (items left on the ground).
 */
public class SaveGameItemCachesPanel extends JPanel
	implements ActionListener, ListSelectionListener
{
	private Map<String, Map<Point, List<Item>>> caches;
	private JComboBox<String> zone;
	private JList<String> tiles;
	private Point currentTile;
	private SaveGameItemListEditor itemListEditor;

	/*-------------------------------------------------------------------------*/
	public SaveGameItemCachesPanel()
	{
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());

		zone = new JComboBox<>();
		zone.addActionListener(this);

		tiles = new JList<>();
		tiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tiles.addListSelectionListener(this);

		itemListEditor = new SaveGameItemListEditor(SwingEditor.Tab.SAVE_GAMES);

		JButton addTile = new JButton("Add Tile");
		addTile.setActionCommand("addTile");
		addTile.addActionListener(this);
		JButton deleteTile = new JButton("Delete Tile");
		deleteTile.setActionCommand("deleteTile");
		deleteTile.addActionListener(this);

		JPanel tileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tileButtons.add(addTile);
		tileButtons.add(deleteTile);

		JPanel left = new JPanel(new BorderLayout(3, 3));
		JPanel zoneHeader = new JPanel();
		zoneHeader.setLayout(new BoxLayout(zoneHeader, BoxLayout.Y_AXIS));
		zoneHeader.add(new JLabel("Zone:"));
		zoneHeader.add(zone);
		left.add(zoneHeader, BorderLayout.NORTH);
		JPanel tilePanel = new JPanel(new BorderLayout(3, 3));
		tilePanel.add(new JLabel("Tiles:"), BorderLayout.NORTH);
		tilePanel.add(new JScrollPane(tiles), BorderLayout.CENTER);
		tilePanel.add(tileButtons, BorderLayout.SOUTH);
		left.add(tilePanel, BorderLayout.CENTER);

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
		add(itemListEditor, gbc);
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> zones = new Vector<>(Database.getInstance().getZoneNames());
		Collections.sort(zones);
		zone.removeActionListener(this);
		zone.setModel(new DefaultComboBoxModel<>(zones));
		zone.addActionListener(this);
		itemListEditor.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Map<String, Map<Point, List<Item>>> itemCaches)
	{
		caches = itemCaches == null ? new HashMap<>() : itemCaches;
		refreshZoneList();
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Map<Point, List<Item>>> getCaches()
	{
		commitCurrentTile();
		return caches;
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String ignored)
	{
		commitCurrentTile();
	}

	/*-------------------------------------------------------------------------*/
	private void refreshZoneList()
	{
		zone.removeActionListener(this);
		Vector<String> zoneNames = new Vector<>(caches.keySet());
		for (String z : Database.getInstance().getZoneNames())
		{
			if (!zoneNames.contains(z))
			{
				zoneNames.add(z);
			}
		}
		Collections.sort(zoneNames);
		zone.setModel(new DefaultComboBoxModel<>(zoneNames));
		if (!zoneNames.isEmpty())
		{
			zone.setSelectedIndex(0);
		}
		zone.addActionListener(this);
		refreshTileList();
	}

	/*-------------------------------------------------------------------------*/
	private void refreshTileList()
	{
		tiles.removeListSelectionListener(this);
		currentTile = null;

		String zoneName = (String)zone.getSelectedItem();
		Vector<String> tileNames = new Vector<>();
		if (zoneName != null)
		{
			Map<Point, List<Item>> zoneMap = caches.get(zoneName);
			if (zoneMap != null)
			{
				List<Point> sorted = new ArrayList<>(zoneMap.keySet());
				sorted.sort((a, b) ->
				{
					int c = Integer.compare(a.y, b.y);
					return c != 0 ? c : Integer.compare(a.x, b.x);
				});
				for (Point p : sorted)
				{
					tileNames.add(p.x + ":" + p.y);
				}
			}
		}
		tiles.setListData(tileNames);
		if (!tileNames.isEmpty())
		{
			tiles.setSelectedIndex(0);
		}
		else
		{
			itemListEditor.refresh(new ArrayList<>());
		}
		tiles.addListSelectionListener(this);
		onTileSelected();
	}

	/*-------------------------------------------------------------------------*/
	private void onTileSelected()
	{
		commitCurrentTile();

		String tileName = tiles.getSelectedValue();
		String zoneName = (String)zone.getSelectedItem();
		if (tileName == null || zoneName == null)
		{
			currentTile = null;
			itemListEditor.refresh(new ArrayList<>());
			return;
		}

		String[] parts = tileName.split(":");
		currentTile = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		Map<Point, List<Item>> zoneMap = caches.get(zoneName);
		List<Item> items = zoneMap == null ? null : zoneMap.get(currentTile);
		itemListEditor.refresh(items == null ? new ArrayList<>() : items);
	}

	/*-------------------------------------------------------------------------*/
	private void commitCurrentTile()
	{
		if (currentTile == null)
		{
			return;
		}
		String zoneName = (String)zone.getSelectedItem();
		if (zoneName == null)
		{
			return;
		}

		itemListEditor.commitToList();
		Map<Point, List<Item>> zoneMap = caches.computeIfAbsent(zoneName, k -> new HashMap<>());
		zoneMap.put(currentTile, new ArrayList<>(itemListEditor.getItems()));
	}

	/*-------------------------------------------------------------------------*/
	private Map<Point, List<Item>> getCurrentZoneMap()
	{
		String zoneName = (String)zone.getSelectedItem();
		if (zoneName == null)
		{
			return null;
		}
		return caches.computeIfAbsent(zoneName, k -> new HashMap<>());
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ("addTile".equals(e.getActionCommand()))
		{
			String xStr = JOptionPane.showInputDialog(this, "Tile X:");
			if (xStr == null)
			{
				return;
			}
			String yStr = JOptionPane.showInputDialog(this, "Tile Y:");
			if (yStr == null)
			{
				return;
			}
			try
			{
				Point p = new Point(Integer.parseInt(xStr.trim()), Integer.parseInt(yStr.trim()));
				Map<Point, List<Item>> zoneMap = getCurrentZoneMap();
				if (zoneMap != null && !zoneMap.containsKey(p))
				{
					zoneMap.put(p, new ArrayList<>());
					markDirty();
					refreshTileList();
					tiles.setSelectedValue(p.x + ":" + p.y, true);
				}
			}
			catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this, "Invalid coordinates.", "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
		else if ("deleteTile".equals(e.getActionCommand()))
		{
			if (currentTile == null)
			{
				return;
			}
			Map<Point, List<Item>> zoneMap = getCurrentZoneMap();
			if (zoneMap != null)
			{
				zoneMap.remove(currentTile);
				markDirty();
				refreshTileList();
			}
		}
		else if (e.getSource() == zone)
		{
			markDirty();
			refreshTileList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting())
		{
			onTileSelected();
		}
	}
}

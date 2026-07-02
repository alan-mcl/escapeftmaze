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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.PlayerTilesVisited;

/**
 * Editor for save-game tiles visited (auto-map exploration per zone and recent trail).
 */
public class SaveGameTilesVisitedPanel extends JPanel implements ActionListener
{
	private Map<String, List<Point>> tilesVisited;
	private JComboBox<String> zone;
	private JList<String> zoneTiles;
	private JList<String> recentTiles;
	private JLabel recentWarning;

	/*-------------------------------------------------------------------------*/
	public SaveGameTilesVisitedPanel()
	{
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());

		zone = new JComboBox<>();
		zone.addActionListener(this);

		zoneTiles = new JList<>();
		zoneTiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		recentTiles = new JList<>();
		recentTiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		recentWarning = new JLabel(" ");
		recentWarning.setForeground(java.awt.Color.RED);

		JPanel zoneSection = buildZoneSection();
		JPanel recentSection = buildRecentSection();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.55;
		gbc.fill = GridBagConstraints.BOTH;
		add(zoneSection, gbc);

		gbc.gridy = 1;
		gbc.weighty = 0.45;
		add(recentSection, gbc);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildZoneSection()
	{
		JPanel section = new JPanel(new BorderLayout(3, 3));
		section.setBorder(BorderFactory.createTitledBorder("Zone exploration"));

		JPanel zoneHeader = new JPanel();
		zoneHeader.setLayout(new BoxLayout(zoneHeader, BoxLayout.Y_AXIS));
		zoneHeader.add(new JLabel("Zone:"));
		zoneHeader.add(zone);
		section.add(zoneHeader, BorderLayout.NORTH);

		JPanel tilePanel = new JPanel(new BorderLayout(3, 3));
		tilePanel.add(new JLabel("Tiles visited:"), BorderLayout.NORTH);
		tilePanel.add(new JScrollPane(zoneTiles), BorderLayout.CENTER);
		tilePanel.add(buildZoneButtons(), BorderLayout.SOUTH);
		section.add(tilePanel, BorderLayout.CENTER);

		return section;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildZoneButtons()
	{
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addTile = new JButton("Add Tile");
		addTile.setActionCommand("addZoneTile");
		addTile.addActionListener(this);
		JButton removeTile = new JButton("Remove");
		removeTile.setActionCommand("removeZoneTile");
		removeTile.addActionListener(this);
		JButton clearZone = new JButton("Clear Zone");
		clearZone.setActionCommand("clearZone");
		clearZone.addActionListener(this);
		buttons.add(addTile);
		buttons.add(removeTile);
		buttons.add(clearZone);
		return buttons;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildRecentSection()
	{
		JPanel section = new JPanel(new BorderLayout(3, 3));
		section.setBorder(BorderFactory.createTitledBorder("Recent tiles (auto-map trail)"));

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.add(new JLabel(
			"Order-sensitive trail used to centre the auto-map (max "
				+ PlayerTilesVisited.MAX_RECENT_TILES + " at runtime)."));
		header.add(recentWarning);
		section.add(header, BorderLayout.NORTH);

		JPanel listPanel = new JPanel(new BorderLayout(3, 3));
		listPanel.add(new JScrollPane(recentTiles), BorderLayout.CENTER);
		listPanel.add(buildRecentButtons(), BorderLayout.SOUTH);
		section.add(listPanel, BorderLayout.CENTER);

		return section;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildRecentButtons()
	{
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addTile = new JButton("Add");
		addTile.setActionCommand("addRecentTile");
		addTile.addActionListener(this);
		JButton removeTile = new JButton("Remove");
		removeTile.setActionCommand("removeRecentTile");
		removeTile.addActionListener(this);
		JButton clearRecent = new JButton("Clear");
		clearRecent.setActionCommand("clearRecent");
		clearRecent.addActionListener(this);
		buttons.add(addTile);
		buttons.add(removeTile);
		buttons.add(clearRecent);
		return buttons;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		zone.removeActionListener(this);
		Vector<String> zones = new Vector<>(Database.getInstance().getZoneNames());
		Collections.sort(zones);
		zone.setModel(new DefaultComboBoxModel<>(zones));
		zone.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(PlayerTilesVisited loaded)
	{
		tilesVisited = new HashMap<>();
		if (loaded != null)
		{
			for (Map.Entry<String, List<Point>> e : loaded.getTilesVisited().entrySet())
			{
				tilesVisited.put(e.getKey(), new ArrayList<>(e.getValue()));
			}
		}
		tilesVisited.computeIfAbsent(PlayerTilesVisited.RECENT_TILES_KEY, k -> new ArrayList<>());
		refreshZoneList();
		refreshRecentList();
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String ignored)
	{
	}

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited getPlayerTilesVisited()
	{
		tilesVisited.computeIfAbsent(PlayerTilesVisited.RECENT_TILES_KEY, k -> new ArrayList<>());
		return new PlayerTilesVisited(tilesVisited);
	}

	/*-------------------------------------------------------------------------*/
	private void refreshZoneList()
	{
		zone.removeActionListener(this);

		Vector<String> zoneNames = new Vector<>();
		for (String key : tilesVisited.keySet())
		{
			if (!PlayerTilesVisited.RECENT_TILES_KEY.equals(key))
			{
				zoneNames.add(key);
			}
		}
		for (String z : Database.getInstance().getZoneNames())
		{
			if (!zoneNames.contains(z))
			{
				zoneNames.add(z);
			}
		}
		Collections.sort(zoneNames);

		String selected = (String)zone.getSelectedItem();
		zone.setModel(new DefaultComboBoxModel<>(zoneNames));
		if (selected != null && zoneNames.contains(selected))
		{
			zone.setSelectedItem(selected);
		}
		else if (!zoneNames.isEmpty())
		{
			zone.setSelectedIndex(0);
		}

		zone.addActionListener(this);
		refreshZoneTileList();
	}

	/*-------------------------------------------------------------------------*/
	private void refreshZoneTileList()
	{
		String zoneName = (String)zone.getSelectedItem();
		Vector<String> tileNames = new Vector<>();
		if (zoneName != null)
		{
			List<Point> visited = tilesVisited.get(zoneName);
			if (visited != null)
			{
				for (Point p : visited)
				{
					tileNames.add(p.x + ":" + p.y);
				}
			}
		}
		zoneTiles.setListData(tileNames);
		if (!tileNames.isEmpty())
		{
			zoneTiles.setSelectedIndex(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void refreshRecentList()
	{
		List<Point> recent = tilesVisited.get(PlayerTilesVisited.RECENT_TILES_KEY);
		Vector<String> tileNames = new Vector<>();
		if (recent != null)
		{
			for (Point p : recent)
			{
				tileNames.add(p.x + ":" + p.y);
			}
		}
		recentTiles.setListData(tileNames);
		if (!tileNames.isEmpty())
		{
			recentTiles.setSelectedIndex(0);
		}
		updateRecentWarning();
	}

	/*-------------------------------------------------------------------------*/
	private void updateRecentWarning()
	{
		List<Point> recent = tilesVisited.get(PlayerTilesVisited.RECENT_TILES_KEY);
		int size = recent == null ? 0 : recent.size();
		if (size > PlayerTilesVisited.MAX_RECENT_TILES)
		{
			recentWarning.setText(
				"Warning: " + size + " entries (runtime keeps at most "
					+ PlayerTilesVisited.MAX_RECENT_TILES + ").");
		}
		else
		{
			recentWarning.setText(" ");
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<Point> getCurrentZoneList()
	{
		String zoneName = (String)zone.getSelectedItem();
		if (zoneName == null)
		{
			return null;
		}
		return tilesVisited.computeIfAbsent(zoneName, k -> new ArrayList<>());
	}

	/*-------------------------------------------------------------------------*/
	private List<Point> getRecentList()
	{
		return tilesVisited.computeIfAbsent(PlayerTilesVisited.RECENT_TILES_KEY, k -> new ArrayList<>());
	}

	/*-------------------------------------------------------------------------*/
	private Point promptForTile(String title)
	{
		String xStr = JOptionPane.showInputDialog(this, "Tile X:", title, JOptionPane.PLAIN_MESSAGE);
		if (xStr == null)
		{
			return null;
		}
		String yStr = JOptionPane.showInputDialog(this, "Tile Y:", title, JOptionPane.PLAIN_MESSAGE);
		if (yStr == null)
		{
			return null;
		}
		try
		{
			return new Point(Integer.parseInt(xStr.trim()), Integer.parseInt(yStr.trim()));
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Invalid coordinates.", "Error",
				JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if ("addZoneTile".equals(cmd))
		{
			Point p = promptForTile("Add zone tile");
			if (p == null)
			{
				return;
			}
			List<Point> visited = getCurrentZoneList();
			if (visited == null)
			{
				return;
			}
			if (visited.contains(p))
			{
				JOptionPane.showMessageDialog(this,
					"Tile already visited in this zone.",
					"Duplicate tile",
					JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			visited.add(p);
			markDirty();
			refreshZoneTileList();
			zoneTiles.setSelectedValue(p.x + ":" + p.y, true);
		}
		else if ("removeZoneTile".equals(cmd))
		{
			String tileName = zoneTiles.getSelectedValue();
			if (tileName == null)
			{
				return;
			}
			List<Point> visited = getCurrentZoneList();
			if (visited == null)
			{
				return;
			}
			String[] parts = tileName.split(":");
			Point p = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			visited.remove(p);
			markDirty();
			refreshZoneTileList();
		}
		else if ("clearZone".equals(cmd))
		{
			List<Point> visited = getCurrentZoneList();
			if (visited == null || visited.isEmpty())
			{
				return;
			}
			visited.clear();
			markDirty();
			refreshZoneTileList();
		}
		else if ("addRecentTile".equals(cmd))
		{
			Point p = promptForTile("Add recent tile");
			if (p == null)
			{
				return;
			}
			getRecentList().add(p);
			markDirty();
			refreshRecentList();
			recentTiles.setSelectedValue(p.x + ":" + p.y, true);
		}
		else if ("removeRecentTile".equals(cmd))
		{
			int index = recentTiles.getSelectedIndex();
			if (index < 0)
			{
				return;
			}
			getRecentList().remove(index);
			markDirty();
			refreshRecentList();
		}
		else if ("clearRecent".equals(cmd))
		{
			List<Point> recent = getRecentList();
			if (recent.isEmpty())
			{
				return;
			}
			recent.clear();
			markDirty();
			refreshRecentList();
		}
		else if (e.getSource() == zone)
		{
			refreshZoneTileList();
		}
	}
}

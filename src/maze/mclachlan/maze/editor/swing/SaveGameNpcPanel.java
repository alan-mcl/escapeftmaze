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
import java.awt.Dimension;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 * Editor for save-game NPC runtime state.
 */
public class SaveGameNpcPanel extends JPanel
	implements ActionListener, ListSelectionListener, ChangeListener
{
	private JList<String> names;
	private String currentName;
	private Map<String, Npc> map;
	private Collection<String> playerCharacterNames;

	private JLabel templateName;
	private JComboBox<String> zone;
	private JSpinner tileX;
	private JSpinner tileY;
	private JComboBox<NpcFaction.Attitude> attitude;
	private JCheckBox found;
	private JCheckBox dead;
	private JCheckBox guildMaster;
	private JSpinner theftCounter;
	private JList<String> guildList;
	private DefaultListModel<String> guildModel;
	private SaveGameItemListEditor tradingInventory;

	/*-------------------------------------------------------------------------*/
	public SaveGameNpcPanel()
	{
		names = new JList<>();
		names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		names.addListSelectionListener(this);
		names.setFixedCellWidth(120);
		JScrollPane nameScroller = new JScrollPane(names);

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("General", buildGeneralTab());
		tabs.add("Trading Inventory", buildTradingTab());

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
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
			tabs);
		add(splitPane, gbc);
		splitPane.setDividerLocation(-1);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildGeneralTab()
	{
		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weighty = 0.0;

		templateName = new JLabel();
		zone = new JComboBox<>();
		zone.addActionListener(this);
		tileX = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		tileX.addChangeListener(this);
		tileY = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		tileY.addChangeListener(this);
		attitude = new JComboBox<>(NpcFaction.Attitude.values());
		attitude.addActionListener(this);
		found = new JCheckBox("Found");
		found.addActionListener(this);
		dead = new JCheckBox("Dead");
		dead.addActionListener(this);
		guildMaster = new JCheckBox("Guild Master");
		guildMaster.addActionListener(this);
		theftCounter = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		theftCounter.addChangeListener(this);

		addRow(content, gbc, "Template:", templateName);
		addRow(content, gbc, "Zone:", zone);
		addRow(content, gbc, "Tile X:", tileX);
		addRow(content, gbc, "Tile Y:", tileY);
		addRow(content, gbc, "Attitude:", attitude);
		addRow(content, gbc, "", found);
		addRow(content, gbc, "", dead);
		addRow(content, gbc, "", guildMaster);
		addRow(content, gbc, "Theft Counter:", theftCounter);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weighty = 0.0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JLabel("Guild members (characters stored at this NPC):"), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guildModel = new DefaultListModel<>();
		guildList = new JList<>(guildModel);
		guildList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane guildScroller = new JScrollPane(guildList);
		guildScroller.setPreferredSize(new Dimension(200, 120));
		content.add(guildScroller, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPanel guildButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addGuildMember = new JButton("Add Member");
		addGuildMember.setActionCommand("addGuildMember");
		addGuildMember.addActionListener(this);
		JButton removeGuildMember = new JButton("Remove Member");
		removeGuildMember.setActionCommand("removeGuildMember");
		removeGuildMember.addActionListener(this);
		guildButtons.add(addGuildMember);
		guildButtons.add(removeGuildMember);
		content.add(guildButtons, gbc);

		JPanel result = new JPanel(new BorderLayout());
		result.add(content, BorderLayout.NORTH);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildTradingTab()
	{
		tradingInventory = new SaveGameItemListEditor(SwingEditor.Tab.SAVE_GAMES);
		return tradingInventory;
	}

	/*-------------------------------------------------------------------------*/
	private void addRow(JPanel panel, GridBagConstraints gbc, String label, Component field)
	{
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		panel.add(new JLabel(label), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(field, gbc);
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		zone.removeActionListener(this);
		Vector<String> zones = new Vector<>(Database.getInstance().getZoneNames());
		Collections.sort(zones);
		zone.setModel(new DefaultComboBoxModel<>(zones));
		zone.addActionListener(this);
		tradingInventory.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Map<String, Npc> npcs, Collection<String> pcNames)
	{
		map = npcs == null ? new HashMap<>() : npcs;
		playerCharacterNames = pcNames == null ? Collections.emptyList() : pcNames;
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Npc> getNpcs()
	{
		return map;
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		String key = name == null ? currentName : name;
		if (key == null || map == null)
		{
			return;
		}

		Npc npc = map.get(key);
		if (npc == null)
		{
			return;
		}

		npc.setZone((String)zone.getSelectedItem());
		npc.setTile(new Point((Integer)tileX.getValue(), (Integer)tileY.getValue()));
		npc.setAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
		npc.setFound(found.isSelected());
		npc.setDead(dead.isSelected());
		npc.setGuildMaster(guildMaster.isSelected());
		npc.setTheftCounter((Integer)theftCounter.getValue());

		List<String> guild = new ArrayList<>();
		for (int i = 0; i < guildModel.size(); i++)
		{
			guild.add(guildModel.get(i));
		}
		npc.setGuild(guild);

		tradingInventory.commitToList();
		npc.setTradingInventory(new ArrayList<>(tradingInventory.getItems()));
	}

	/*-------------------------------------------------------------------------*/
	private void refreshNames(String toBeSelected)
	{
		currentName = null;
		names.removeListSelectionListener(this);

		Vector<String> vec = new Vector<>(map.keySet());
		Collections.sort(vec);
		names.setListData(vec);
		if (toBeSelected != null)
		{
			names.setSelectedValue(toBeSelected, true);
		}
		else if (!vec.isEmpty())
		{
			names.setSelectedIndex(0);
		}
		currentName = names.getSelectedValue();
		if (currentName != null)
		{
			refreshDetail(currentName);
		}

		names.addListSelectionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void refreshDetail(String name)
	{
		Npc npc = map.get(name);
		if (npc == null)
		{
			return;
		}

		removeListeners();
		templateName.setText(npc.getTemplate().getName());
		zone.setSelectedItem(npc.getZone());
		tileX.setValue(npc.getTile().x);
		tileY.setValue(npc.getTile().y);
		attitude.setSelectedItem(npc.getAttitude());
		found.setSelected(npc.isFound());
		dead.setSelected(npc.isDead());
		guildMaster.setSelected(npc.isGuildMaster());
		theftCounter.setValue(npc.getTheftCounter());

		guildModel.clear();
		List<String> guild = npc.getGuild();
		if (guild != null)
		{
			for (String member : guild)
			{
				guildModel.addElement(member);
			}
		}

		tradingInventory.refresh(npc.getTradingInventory());
		addListeners();
	}

	/*-------------------------------------------------------------------------*/
	private void removeListeners()
	{
		zone.removeActionListener(this);
		tileX.removeChangeListener(this);
		tileY.removeChangeListener(this);
		attitude.removeActionListener(this);
		found.removeActionListener(this);
		dead.removeActionListener(this);
		guildMaster.removeActionListener(this);
		theftCounter.removeChangeListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void addListeners()
	{
		zone.addActionListener(this);
		tileX.addChangeListener(this);
		tileY.addChangeListener(this);
		attitude.addActionListener(this);
		found.addActionListener(this);
		dead.addActionListener(this);
		guildMaster.addActionListener(this);
		theftCounter.addChangeListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ("addGuildMember".equals(e.getActionCommand()))
		{
			addGuildMember();
			return;
		}
		if ("removeGuildMember".equals(e.getActionCommand()))
		{
			removeGuildMember();
			return;
		}

		markDirty();
	}

	/*-------------------------------------------------------------------------*/
	private void addGuildMember()
	{
		Vector<String> options = new Vector<>();
		for (String name : playerCharacterNames)
		{
			if (!containsGuildMember(name))
			{
				options.add(name);
			}
		}
		Collections.sort(options);
		if (options.isEmpty())
		{
			JOptionPane.showMessageDialog(this,
				"No more player characters are available to add.",
				"Add Guild Member",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		String selected = (String)JOptionPane.showInputDialog(
			this,
			"Select a player character to add to this NPC's guild:",
			"Add Guild Member",
			JOptionPane.QUESTION_MESSAGE,
			null,
			options.toArray(),
			options.get(0));
		if (selected != null)
		{
			guildModel.addElement(selected);
			markDirty();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void removeGuildMember()
	{
		int index = guildList.getSelectedIndex();
		if (index >= 0)
		{
			guildModel.remove(index);
			markDirty();
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean containsGuildMember(String name)
	{
		for (int i = 0; i < guildModel.size(); i++)
		{
			if (name.equals(guildModel.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		markDirty();
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
}

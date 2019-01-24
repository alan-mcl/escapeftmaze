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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Collections;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 *
 */
public class NpcFactionPanel extends JPanel implements ActionListener, ListSelectionListener
{
	// names
	private JList names;
	private String currentName;
	private Container editControls;
	private int dirtyFlag;

	private JComboBox<NpcFaction.Attitude> attitude;
	private String saveGameName;
	private Map<String, NpcFaction> map;

	/*-------------------------------------------------------------------------*/
	public NpcFactionPanel(String saveGameName)
	{
		this.saveGameName = saveGameName;

		names = new JList();

		refreshNames(null);
		names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		names.addListSelectionListener(this);
		names.setFixedCellWidth(100);
		JScrollPane nameScroller = new JScrollPane(names);

		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls = getEditControls();
		JScrollPane editControlsScroller = new JScrollPane(editControls);

		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			true,
			nameScroller,
			editControlsScroller);

		add(splitPane, gbc);

		if (currentName != null)
		{
			refresh(currentName);
		}

		splitPane.setDividerLocation(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void refreshNames(String toBeSelected)
	{
		currentName = null;
		Vector vec = getNames();
		names.setListData(vec);
		if (toBeSelected == null)
		{
			names.setSelectedIndex(0);
		}
		else
		{
			names.setSelectedValue(toBeSelected, true);
		}
		currentName = (String)names.getSelectedValue();
	}

	/*-------------------------------------------------------------------------*/
	public Vector getNames()
	{
		if (map == null)
		{
			return new Vector();
		}
		Vector vector = new Vector(map.keySet());
		Collections.sort(vector);
		return vector;
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		attitude = new JComboBox<NpcFaction.Attitude>(NpcFaction.Attitude.values());
		attitude.addActionListener(this);

		result.add(new JLabel("Attitude:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		result.add(attitude, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		try
		{
			map = Database.getInstance().getLoader().loadNpcFactions(saveGameName);
			Vector<String> vec = new Vector<String>(map.keySet());
			Collections.sort(vec);
			return vec;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}

	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		NpcFaction nf = map.get(name);

		attitude.removeActionListener(this);
		attitude.setSelectedItem(nf.getAttitude());
		attitude.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		NpcFaction nf = map.get(name==null?currentName:name);
		nf.setAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFaction> getNpcFactions()
	{
		return map;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(NpcManager instance)
	{
		map = instance.getMap();

		refreshNames(null);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{

	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (currentName != null)
		{
			commit(currentName);
		}

		currentName = (String)names.getSelectedValue();
		if (currentName == null)
		{
			return;
		}
		if (currentName != null)
		{
			refresh(currentName);
		}
	}
}

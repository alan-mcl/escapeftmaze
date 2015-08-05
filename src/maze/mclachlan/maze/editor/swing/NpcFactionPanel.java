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
import java.util.Vector;
import java.util.Collections;
import java.util.Map;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 *
 */
public class NpcFactionPanel extends EditorPanel
{
	private JComboBox<NpcFaction.Attitude> attitude;
	private String saveGameName;
	private Map<String, NpcFaction> map;

	/*-------------------------------------------------------------------------*/
	public NpcFactionPanel(String saveGameName)
	{
		super(SwingEditor.Tab.SAVE_GAMES);
		this.saveGameName = saveGameName;
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
		NpcFaction nf = map.get(name);
		nf.setAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFaction> getNpcFactions()
	{
		return map;
	}
}

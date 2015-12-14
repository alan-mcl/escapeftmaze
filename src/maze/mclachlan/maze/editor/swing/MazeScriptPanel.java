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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class MazeScriptPanel extends EditorPanel
{
	private MazeEventsPanel panel;

	/*-------------------------------------------------------------------------*/
	public MazeScriptPanel()
	{
		super(SwingEditor.Tab.SCRIPTS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls.add(new JLabel("Maze Script:"), gbc);
		gbc.gridy++;
		gbc.gridwidth = 3;
		editControls.add(new JLabel("Events are executed in order"), gbc);
		
		panel = new MazeEventsPanel(dirtyFlag);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth = 3;
		editControls.add(panel, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		MazeScript le = Database.getInstance().getScript(name);

		panel.refresh(le.getEvents());
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		MazeScript me = new MazeScript(name, new ArrayList<MazeEvent>());
		Database.getInstance().getMazeScripts().put(name, me);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		MazeScript me = Database.getInstance().getMazeScripts().remove(currentName);
		me.setName(newName);
		Database.getInstance().getMazeScripts().put(newName, me);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		MazeScript current = Database.getInstance().getMazeScripts().get(currentName);
		MazeScript me = new MazeScript(newName, current.getEvents());
		Database.getInstance().getMazeScripts().put(newName, me);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getMazeScripts().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		MazeScript me = Database.getInstance().getMazeScripts().get(name);
		if (me != null)
		{
			me.setEvents(panel.getMazeEvents());
		}
	}
}

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

package mclachlan.maze.editor.swing.map;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class ToolsPanel extends JPanel implements ActionListener
{
	private final MapEditor editor;
	private final Zone zone;
	private final Map<String, Tool> tools = new HashMap<>();

	/*-------------------------------------------------------------------------*/
	public ToolsPanel(MapEditor editor, Zone zone)
	{
		this.editor = editor;
		this.zone = zone;
		List<Tool> list = this.editor.getTools();

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		for (Tool t : list)
		{
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.gridx=0;
			gbc.gridy++;
			
			String toolName = t.getName();
			JButton button = new JButton(toolName);
			button.addActionListener(this);
			button.setActionCommand(toolName);
			add(button, gbc);
			tools.put(toolName, t);
		}

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		add(new JPanel(), gbc);
	}
	
	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		Tool tool = tools.get(e.getActionCommand());
		tool.execute(editor, zone);
	}
}

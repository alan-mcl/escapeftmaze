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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class PortalDisplayPanel extends JPanel implements ActionListener
{
	private PortalDetailsPanel details;
	private Portal portal;
	private JButton delete;
	private Zone zone;
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public PortalDisplayPanel(Zone zone, MapEditor editor)
	{
		this.zone = zone;
		this.editor = editor;

		setLayout(new BorderLayout());
		add(new JLabel(" --- Portal Details --- "), BorderLayout.NORTH);
		details = new PortalDetailsPanel(zone);
		add(details, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		delete = new JButton("Delete This Portal");
		delete.addActionListener(this);
		buttonPanel.add(delete);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	/*-------------------------------------------------------------------------*/
	public void setPortal(Portal portal)
	{
		this.portal = portal;
		details.refresh(portal);
	}
	
	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == delete)
		{
			zone.removePortal(portal);
			editor.clearSelection();
		}
	}
}

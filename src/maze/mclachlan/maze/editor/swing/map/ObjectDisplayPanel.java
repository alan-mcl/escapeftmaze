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

import java.awt.BorderLayout;
import javax.swing.*;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class ObjectDisplayPanel extends JPanel
{
	private ObjectDetailsPanel details;

	/*-------------------------------------------------------------------------*/
	public ObjectDisplayPanel(Zone zone)
	{
		setLayout(new BorderLayout());
		add(new JLabel(" --- Object Details --- "), BorderLayout.NORTH);
		details = new ObjectDetailsPanel(false, zone);
		add(details, BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void setObject(EngineObject object)
	{
		details.refresh(new SingleObjectProxy(object));
	}
}

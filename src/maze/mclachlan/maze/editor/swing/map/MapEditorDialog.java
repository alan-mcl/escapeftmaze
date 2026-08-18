/*
 * Copyright (c) 2026 Alan McLachlan
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
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.ZonePanel;
import mclachlan.maze.map.Zone;

/**
 * Opens the map editor on an in-memory zone without persisting it.
 */
public final class MapEditorDialog
{
	private MapEditorDialog()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void open(Frame owner, Zone zone)
	{
		open(owner, zone, null);
	}

	/*-------------------------------------------------------------------------*/
	public static void open(Frame owner, Zone zone, ZonePanel zonePanel)
	{
		JDialog dialog = new JDialog(owner, "Map Editor: " + zone.getName(), true);
		dialog.add(new MapEditor(zone, dialog, zonePanel));
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)(d.getWidth() / 2);
		int centerY = (int)(d.getHeight() / 2);
		int width = (int)(d.getWidth() - 150);
		int height = (int)(d.getHeight() - 150);
		dialog.setBounds(centerX - width / 2, centerY - height / 2, width, height);
		dialog.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	public static Zone cloneShell(String shellName, String previewName)
	{
		Zone zone = Database.getInstance().getZone(shellName);
		zone.setName(previewName);
		zone.setDisplayName(previewName);
		return zone;
	}
}

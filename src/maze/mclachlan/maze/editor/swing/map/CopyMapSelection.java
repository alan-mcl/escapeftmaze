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

import java.util.List;
import javax.swing.JOptionPane;
import mclachlan.maze.map.Zone;

/**
 * Copies the current map selection to the editor clipboard.
 */
public class CopyMapSelection extends Tool
{
	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "Copy";
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(MapEditor editor, Zone zone)
	{
		List<Object> selection = editor.getSelection();

		if (selection == null || selection.isEmpty())
		{
			JOptionPane.showMessageDialog(editor, "No selection to copy");
			return;
		}

		MapSelectionClipboard clipboard = MapSelectionCopier.copy(editor, zone);

		if (clipboard == null || clipboard.isEmpty())
		{
			JOptionPane.showMessageDialog(editor, "No selection to copy");
			return;
		}

		editor.setClipboard(clipboard);
		editor.refreshClipboardStatus();
	}
}

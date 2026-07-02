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

import javax.swing.JOptionPane;
import mclachlan.maze.map.Zone;

/**
 * Enters paste mode so the next tile click applies the clipboard.
 */
public class PasteMapSelection extends Tool
{
	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "Paste";
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(MapEditor editor, Zone zone)
	{
		if (editor.getClipboard() == null || editor.getClipboard().isEmpty())
		{
			JOptionPane.showMessageDialog(editor, "Nothing to paste — copy a selection first");
			return;
		}

		editor.enterPasteMode();
	}
}

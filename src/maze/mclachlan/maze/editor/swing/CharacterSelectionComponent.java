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
import javax.swing.*;
import mclachlan.maze.stat.CharacterSelection;

/**
 * Summary button that opens {@link CharacterSelectionEditor}.
 */
public class CharacterSelectionComponent extends JButton implements ActionListener
{
	private CharacterSelection selection;
	private final int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public CharacterSelectionComponent(int dirtyFlag)
	{
		this(null, dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public CharacterSelectionComponent(CharacterSelection selection, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		refresh(selection);
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		Dimension result = new Dimension(d);
		result.height -= 5;
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(CharacterSelection selection)
	{
		this.selection = selection;
		if (selection == null
			|| ((selection.getMethods() == null || selection.getMethods().isEmpty())
				&& (selection.getExclusions() == null || selection.getExclusions().isEmpty())))
		{
			setText(EditorPanel.NONE);
		}
		else
		{
			setText(selection.describe());
		}
	}

	/*-------------------------------------------------------------------------*/
	public CharacterSelection getSelection()
	{
		return selection;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelection(CharacterSelection selection)
	{
		this.selection = selection;
		refresh(selection);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		CharacterSelectionEditor dialog = new CharacterSelectionEditor(
			SwingEditor.instance, selection, dirtyFlag);
		if (dialog.getResult() != null)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			setSelection(dialog.getResult());
		}
	}
}

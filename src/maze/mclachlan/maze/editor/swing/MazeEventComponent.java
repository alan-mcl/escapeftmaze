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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MazeEventComponent extends JButton implements ActionListener
{
	private MazeEvent result;
	private final int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public MazeEventComponent(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(MazeEvent e)
	{
		this.result = e;
		if (e == null)
		{
			this.setText("null");
		}
		else
		{
			String text = e.getClass().getSimpleName();

			this.setToolTipText(text);

			if (text.length() > 30)
			{
				text = text.substring(0, 26)+"...";
			}
			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public MazeEvent getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setResult(MazeEvent result)
	{
		this.result = result;
		refresh(result);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this)
		{
			MazeEventEditor dialog = new MazeEventEditor(SwingEditor.instance, result, dirtyFlag);
			this.result = dialog.getResult();
			refresh(this.result);
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}
}

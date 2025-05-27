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
import mclachlan.maze.stat.magic.SpellResult;

/**
 *
 */
public class SpellResultComponent extends JButton implements ActionListener
{
	private SpellResult spellResult;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public SpellResultComponent(int dirtyFlag)
	{
		this(null, dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public SpellResultComponent(SpellResult spellResult, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		refresh(spellResult);
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(SpellResult spellResult)
	{
		this.spellResult = spellResult;
		if (spellResult == null)
		{
			this.setText("null");
		}
		else
		{
			String text = spellResult.getClass().getSimpleName();

			this.setToolTipText(text);

			if (text.length() > 20)
			{
				text = text.substring(0, 16)+"...";
			}
			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellResult getSpellResult()
	{
		return spellResult;
	}

	/*-------------------------------------------------------------------------*/
	public void setResult(SpellResult spellResult)
	{
		this.spellResult = spellResult;
		this.refresh(spellResult);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			SpellResultEditor dialog = new SpellResultEditor(SwingEditor.instance, spellResult, dirtyFlag);
			this.spellResult = dialog.getResult();
			refresh(this.spellResult);
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}
}

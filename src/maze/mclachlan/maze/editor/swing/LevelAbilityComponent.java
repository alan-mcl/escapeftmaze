/*
 * Copyright (c) 2014 Alan McLachlan
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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.LevelAbility;

/**
 *
 */
public class LevelAbilityComponent extends JButton implements ActionListener
{
	private LevelAbility result;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public LevelAbilityComponent(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(LevelAbility la)
	{
		this.result = la;
		if (la == null)
		{
			this.setText("+");
		}
		else
		{
			String text = StringUtil.getGamesysString(la.getDisplayName(), true, la.getDisplayArgs());
			this.setToolTipText(la.getDisplayName());

			if (text == null)
			{
				text = la.getDisplayName();
			}

			int maxLen = 20;
			if (text.length() > maxLen)
			{
				text = text.substring(0, maxLen -4)+"...";
			}

			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public LevelAbility getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this)
		{
			LevelAbilityEditor dialog = new LevelAbilityEditor(SwingEditor.instance, result, dirtyFlag);
			refresh(dialog.getResult());
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}
}

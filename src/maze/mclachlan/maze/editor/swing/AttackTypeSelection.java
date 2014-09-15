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

import javax.swing.*;
import mclachlan.maze.data.Database;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 */
public class AttackTypeSelection extends JPanel implements ActionListener
{
	Map<String, JCheckBox> checkBoxes;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public AttackTypeSelection(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		List<String> attacktypeList = new ArrayList<String>(Database.getInstance().getAttackTypes().keySet());
		Collections.sort(attacktypeList);
		int max = attacktypeList.size();
		checkBoxes = new HashMap<String, JCheckBox>();

		JPanel panel = new JPanel(new GridLayout(max, 1, 3, 3));
		for (String s : attacktypeList)
		{
			JCheckBox cb = new JCheckBox(s);
			checkBoxes.put(s, cb);
			cb.addActionListener(this);
			panel.add(cb);
		}

		JScrollPane scroller = new JScrollPane(panel);
		this.add(scroller);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String[] allowedAttacktypes)
	{
		if (allowedAttacktypes == null)
		{
			// deselect all
			for (JCheckBox cb : checkBoxes.values())
			{
				cb.setSelected(false);
			}
			return;
		}

		// disable all
		for (JCheckBox cb : checkBoxes.values())
		{
			cb.setSelected(false);
		}
		for (String s : allowedAttacktypes)
		{
			JCheckBox cb = checkBoxes.get(s);
			cb.setSelected(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public String[] getAttackTypes()
	{
		List<String> result = new ArrayList<String>();
		for (JCheckBox cb : checkBoxes.values())
		{
			if (cb.isSelected())
			{
				result.add(cb.getText());
			}
		}
		return result.toArray(new String[result.size()]);
	}
}

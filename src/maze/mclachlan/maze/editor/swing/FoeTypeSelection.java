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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.FoeType;

/**
 *
 */
public class FoeTypeSelection extends JPanel implements ActionListener
{
	private JButton selectAll, selectNone;
	private Map<String, JCheckBox> checkBoxes;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public FoeTypeSelection(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		List<String> foeTypesList = new ArrayList<String>(
			Database.getInstance().getFoeTypes().keySet());

		Collections.sort(foeTypesList);
		int max = foeTypesList.size();
		checkBoxes = new HashMap<String, JCheckBox>();

		JPanel panel = new JPanel(new GridLayout(max/2+2, 2, 3, 3));

		selectAll = new JButton("Select All");
		selectAll.addActionListener(this);
		selectNone = new JButton("Select None");
		selectNone.addActionListener(this);

		panel.add(selectAll);
		panel.add(selectNone);

		for (String s : foeTypesList)
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
	public void refresh(Collection<FoeType> types)
	{
		if (types == null)
		{
			// select none
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
		for (FoeType ft : types)
		{
			JCheckBox cb = checkBoxes.get(ft.getName());
			if (cb == null)
			{
				// someone has deleted something
				continue;
			}
			cb.setSelected(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (e.getSource() == selectAll)
		{
			massSelection(true);
		}
		else if (e.getSource() == selectNone)
		{
			massSelection(false);
		}
	}

	private void massSelection(boolean b)
	{
		for (JCheckBox cb : this.checkBoxes.values())
		{
			cb.setSelected(b);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<FoeType> getFoeTypes()
	{
		List<FoeType> result = new ArrayList<FoeType>();
		for (JCheckBox cb : checkBoxes.values())
		{
			if (cb.isSelected())
			{
				result.add(Database.getInstance().getFoeTypes().get(cb.getText()));
			}
		}
		return result;
	}
}

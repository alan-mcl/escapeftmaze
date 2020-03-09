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
import mclachlan.maze.stat.CharacterClass;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 */
public class CharacterClassSelection extends JPanel implements ActionListener
{
	private JButton selectAll, selectNone;
	private Map<String, JCheckBox> checkBoxes;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public CharacterClassSelection(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		List<String> characterClassList;
		characterClassList = new ArrayList<>(Database.getInstance().getCharacterClasses().keySet());

		Collections.sort(characterClassList);
		int max = characterClassList.size();
		checkBoxes = new HashMap<String, JCheckBox>();

		JPanel panel = new JPanel(new GridLayout(max/2+2, 2, 3, 3));

		selectAll = new JButton("Select All");
		selectAll.addActionListener(this);
		selectNone = new JButton("Select None");
		selectNone.addActionListener(this);

		panel.add(selectAll);
		panel.add(selectNone);

		for (String s : characterClassList)
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
	public void refresh(Collection<String> allowedCharacterClasses)
	{
		if (allowedCharacterClasses == null)
		{
			// select all
			for (JCheckBox cb : checkBoxes.values())
			{
				cb.setSelected(true);
			}
			return;
		}

		// disable all
		for (JCheckBox cb : checkBoxes.values())
		{
			cb.setSelected(false);
		}
		for (String s : allowedCharacterClasses)
		{
			JCheckBox cb = checkBoxes.get(s);
			if (cb == null)
			{
				// someone has deleted a character class!
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
	public Set<String> getAllowedCharacterClasses()
	{
		Set<String> result = new HashSet<String>();
		for (JCheckBox cb : checkBoxes.values())
		{
			if (cb.isSelected())
			{
				result.add(cb.getText());
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<CharacterClass> getAllowedCharacterclassList()
	{
		List<CharacterClass> result = new ArrayList<CharacterClass>();
		for (JCheckBox cb : checkBoxes.values())
		{
			if (cb.isSelected())
			{
				result.add(Database.getInstance().getCharacterClass(cb.getText()));
			}
		}
		return result;
	}
}

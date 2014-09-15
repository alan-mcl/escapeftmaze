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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.stat.Gender;

/**
 *
 */
public class GenderSelection extends JPanel implements ActionListener, KeyListener
{
	private Map<String, JCheckBox> checkBoxes;
	private Map<String, JTextField> suggestedNames;
	private int dirtyFlag;
	private boolean captureSuggestedNames;

	/*-------------------------------------------------------------------------*/
	public GenderSelection(int dirtyFlag, boolean captureSuggestedNames)
	{
		this.dirtyFlag = dirtyFlag;
		this.captureSuggestedNames = captureSuggestedNames;
		List<String> genderList = Database.getInstance().getGenderList();
		Collections.sort(genderList);
		int max = genderList.size();
		checkBoxes = new HashMap<String, JCheckBox>();
		if (captureSuggestedNames)
		{
			suggestedNames = new HashMap<String, JTextField>();
		}

		JPanel panel = new JPanel(new GridLayout(max, 1, 3, 3));
		for (String s : genderList)
		{
			JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

			JCheckBox cb = new JCheckBox(s);
			cb.addActionListener(this);
			row.add(cb);
			checkBoxes.put(s, cb);

			if (captureSuggestedNames)
			{
				JTextField tf = new JTextField(20);
				tf.addActionListener(this);
				tf.addKeyListener(this);
				row.add(new JLabel("Suggested Names:"));
				row.add(tf);
				suggestedNames.put(s, tf);
			}

			panel.add(row);
		}

		JScrollPane scroller = new JScrollPane(panel);
		this.add(scroller);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Collection<String> allowedGenders,
		Map<String, List<String>> names)
	{
		if (allowedGenders == null)
		{
			selectAll();
			return;
		}
		disableAll();

		for (String s : allowedGenders)
		{
			JCheckBox cb = checkBoxes.get(s);
			cb.setSelected(true);
			if (captureSuggestedNames)
			{
				JTextField tf = suggestedNames.get(s);
				tf.setEnabled(true);

				String t;
				if (names != null)
				{
					List<String> n = names.get(s);
					t = V1Utils.stringList.toString(n);
				}
				else
				{
					t = "";
				}

				tf.setText(t);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void disableAll()
	{
		// disable all
		for (JCheckBox cb : checkBoxes.values())
		{
			cb.setSelected(false);
		}
		if (captureSuggestedNames)
		{
			for (JTextField tf : suggestedNames.values())
			{
				tf.setEnabled(false);
				tf.setText("");
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void selectAll()
	{
		// select all
		for (JCheckBox cb : checkBoxes.values())
		{
			cb.setSelected(true);
		}
		if (captureSuggestedNames)
		{
			for (JTextField tf : suggestedNames.values())
			{
				tf.setEnabled(true);
				tf.setText("");
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refreshGenders(Collection<Gender> allowedGenders,
		Map<String, List<String>> names)
	{
		List<String> genders = new ArrayList<String>();
		for (Gender g : allowedGenders)
		{
			genders.add(g.getName());
		}
		refresh(genders, names);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public Set<String> getAllowedGenders()
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
	public List<Gender> getAllowedGendersList()
	{
		List<Gender> result = new ArrayList<Gender>();
		for (JCheckBox cb : checkBoxes.values())
		{
			if (cb.isSelected())
			{
				result.add(Database.getInstance().getGender(cb.getText()));
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, List<String>> getSuggestedNamesMap()
	{
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		for (String s : suggestedNames.keySet())
		{
			if (checkBoxes.get(s).isSelected())
			{
				JTextField tf = suggestedNames.get(s);
				List<String> strings = V1Utils.stringList.fromString(tf.getText());

				if (strings != null)
				{
					for (int i=0; i<strings.size(); i++)
					{
						strings.set(i, strings.get(i).trim());
					}
				}

				result.put(s, strings);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void keyTyped(KeyEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	public void keyPressed(KeyEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	public void keyReleased(KeyEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}

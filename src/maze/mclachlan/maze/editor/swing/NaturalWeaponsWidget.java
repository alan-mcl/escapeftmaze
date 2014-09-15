/*
 * Copyright (c) 2013 Alan McLachlan
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.NaturalWeapon;

/**
 *
 */
public class NaturalWeaponsWidget extends JPanel
	implements ActionListener
{
	private JList list;
	private NaturalWeaponsListModel dataModel;
	private JButton add, remove;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public NaturalWeaponsWidget(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new NaturalWeaponsListModel(new ArrayList<NaturalWeapon>());
		list = new JList(dataModel);
		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);

		JPanel panel = new JPanel(new BorderLayout(3,3));
		JScrollPane scroller = new JScrollPane(list);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(remove);
		panel.add(scroller, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		this.add(panel);
		this.setBorder(BorderFactory.createTitledBorder("Natural Weapons"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<NaturalWeapon> list)
	{
		this.dataModel.clear();
		if (list == null)
		{
			return;
		}
		for (NaturalWeapon nw : list)
		{
			this.dataModel.add(nw);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (e.getSource() == add)
		{
			List<String> naturalWeaponOptions = new ArrayList<String>(
				Database.getInstance().getNaturalWeapons().keySet());
			Collections.sort(naturalWeaponOptions);
			String[] options = naturalWeaponOptions.toArray(new String[naturalWeaponOptions.size()]);

			String selected = (String)JOptionPane.showInputDialog(
				this,
				"Select a natural weapon",
				"Add Natural Weapon",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

			if (selected != null)
			{
				NaturalWeapon nw = Database.getInstance().getNaturalWeapons().get(selected);
				dataModel.add(nw);
			}
		}
		else if (e.getSource() == remove)
		{
			int index = list.getSelectedIndex();
			if (index > -1)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				dataModel.remove(index);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<NaturalWeapon> getNaturalWeapons()
	{
		return new ArrayList<NaturalWeapon>(dataModel.data);
	}

	/*-------------------------------------------------------------------------*/
	private static class NaturalWeaponsListModel extends AbstractListModel
	{
		private List<NaturalWeapon> data;

		public NaturalWeaponsListModel(List<NaturalWeapon> data)
		{
			this.data = data;
		}

		public Object getElementAt(int index)
		{
			return data.get(index).getName();
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(NaturalWeapon si)
		{
			data.add(si);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(NaturalWeapon si, int index)
		{
			data.set(index, si);
			fireContentsChanged(this, index, index);
		}

		public void clear()
		{
			int size = data.size();
			data.clear();
			fireContentsChanged(this, 0, size-1);
		}
	}
}

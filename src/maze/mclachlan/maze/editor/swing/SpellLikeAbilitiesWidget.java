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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.stat.SpellLikeAbility;

/**
 *
 */
public class SpellLikeAbilitiesWidget extends JPanel
	implements ActionListener, MouseListener
{
	private JList list;
	private SpellLikeAbilitiesListModel dataModel;
	private JButton add, remove, edit;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public SpellLikeAbilitiesWidget(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new SpellLikeAbilitiesListModel(new ArrayList<SpellLikeAbility>());
		list = new JList(dataModel);
		list.addMouseListener(this);
		add = new JButton("Add");
		add.addActionListener(this);
		edit = new JButton("Edit");
		edit.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);

		JPanel panel = new JPanel(new BorderLayout(3,3));
		JScrollPane scroller = new JScrollPane(list);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(edit);
		buttons.add(remove);
		panel.add(scroller, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		this.add(panel);
		this.setBorder(BorderFactory.createTitledBorder("Spell-like Abilities"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<SpellLikeAbility> list)
	{
		this.dataModel.clear();
		if (list == null)
		{
			return;
		}
		for (SpellLikeAbility nw : list)
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
			SpellLikeAbilityEditor dialog = new SpellLikeAbilityEditor(
				SwingEditor.instance, null, dirtyFlag);

			SpellLikeAbility selected = dialog.getResult();

			if (selected != null)
			{
				dataModel.add(selected);
			}
		}
		else if (e.getSource() == edit)
		{
			editListItem();
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
	private void editListItem()
	{
		SpellLikeAbilityEditor dialog = new SpellLikeAbilityEditor(
			SwingEditor.instance,
			(SpellLikeAbility)dataModel.getElementAt(list.getSelectedIndex()),
			dirtyFlag);

		SpellLikeAbility selected = dialog.getResult();

		if (selected != null)
		{
			dataModel.update(selected, list.getSelectedIndex());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == list)
		{
			if (e.getClickCount() == 2)
			{
				// a double click on a list item, treat as an edit
				SwingEditor.instance.setDirty(dirtyFlag);
				editListItem();
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{

	}

	public void mouseReleased(MouseEvent e)
	{

	}

	public void mouseEntered(MouseEvent e)
	{

	}

	public void mouseExited(MouseEvent e)
	{

	}


	/*-------------------------------------------------------------------------*/
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return new ArrayList<SpellLikeAbility>(dataModel.data);
	}

	/*-------------------------------------------------------------------------*/
	private static class SpellLikeAbilitiesListModel extends AbstractListModel
	{
		private List<SpellLikeAbility> data;

		public SpellLikeAbilitiesListModel(List<SpellLikeAbility> data)
		{
			this.data = data;
		}

		public Object getElementAt(int index)
		{
			return data.get(index);
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(SpellLikeAbility sla)
		{
			data.add(sla);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(SpellLikeAbility sla, int index)
		{
			data.set(index, sla);
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

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
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;

/**
 *
 */
public class ConditionListWidget extends JPanel
	implements ActionListener, MouseListener
{
	private JList list;
	private ConditionListModel dataModel;
	private JButton add, remove, edit;
	private int dirtyFlag;
	private ConditionBearer conditionBearer;

	/*-------------------------------------------------------------------------*/
	public ConditionListWidget(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new ConditionListWidget.ConditionListModel(new ArrayList<Condition>());
		list = new JList(dataModel);
		list.addMouseListener(this);
		list.setVisibleRowCount(20);
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
		this.setBorder(BorderFactory.createTitledBorder("Conditions"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<Condition> spell)
	{
		this.dataModel.clear();
		if (spell == null)
		{
			return;
		}
		for (Condition si : spell)
		{
			this.dataModel.add(si);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(ConditionBearer cb)
	{
		this.dataModel.clear();
		if (cb == null)
		{
			return;
		}

		this.conditionBearer = cb;

		for (Condition si : cb.getConditions())
		{
			this.dataModel.add(si);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (e.getSource() == add)
		{
			final JDialog dialog = new JDialog(SwingEditor.instance, "Add Condition", true);
			dialog.setLayout(new BorderLayout());

			final ConditionWidget conditionWidget = new ConditionWidget(this.dirtyFlag);
			conditionWidget.refresh(null);

			JPanel buttons = new JPanel();
			JButton ok = new JButton("OK");
			ok.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Condition condition = conditionWidget.getCondition();
					dataModel.add(condition);
					dialog.setVisible(false);
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dialog.setVisible(false);
				}
			});
			buttons.add(ok);
			buttons.add(cancel);

			dialog.add(conditionWidget, BorderLayout.CENTER);
			dialog.add(buttons, BorderLayout.SOUTH);

			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
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
		else if (e.getSource() == edit)
		{
			editListItem();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		final int index = list.getSelectedIndex();
		if (index > -1)
		{
			Condition c = (Condition)dataModel.data.get(index);

			final JDialog dialog = new JDialog(SwingEditor.instance, "Edit Condition", true);
			dialog.setLayout(new BorderLayout());

			final ConditionWidget conditionWidget = new ConditionWidget(this.dirtyFlag);
			conditionWidget.refresh(c);

			JPanel buttons = new JPanel();
			JButton ok = new JButton("OK");
			ok.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Condition condition = conditionWidget.getCondition();
					dataModel.update(condition, index);
					dialog.setVisible(false);
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dialog.setVisible(false);
				}
			});
			buttons.add(ok);
			buttons.add(cancel);

			dialog.add(conditionWidget, BorderLayout.CENTER);
			dialog.add(buttons, BorderLayout.SOUTH);

			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Condition> getConditions()
	{
		return new ArrayList<Condition>(dataModel.data);
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
	static class ConditionListModel extends AbstractListModel
	{
		List<Condition> data;

		public ConditionListModel(List<Condition> data)
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

		public void add(Condition si)
		{
			data.add(si);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(Condition si, int index)
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

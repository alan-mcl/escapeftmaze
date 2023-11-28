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
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.data.v1.V1CrusaderObjectScript;

/**
 *
 */
public class ObjectScriptListPanel extends JPanel
	implements ActionListener, MouseListener
{
	private final JList<String> list;
	private final ObjectScriptListModel dataModel;
	private final JButton add, remove, edit;
	private final int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public ObjectScriptListPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new ObjectScriptListModel(new ArrayList<>());
		list = new JList<>(dataModel);
		list.addMouseListener(this);
		list.setVisibleRowCount(15);
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
		this.setBorder(BorderFactory.createTitledBorder("Object Scripts"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<ObjectScript> scripts)
	{
		this.dataModel.clear();
		if (scripts == null)
		{
			return;
		}

		if (scripts != null)
		{
			for (ObjectScript script : scripts)
			{
				this.dataModel.add(script);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (e.getSource() == add)
		{
			ObjectScriptEditor dialog = new ObjectScriptEditor(SwingEditor.instance, null, dirtyFlag);
			ObjectScript script = dialog.getResult();

			if (script != null)
			{
				this.dataModel.add(script);
				SwingEditor.instance.setDirty(dirtyFlag);
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
		else if (e.getSource() == edit)
		{
			editListItem();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = list.getSelectedIndex();
		if (index > -1)
		{
			ObjectScriptEditor dialog = new ObjectScriptEditor(
				SwingEditor.instance,
				(ObjectScript)dataModel.data.get(index),
				dirtyFlag);
			ObjectScript script = dialog.getResult();

			if (script != null)
			{
				dataModel.update(script, index);
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<ObjectScript> getObjectScripts()
	{
		return new ArrayList<>(dataModel.data);
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
	static class ObjectScriptListModel extends AbstractListModel<String>
	{
		List<ObjectScript> data;

		public ObjectScriptListModel(List<ObjectScript> data)
		{
			this.data = data;
		}

		public String getElementAt(int index)
		{
			ObjectScript s = data.get(index);
			return s.getClass().getSimpleName()+"("+ V1CrusaderObjectScript.toString(s) +")";
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(ObjectScript si)
		{
			data.add(si);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(ObjectScript si, int index)
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
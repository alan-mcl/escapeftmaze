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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.v1.V1MazeEvent;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;

/**
 *
 */
public class MazeEventsPanel extends JPanel
	implements ActionListener, MouseListener
{
	JList list;
	MazeEventsListModel dataModel;
	JButton add, remove, edit, moveUp, moveDown;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public MazeEventsPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new MazeEventsListModel(new ArrayList<MazeEvent>());
		list = new JList(dataModel);
		list.addMouseListener(this);
		add = new JButton("Add");
		add.addActionListener(this);
		edit = new JButton("Edit");
		edit.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		moveUp = new JButton("Move Up");
		moveUp.addActionListener(this);
		moveDown = new JButton("Move Down");
		moveDown.addActionListener(this);

		JPanel panel = new JPanel(new BorderLayout(3,3));
		JScrollPane scroller = new JScrollPane(list);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(edit);
		buttons.add(remove);
		buttons.add(moveUp);
		buttons.add(moveDown);
		panel.add(scroller, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		this.add(panel);
		this.setBorder(BorderFactory.createTitledBorder("Maze Events"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(java.util.List<MazeEvent> events)
	{
		this.dataModel.clear();
		if (events == null)
		{
			return;
		}
		for (MazeEvent si : events)
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
			MazeEvent me = new ZoneChangeEvent("", new Point(), ZoneChangeEvent.Facing.NORTH);
			MazeEventEditor dialog = new MazeEventEditor(SwingEditor.instance, me, dirtyFlag);
			if (dialog.getResult() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				dataModel.add(dialog.getResult());
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
		else if (e.getSource() == moveUp)
		{
			dataModel.moveUp(list.getSelectedIndex());
			list.setSelectedIndex(list.getSelectedIndex()-1);
		}
		else if (e.getSource() == moveDown)
		{
			dataModel.moveDown(list.getSelectedIndex());
			list.setSelectedIndex(list.getSelectedIndex()+1);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = list.getSelectedIndex();
		if (index > -1)
		{
			MazeEvent event = dataModel.data.get(index);
			MazeEventEditor dialog = new MazeEventEditor(SwingEditor.instance, event, dirtyFlag);
			if (dialog.getResult() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				dataModel.update(dialog.getResult(), index);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> getMazeEvents()
	{
		return new ArrayList<MazeEvent>(dataModel.data);
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
	static class MazeEventsListModel extends AbstractListModel
	{
		java.util.List<MazeEvent> data;

		public MazeEventsListModel(java.util.List<MazeEvent> data)
		{
			this.data = data;
		}

		public Object getElementAt(int index)
		{
			MazeEvent event = data.get(index);
			String s = event.getClass().getSimpleName() + " [" + V1MazeEvent.toString(event) + "]";
			if (s.length() > 75)
			{
				s = s.substring(0, 73)+"...";
			}
			return s;
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(MazeEvent me)
		{
			data.add(me);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(MazeEvent me, int index)
		{
			data.set(index, me);
			fireContentsChanged(this, index, index);
		}
		
		public void moveUp(int index)
		{
			if (index > 0)
			{
				MazeEvent event = data.remove(index);
				data.add(index-1, event);
				fireContentsChanged(this, index-1, index);
			}
		}
		
		public void moveDown(int index)
		{
			if (index < data.size()-1)
			{
				MazeEvent event = data.remove(index);
				data.add(index+1, event);
				fireContentsChanged(this, index, index+1);
			}
		}

		public void clear()
		{
			int size = data.size();
			data.clear();
			fireContentsChanged(this, 0, size-1);
		}
	}
}

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
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.StartingKit;

/**
 *
 */
public class StartingItemsPanel extends JPanel
	implements ActionListener, MouseListener
{
	private JList list;
	private StartingItemsListModel dataModel;
	private JButton add, remove;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public StartingItemsPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new StartingItemsListModel(new ArrayList<StartingKit>());
		list = new JList(dataModel);
		list.addMouseListener(this);
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
		this.setBorder(BorderFactory.createTitledBorder("Starting Items"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<StartingKit> startingItems)
	{
		this.dataModel.clear();
		if (startingItems == null)
		{
			return;
		}
		for (StartingKit si : startingItems)
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
			List<String> startingKitsOptions = new ArrayList<String>(
				Database.getInstance().getStartingKits().keySet());
			Collections.sort(startingKitsOptions);
			String[] options = startingKitsOptions.toArray(new String[startingKitsOptions.size()]);

			String selected = (String)JOptionPane.showInputDialog(
				this,
				"Select a starting kit",
				"Add Starting Kit",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

			if (selected != null)
			{
				StartingKit si = Database.getInstance().getStartingKits().get(selected);
				dataModel.add(si);
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
	public List<StartingKit> getStartingItems()
	{
		return new ArrayList<StartingKit>(dataModel.data);
	}

	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		
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
	static class StartingItemsListModel extends AbstractListModel
	{
		List<StartingKit> data;

		public StartingItemsListModel(List<StartingKit> data)
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

		public void add(StartingKit si)
		{
			data.add(si);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(StartingKit si, int index)
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

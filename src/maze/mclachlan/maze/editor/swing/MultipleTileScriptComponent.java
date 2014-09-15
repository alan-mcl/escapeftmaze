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
import mclachlan.maze.data.v1.V1TileScript;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.FlavourText;

/**
 *
 */
public class MultipleTileScriptComponent extends JButton implements ActionListener
{
	private List<TileScript> scripts;
	private int dirtyFlag;
	private TileScriptComponentCallback callback;
	private Zone zone;
	private TileScriptsListModel dataModel;
	private JDialog dialog;

	/*-------------------------------------------------------------------------*/
	public MultipleTileScriptComponent(int dirtyFlag, Zone zone)
	{
		this(null, dirtyFlag, null, zone);
	}

	/*-------------------------------------------------------------------------*/
	public MultipleTileScriptComponent(List<TileScript> scripts, int dirtyFlag, Zone zone)
	{
		this(scripts, dirtyFlag, null, zone);
	}

	/*-------------------------------------------------------------------------*/
	public MultipleTileScriptComponent(
		List<TileScript> scripts,
		int dirtyFlag,
		TileScriptComponentCallback callback,
		Zone zone)
	{
		this.dirtyFlag = dirtyFlag;
		this.callback = callback;
		refresh(scripts, zone);
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<TileScript> scripts, Zone zone)
	{
		this.scripts = scripts;
		this.zone = zone;

		if (scripts != null)
		{
			this.setText(scripts.size()+" scripts");
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<TileScript> getScripts()
	{
		return scripts;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			dialog = new JDialog(SwingEditor.instance, "Tile Scripts", true);
			dialog.setLayout(new BorderLayout());
			TileScriptListPanel listPanel = new TileScriptListPanel(scripts);
			dialog.add(listPanel, BorderLayout.CENTER);
			dialog.setLocationRelativeTo(SwingEditor.instance);
			dialog.pack();
			dialog.setVisible(true);

			refresh(dataModel.data, zone);
			SwingEditor.instance.setDirty(dirtyFlag);
			if (callback != null)
			{
				callback.tileScriptChanged(MultipleTileScriptComponent.this);
			}
		}
		else if (dialog != null && dialog.isVisible())
		{
			dialog.setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	class TileScriptListPanel extends JPanel implements ActionListener, MouseListener
	{
		JList list;
		JButton add, delete, edit, moveUp, moveDown, ok;

		TileScriptListPanel(List<TileScript> scripts)
		{
			setLayout(new BorderLayout());

			Vector vec = new Vector(scripts);
			list = new JList(vec);
			list.addMouseListener(this);
			dataModel = new TileScriptsListModel(scripts);
			list.setModel(dataModel);

			add = new JButton("Add");
			add.addActionListener(this);

			delete = new JButton("Delete");
			delete.addActionListener(this);

			edit = new JButton("Edit");
			edit.addActionListener(this);

			moveUp = new JButton("Move Up");
			moveUp.addActionListener(this);

			moveDown = new JButton("Move Down");
			moveDown.addActionListener(this);

			ok = new JButton("OK");
			ok.addActionListener(MultipleTileScriptComponent.this);

			JPanel buttons = new JPanel();
			buttons.add(add);
			buttons.add(delete);
			buttons.add(edit);
			buttons.add(moveUp);
			buttons.add(moveDown);
			buttons.add(ok);

			add(new JScrollPane(list), BorderLayout.CENTER);
			add(buttons, BorderLayout.SOUTH);
		}

		/*----------------------------------------------------------------------*/
		public void actionPerformed(ActionEvent e)
		{
			SwingEditor.instance.setDirty(dirtyFlag);

			if (e.getSource() == add)
			{
				TileScript script = new FlavourText("");
				TileScriptEditor dialog = new TileScriptEditor(SwingEditor.instance, script, dirtyFlag, zone);
				if (dialog.getResult() != null)
				{
					SwingEditor.instance.setDirty(dirtyFlag);
					dataModel.add(dialog.getResult());
				}
			}
			else if (e.getSource() == delete)
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

		/*----------------------------------------------------------------------*/
		private void editListItem()
		{
			int index = list.getSelectedIndex();
			if (index > -1)
			{
				TileScript script = dataModel.data.get(index);

				TileScriptEditor dialog = new TileScriptEditor(
					SwingEditor.instance,
					script,
					dirtyFlag,
					zone);

				if (dialog.getResult() != null)
				{
					dataModel.data.set(index, dialog.getResult());
				}
			}
		}

		/*----------------------------------------------------------------------*/
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
	}

	/*-------------------------------------------------------------------------*/
	static class TileScriptsListModel extends AbstractListModel
	{
		List<TileScript> data;

		public TileScriptsListModel(List<TileScript> data)
		{
			this.data = data;
		}

		public Object getElementAt(int index)
		{
			TileScript event = data.get(index);
			String s = event.getClass().getSimpleName() + " [" + V1TileScript.toString(event) + "]";
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

		public void add(TileScript me)
		{
			data.add(me);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(TileScript me, int index)
		{
			data.set(index, me);
			fireContentsChanged(this, index, index);
		}

		public void moveUp(int index)
		{
			if (index > 0)
			{
				TileScript event = data.remove(index);
				data.add(index-1, event);
				fireContentsChanged(this, index-1, index);
			}
		}

		public void moveDown(int index)
		{
			if (index < data.size()-1)
			{
				TileScript event = data.remove(index);
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
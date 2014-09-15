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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.data.Database;

/**
 *
 */
public class SpellListPanel extends JPanel
	implements ActionListener, MouseListener
{
	JList list;
	SpellListModel dataModel;
	JButton add, remove, edit;
	JComboBox spellCombo;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public SpellListPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		dataModel = new SpellListPanel.SpellListModel(new ArrayList<Spell>());
		list = new JList(dataModel);
		list.addMouseListener(this);
		list.setVisibleRowCount(20);
		add = new JButton("Add");
		add.addActionListener(this);
		edit = new JButton("Edit");
		edit.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		
		spellCombo = new JComboBox();

		JPanel panel = new JPanel(new BorderLayout(3,3));
		JScrollPane scroller = new JScrollPane(list);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(edit);
		buttons.add(remove);
		panel.add(scroller, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		this.add(panel);
		this.setBorder(BorderFactory.createTitledBorder("Spells"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<Spell> spell)
	{
		this.dataModel.clear();
		if (spell == null)
		{
			return;
		}
		for (Spell si : spell)
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
			String[] spells = Database.getInstance().getSpellList().toArray(new String[]{});
			Arrays.sort(spells);
			Object spell = JOptionPane.showInputDialog(
				this,
				"Select Spell",
				"Spell",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				spells,
				spells[0]);
			
			if (spell != null)
			{
				dataModel.add(Database.getInstance().getSpell((String)spell));
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
			String[] spells = Database.getInstance().getSpellList().toArray(new String[]{});
			Arrays.sort(spells);
			Object spell = JOptionPane.showInputDialog(
				this,
				"Select Spell",
				"Spell",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				spells,
				spells[index]);

			if (spell != null)
			{
				dataModel.update(Database.getInstance().getSpell((String)spell), index);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<Spell> getSpells()
	{
		return new ArrayList<Spell>(dataModel.data);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpellList());
		Collections.sort(vec);
		spellCombo.setModel(new DefaultComboBoxModel(vec));
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
	static class SpellListModel extends AbstractListModel
	{
		List<Spell> data;

		public SpellListModel(List<Spell> data)
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

		public void add(Spell si)
		{
			data.add(si);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(Spell si, int index)
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

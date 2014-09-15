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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class SpellEffectListPanel extends JPanel implements ActionListener, MouseListener
{
	JList list;
	JButton add, remove, edit;
	int dirtyFlag;
	DefaultListModel dataModel = new DefaultListModel();

	/*-------------------------------------------------------------------------*/
	public SpellEffectListPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
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
		this.setBorder(BorderFactory.createTitledBorder("Spell Effects"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<SpellEffect> spellEffects)
	{
		this.dataModel.clear();
		if (spellEffects == null)
		{
			return;
		}
		for (SpellEffect se : spellEffects)
		{
			this.dataModel.addElement(se.getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<SpellEffect> getSpellEffects()
	{
		List<SpellEffect> result = new ArrayList<SpellEffect>();

		for (Object obj : dataModel.toArray())
		{
			result.add(Database.getInstance().getSpellEffect((String)obj));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (e.getSource() == add)
		{
			ArrayList list = new ArrayList(Database.getInstance().getSpellEffects().keySet());
			Object[] selections = list.toArray();
			Arrays.sort(selections);

			String option = (String)JOptionPane.showInputDialog(
				SwingEditor.instance,
				"Add Spell Effect",
				"Add Spell Effect",
				JOptionPane.PLAIN_MESSAGE,
				null,
				selections,
				selections[0]);

			if (option != null)
			{
				dataModel.addElement(option);
			}
		}
		else if (e.getSource() == remove)
		{
			int index = list.getSelectedIndex();
			if (index > -1)
			{
				dataModel.remove(index);
			}
		}
		else if (e.getSource() == edit)
		{
			editSpellEffect();

		}
	}

	private void editSpellEffect()
	{
		int index = list.getSelectedIndex();

		if (index==-1)
		{
			return;
		}

		ArrayList list = new ArrayList(Database.getInstance().getSpellEffects().keySet());
		Object[] selections = list.toArray();
		Arrays.sort(selections);

		String option = (String)JOptionPane.showInputDialog(
			SwingEditor.instance,
			"Edit Spell Effect",
			"Edit Spell Effect",
			JOptionPane.PLAIN_MESSAGE,
			null,
			selections,
			this.list.getSelectedValue());

		if (option != null)
		{
			dataModel.set(index, option);
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == list)
		{
			if (e.getClickCount() == 2)
			{
				// a double click on a list item, treat as an edit
				SwingEditor.instance.setDirty(dirtyFlag);
				editSpellEffect();
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

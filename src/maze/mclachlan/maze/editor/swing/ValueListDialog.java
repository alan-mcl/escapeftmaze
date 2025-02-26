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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.stat.magic.*;

/**
 *
 */
public class ValueListDialog extends JDialog
	implements ActionListener, MouseListener
{
	static Value NULL_VALUE = new Value();

	private JList values;
	private JButton add, edit, remove, ok, cancel;

	private ValueList value;
	private ValueListModel dataModel;

	/*-------------------------------------------------------------------------*/
	public ValueListDialog(Frame owner, ValueList value) throws HeadlessException
	{
		super(owner, "Value List", true);

		dataModel = new ValueListModel();
		values = new JList(dataModel);
		if (value != null)
		{
			dataModel.add(value);
		}
		values.addMouseListener(this);

		add = new JButton("Add");
		add.addActionListener(this);
		edit = new JButton("Edit");
		edit.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel grid = new JPanel(new GridLayout(2, 1, 3, 3));

		JPanel buttons1 = new JPanel();
		buttons1.add(add);
		buttons1.add(edit);
		buttons1.add(remove);

		JPanel buttons2 = new JPanel();
		buttons2.add(ok);
		buttons2.add(cancel);

		grid.add(buttons1);
		grid.add(buttons2);

		setLayout(new BorderLayout(3,3));

		add(new JLabel("Values:"), BorderLayout.NORTH);
		add(new JScrollPane(values), BorderLayout.CENTER);
		add(grid, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			// set up the value
			List<Value> list = this.dataModel.data;
			if (list.size() == 0)
			{
				this.value = new ValueList(NULL_VALUE);
			}
			else
			{
				this.value = new ValueList(list);
			}

			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			setVisible(false);
		}
		else if (e.getSource() == add)
		{
			ValueEditor dialog = new ValueEditor(SwingEditor.instance,
				new Value(0, Value.SCALE.SCALE_WITH_CASTING_LEVEL));

			Value v = dialog.getValue();
			if (v != null)
			{
				this.dataModel.add(v);
			}
		}
		else if (e.getSource() == edit)
		{
			editListItem();
		}
		else if (e.getSource() == remove)
		{
			this.dataModel.remove(values.getSelectedIndex());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = values.getSelectedIndex();
		Value input = dataModel.data.get(index);
		ValueEditor dialog = new ValueEditor(SwingEditor.instance, input);
		Value v = dialog.getValue();
		if (v != null)
		{
			this.dataModel.update(v, index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == values)
		{
			if (e.getClickCount() == 2)
			{
				// a double click on a list item, treat as an edit
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
	static class ValueListModel extends AbstractListModel
	{
		List<Value> data = new ArrayList<Value>();

		public Object getElementAt(int index)
		{
			return prettyPrint(data.get(index));
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(ValueList v)
		{
			for (Value value : v.getValues())
			{
				data.add(value);
			}
			fireContentsChanged(this, data.size(), data.size());
		}

		public void add(Value v)
		{
			data.add(v);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(Value v, int index)
		{
			data.set(index, v);
			fireContentsChanged(this, index, index);
		}

		public void clear()
		{
			int size = data.size();
			data.clear();
			fireContentsChanged(this, 0, size-1);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String prettyPrint(Value v)
	{
		String result;
		if (v.getClass() == Value.class)
		{
			result = ""+v.getValue();
		}
		else if (v instanceof DiceValue)
		{
			result = V1Dice.toString(((DiceValue)v).getDice());
		}
		else if (v instanceof ModifierValue)
		{
			ModifierValue mv = (ModifierValue)v;
			result = mv.getModifier()+" modifier";
		}
		else if (v instanceof MagicPresentValue)
		{
			result = MagicSys.MagicColour.describe(((MagicPresentValue)v).getColour());
		}
		else
		{
			return "Custom: "+v.getClass().getName();
		}

		if (v.getScaling() == Value.SCALE.SCALE_WITH_CASTING_LEVEL)
		{
			result += " per casting level";
		}
		else if (v.getScaling() == Value.SCALE.SCALE_WITH_CHARACTER_LEVEL)
		{
			result += " per character level";
		}
		else if (v.getScaling() == Value.SCALE.SCALE_WITH_CLASS_LEVEL)
		{
			result += " per class level ("+v.getReference()+")";
		}
		else if (v.getScaling() == Value.SCALE.SCALE_WITH_MODIFIER)
		{
			result += " per modifier ("+v.getReference()+")";
		}
		else if (v.getScaling() == Value.SCALE.SCALE_WITH_PARTY_SIZE)
		{
			result += " per player character";
		}
		
		if (v.isShouldNegate())
		{
			result = " - "+result;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getValue()
	{
		return this.value;
	}
}

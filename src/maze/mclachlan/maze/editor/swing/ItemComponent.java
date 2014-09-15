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

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Vector;
import java.util.Collections;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.CurMax;

/**
 *
 */
public class ItemComponent extends JPanel implements ActionListener, ChangeListener
{
	JComboBox itemTemplate, cursedState;
	JCheckBox identified;
	JSpinner stack, charges, chargesMax;
	
	static Vector<String> cursedOptions;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	static
	{
		cursedOptions = new Vector<String>();
		cursedOptions.add("unknown");
		cursedOptions.add("known");
		cursedOptions.add("removed");
	}

	/*-------------------------------------------------------------------------*/
	public ItemComponent(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		setLayout(new FlowLayout());
		
		itemTemplate = new JComboBox();
		cursedState = new JComboBox(cursedOptions);
		identified = new JCheckBox("Identified?");
		stack = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		charges = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		chargesMax = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		
		itemTemplate.addActionListener(this);
		cursedState.addActionListener(this);
		identified.addActionListener(this);
		stack.addChangeListener(this);
		charges.addChangeListener(this);
		chargesMax.addChangeListener(this);
		
		add(itemTemplate);
		add(cursedState);
		add(identified);
		add(stack);
		add(charges);
		add(chargesMax);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(vec);
		vec.add(0, EditorPanel.NONE);
		itemTemplate.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
	
	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Item i)
	{
		itemTemplate.removeActionListener(this);
		cursedState.removeActionListener(this);
		identified.removeActionListener(this);
		stack.removeChangeListener(this);
		charges.removeChangeListener(this);
		chargesMax.removeChangeListener(this);
		
		if (i==null)
		{
			itemTemplate.setSelectedItem(EditorPanel.NONE);
			cursedState.setSelectedIndex(0);
			identified.setSelected(false);
			stack.setValue(0);
			charges.setValue(0);
			chargesMax.setValue(0);
		}
		else
		{
			itemTemplate.setSelectedItem(i.getTemplate().getName());
			cursedState.setSelectedIndex(i.getCursedState());
			identified.setSelected(i.getIdentificationState() == Item.IdentificationState.IDENTIFIED);
			stack.setValue(i.getStack().getCurrent());
			CurMax chargesCM = i.getCharges();
			charges.setValue(chargesCM==null?0:chargesCM.getCurrent());
			chargesMax.setValue(chargesCM==null?0:chargesCM.getMaximum());
		}
		
		itemTemplate.addActionListener(this);
		cursedState.addActionListener(this);
		identified.addActionListener(this);
		stack.addChangeListener(this);
		charges.addChangeListener(this);
		chargesMax.addChangeListener(this);
	}
	
	/*-------------------------------------------------------------------------*/
	public Item getItem()
	{
		if (itemTemplate.getSelectedItem().equals(EditorPanel.NONE))
		{
			return null;
		}
		else
		{
			ItemTemplate template = Database.getInstance().getItemTemplate((String)itemTemplate.getSelectedItem());
			int cursed = cursedState.getSelectedIndex();
			int id = identified.isSelected()?Item.IdentificationState.IDENTIFIED:Item.IdentificationState.UNIDENTIFIED;
			int stackCur = (Integer)stack.getValue();
			int chargesCur = (Integer)charges.getValue();
			int chargesMaxInt = (Integer)chargesMax.getValue();
			CurMax stackCM = new CurMax(stackCur, template.getMaxItemsPerStack());
			CurMax chargesCM = new CurMax(chargesCur, chargesMaxInt);
			return new Item(template, cursed, id, stackCM, chargesCM, null); // todo
		}
	}
}

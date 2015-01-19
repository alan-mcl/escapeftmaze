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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.BitSet;
import javax.swing.*;
import static mclachlan.maze.stat.ItemTemplate.Type.MAX_ITEM_TYPES;

import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class ItemTypeComponent extends JPanel implements ActionListener
{
	JCheckBox[] boxes;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public ItemTypeComponent(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		this.setLayout(new GridLayout(MAX_ITEM_TYPES/2, 2, 2, 2));

		boxes = new JCheckBox[MAX_ITEM_TYPES];

		for (int i=0; i<MAX_ITEM_TYPES; i++)
		{
			boxes[i] = new JCheckBox(ItemTemplate.Type.describe(i));
			boxes[i].addActionListener(this);
			this.add(boxes[i]);
		}

		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(BitSet itemTypes)
	{
		if (itemTypes != null)
		{
			for (int i=0; i<MAX_ITEM_TYPES; i++)
			{
				boxes[i].setSelected(itemTypes.get(i));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public BitSet getItemTypes()
	{
		BitSet result = new BitSet(MAX_ITEM_TYPES);
		for (int i=0; i<MAX_ITEM_TYPES; i++)
		{
			result.set(i, boxes[i].isSelected());
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}

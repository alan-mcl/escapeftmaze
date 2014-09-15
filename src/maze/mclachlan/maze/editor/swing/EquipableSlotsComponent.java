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
import java.util.BitSet;
import javax.swing.*;
import mclachlan.maze.stat.PlayerCharacter;
import static mclachlan.maze.stat.PlayerCharacter.EquipableSlots.NUMBER_OF_SLOTS;

/**
 *
 */
public class EquipableSlotsComponent extends JPanel implements ActionListener
{
	JCheckBox[] boxes;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public EquipableSlotsComponent(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		this.setLayout(new GridLayout(NUMBER_OF_SLOTS-1, 1, 2, 2));

		boxes = new JCheckBox[NUMBER_OF_SLOTS-1];

		for (int i=0; i<NUMBER_OF_SLOTS; i++)
		{
			if (i == PlayerCharacter.EquipableSlots.MISC_ITEM_2)
			{
				// skip this. the user is presented by 1 "Misc Item" checkbox
				continue;
			}
			boxes[i] = new JCheckBox(PlayerCharacter.EquipableSlots.describe(i));
			boxes[i].addActionListener(this);
			this.add(boxes[i]);
		}

		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(BitSet equipableSlots)
	{
		for (int i=0; i<NUMBER_OF_SLOTS; i++)
		{
			if (i == PlayerCharacter.EquipableSlots.MISC_ITEM_2)
			{
				// skip this. the user is presented by 1 "Misc Item" checkbox
				continue;
			}
			
			if (equipableSlots == null)
			{
				boxes[i].setSelected(false);
			}
			else
			{
				boxes[i].setSelected(equipableSlots.get(i));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public BitSet getEquipableSlots()
	{
		BitSet result = new BitSet(NUMBER_OF_SLOTS);
		for (int i=0; i<NUMBER_OF_SLOTS; i++)
		{
			if (i == PlayerCharacter.EquipableSlots.MISC_ITEM_2)
			{
				// skip this. the user is presented by 1 "Misc Item" checkbox
				continue;
			}
			
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

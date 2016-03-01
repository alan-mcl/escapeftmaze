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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.BodyPart;

/**
 *
 */
public class PlayerBodyPartTablePanel extends JPanel implements ActionListener
{
	private int dirtyFlag;
	JComboBox head, torso, leg, hand, foot;	

	/*-------------------------------------------------------------------------*/
	protected PlayerBodyPartTablePanel(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		this.setLayout(new GridLayout(5,2));
		
		head = new JComboBox();
		head.addActionListener(this);
		torso = new JComboBox();
		torso.addActionListener(this);
		leg = new JComboBox();
		leg.addActionListener(this);
		hand = new JComboBox();
		hand.addActionListener(this);
		foot = new JComboBox();
		foot.addActionListener(this);
		
		this.add(new JLabel("Head:"));
		this.add(head);
		this.add(new JLabel("Torso:"));
		this.add(torso);
		this.add(new JLabel("Leg:"));
		this.add(leg);
		this.add(new JLabel("Hand:"));
		this.add(hand);
		this.add(new JLabel("Foot:"));
		this.add(foot);
		
		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getBodyParts().keySet());
		Collections.sort(vec);
		head.setModel(new DefaultComboBoxModel(vec));
		torso.setModel(new DefaultComboBoxModel(vec));
		leg.setModel(new DefaultComboBoxModel(vec));
		hand.setModel(new DefaultComboBoxModel(vec));
		foot.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public BodyPart getHead()
	{
		return Database.getInstance().getBodyPart((String)head.getSelectedItem());
	}
	
	/*-------------------------------------------------------------------------*/
	public BodyPart getTorso()
	{
		return Database.getInstance().getBodyPart((String)torso.getSelectedItem());
	}
	
	/*-------------------------------------------------------------------------*/
	public BodyPart getLeg()
	{
		return Database.getInstance().getBodyPart((String)leg.getSelectedItem());
	}
	
	/*-------------------------------------------------------------------------*/
	public BodyPart getHand()
	{
		return Database.getInstance().getBodyPart((String)hand.getSelectedItem());
	}
	
	/*-------------------------------------------------------------------------*/
	public BodyPart getFoot()
	{
		return Database.getInstance().getBodyPart((String)foot.getSelectedItem());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(
		BodyPart head,
		BodyPart torso,
		BodyPart leg,
		BodyPart hand,
		BodyPart foot)
	{
		this.head.removeActionListener(this);
		this.torso.removeActionListener(this);
		this.leg.removeActionListener(this);
		this.hand.removeActionListener(this);
		this.foot.removeActionListener(this);

		if (head != null)
			this.head.setSelectedItem(head.getName());
		if (torso != null)
			this.torso.setSelectedItem(torso.getName());
		if (leg != null)
			this.leg.setSelectedItem(leg.getName());
		if (hand != null)
			this.hand.setSelectedItem(hand.getName());
		if (foot != null)
			this.foot.setSelectedItem(foot.getName());
		
		this.head.addActionListener(this);
		this.torso.addActionListener(this);
		this.leg.addActionListener(this);
		this.hand.addActionListener(this);
		this.foot.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}

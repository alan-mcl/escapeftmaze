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
import java.awt.event.ActionEvent;
import java.awt.*;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class PlayerBodyPartAttackedPanel extends JPanel 
	implements ChangeListener
{
	private int dirtyFlag;
	private JSpinner head, torso, leg, hand, foot;

	/*-------------------------------------------------------------------------*/
	protected PlayerBodyPartAttackedPanel(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		this.setLayout(new GridLayout(5,2));
		this.setPreferredSize(new Dimension(300, 100));
		
		head = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		head.addChangeListener(this);
		torso = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		torso.addChangeListener(this);
		leg = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		leg.addChangeListener(this);
		hand = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		hand.addChangeListener(this);
		foot = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		foot.addChangeListener(this);
		
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
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<String> getPlayerBodyParts()
	{
		PercentageTable<String> result = new PercentageTable<String>(true);
		
		result.add(PlayerCharacter.BodyParts.HEAD, (Integer)head.getValue());
		result.add(PlayerCharacter.BodyParts.TORSO, (Integer)torso.getValue());
		result.add(PlayerCharacter.BodyParts.LEG, (Integer)leg.getValue());
		result.add(PlayerCharacter.BodyParts.HAND, (Integer)hand.getValue());
		result.add(PlayerCharacter.BodyParts.FOOT, (Integer)foot.getValue());
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(PercentageTable<String> bodyParts)
	{
		this.head.removeChangeListener(this);
		this.torso.removeChangeListener(this);
		this.leg.removeChangeListener(this);
		this.hand.removeChangeListener(this);
		this.foot.removeChangeListener(this);
		
		this.head.setValue(bodyParts.getPercentage(PlayerCharacter.BodyParts.HEAD));
		this.torso.setValue(bodyParts.getPercentage(PlayerCharacter.BodyParts.TORSO));
		this.leg.setValue(bodyParts.getPercentage(PlayerCharacter.BodyParts.LEG));
		this.hand.setValue(bodyParts.getPercentage(PlayerCharacter.BodyParts.HAND));
		this.foot.setValue(bodyParts.getPercentage(PlayerCharacter.BodyParts.FOOT));
		
		this.head.addChangeListener(this);
		this.torso.addChangeListener(this);
		this.leg.addChangeListener(this);
		this.hand.addChangeListener(this);
		this.foot.addChangeListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}

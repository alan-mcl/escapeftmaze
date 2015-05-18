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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.SpellLikeAbility;


/**
 *
 */
public class SpellLikeAbilityEditor extends JDialog implements ActionListener
{
	public static final int MAX = 5;

	private SpellLikeAbility result;


	private int dirtyFlag;
	private JButton ok, cancel;

	// Special Ability ability
	private JComboBox spell;
	private ValueComponent castingLevelValue;

	/*-------------------------------------------------------------------------*/
	public SpellLikeAbilityEditor(Frame owner, SpellLikeAbility sla,
		int dirtyFlag) throws HeadlessException
	{
		super(owner, "Spell Like Ability", true);
		this.dirtyFlag = dirtyFlag;

		Vector<String> spells = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(spells);
		spell = new JComboBox(spells);

		castingLevelValue = new ValueComponent(this.dirtyFlag);

		JPanel panel = dirtyGridBagCrap(
			new JLabel("Spell:"), spell,
			new JLabel("Casting Level:"), castingLevelValue);

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3, 3));
		this.add(panel, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (sla != null)
		{
			setState(sla);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void setState(SpellLikeAbility sla)
	{
		spell.setSelectedItem(sla.getSpell().getName());
		castingLevelValue.setValue(sla.getCastingLevel());
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		this.result = new SpellLikeAbility(
			Database.getInstance().getSpell((String)spell.getSelectedItem()),
			castingLevelValue.getValue());
	}

	/*-------------------------------------------------------------------------*/
	public SpellLikeAbility getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridBagCrap(Component... comps)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i<comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx=0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			// save changes
			saveResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver);
		loader.init(Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1==1)
		{
			SpellLikeAbilityEditor test = new SpellLikeAbilityEditor(owner, null, -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}
}

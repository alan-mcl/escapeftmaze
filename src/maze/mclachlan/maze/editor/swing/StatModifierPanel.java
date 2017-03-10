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
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class StatModifierPanel extends JDialog implements ActionListener, ChangeListener
{
	private StatModifier modifier;

	private JButton ok, cancel, clear;
	private Map<JSpinner, JLabel> labelMap = new HashMap<JSpinner, JLabel>();
	private Map<Stats.Modifier, JSpinner> fieldMap = new HashMap<Stats.Modifier, JSpinner>();

	private JTabbedPane tabs;
	private List<Stats.Modifier> regularPlusResource, other;

	/*-------------------------------------------------------------------------*/
	public StatModifierPanel(Frame owner, StatModifier modifier)
	{
		super(owner, "Edit Stat Modifier", true);

		if (modifier == null)
		{
			modifier = new StatModifier();
		}

		this.modifier = modifier;

		tabs = new JTabbedPane();

		regularPlusResource = new ArrayList<Stats.Modifier>();
		regularPlusResource.addAll(Stats.resourceModifiers);
		regularPlusResource.addAll(Stats.regularModifiers);

		other = new ArrayList<Stats.Modifier>();
		other.addAll(Stats.allModifiers);
		other.removeAll(regularPlusResource);
		other.removeAll(Stats.statistics);
		other.removeAll(Stats.resistancesAndImmunities);
		other.removeAll(Stats.touches);
		other.removeAll(Stats.weaponAbilities);
		other.removeAll(Stats.favouredEnemies);

		List<Stats.Modifier> statisticsMinusResistances =
			new ArrayList<Stats.Modifier>(Stats.statistics);
		statisticsMinusResistances.removeAll(Stats.resistancesAndImmunities);

		JPanel regular = getTab(regularPlusResource);
		JPanel stats = getTab(new ArrayList<Stats.Modifier>(statisticsMinusResistances));
		JPanel rAndI = getTab(Stats.resistancesAndImmunities);
		JPanel touch = getTab(Stats.touches);
		JPanel weap = getTab(Stats.weaponAbilities);
		JPanel favEn = getTab(Stats.favouredEnemies);
		JPanel misc = getTab(other);

		tabs.addTab(getTitle("Regular", regularPlusResource), regular);
		tabs.addTab(getTitle("Statistics", Stats.statistics), stats);
		tabs.addTab(getTitle("Resistances & Immunities", Stats.resistancesAndImmunities), rAndI);
		tabs.addTab(getTitle("Touches", Stats.touches), touch);
		tabs.addTab(getTitle("Weapon Abilities", Stats.weaponAbilities), weap);
		tabs.addTab(getTitle("Favoured Enemies", Stats.favouredEnemies), favEn);
		tabs.addTab(getTitle("Other", other), misc);

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		clear = new JButton("Clear");
		clear.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);
		buttons.add(clear);

		this.setLayout(new BorderLayout(3,3));

		this.add(tabs, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private String getTitle(String s, List<Stats.Modifier> set)
	{
		int i = countSetModifiers(set);

		if (i > 0)
		{
			s += " ("+i+")";
		}

		return s;
	}

	/*-------------------------------------------------------------------------*/
	private int countSetModifiers(List<Stats.Modifier> modifiers)
	{
		int result = 0;

		for (Stats.Modifier modifier : modifiers)
		{
			JSpinner field = fieldMap.get(modifier);
			int value = (Integer)field.getValue();

			if (value != 0)
			{
				result ++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTab(List<Stats.Modifier> modifiers)
	{
		int max = modifiers.size();
		int cols = 3;

		JPanel result = new JPanel(new GridLayout(1, cols, 3, 3));
		int count=0;
		int perCol = (max+cols)/cols;

		for (int i=0; i<cols; i++)
		{
			int maxForThisCol = perCol + perCol*i;
			JPanel col = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = createGridBagConstraints();

			for (; count<maxForThisCol; count++)
			{
				if (count < max)
				{
					Stats.Modifier modifier = modifiers.get(count);

					int value = this.modifier.getModifier(modifier);
					JLabel label = getModifierLabel(modifier, value);
					JSpinner field = getModifierWidget(value);
					dodgyGridBagShite(col, label, field, gbc);
					labelMap.put(field, label);
					fieldMap.put(modifier, field);
				}
			}

			result.add(col);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JSpinner getModifierWidget(int modValue)
	{
		JSpinner result = new JSpinner(new SpinnerNumberModel(0, -128, 128, 1));
		result.addChangeListener(this);
		result.setValue(modValue);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JLabel getModifierLabel(Stats.Modifier mod, int modValue)
	{
		JLabel result = new JLabel(StringUtil.getModifierName(mod), JLabel.RIGHT);
		
		if (modValue == 0)
		{
			result.setEnabled(false);
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getUpdatedStatModifier()
	{
		StatModifier result = new StatModifier();
		for (Stats.Modifier modifier : Stats.Modifier.values())
		{
			JSpinner field = fieldMap.get(modifier);

			if (field == null)
			{
				continue;
			}

			int value = (Integer)field.getValue();
			if (value != 0)
			{
				result.setModifier(modifier, value);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			// save changes
			this.modifier = getUpdatedStatModifier();
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
		else if (e.getSource() == clear)
		{
			clear();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void clear()
	{
		for (Stats.Modifier modifier : Stats.Modifier.values())
		{
			JSpinner field = fieldMap.get(modifier);
			if (field != null)
			{
				field.setValue(0);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
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
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		JSpinner source = (JSpinner)e.getSource();
		int value = (Integer)source.getValue();
		JLabel label = labelMap.get(source);
		if (label != null)
		{
			label.setEnabled(value != 0);
		}

		if (tabs.getTabCount() > 0)
		{
			tabs.setTitleAt(0, getTitle("Regular", regularPlusResource));
			tabs.setTitleAt(1, getTitle("Statistics", Stats.statistics));
			tabs.setTitleAt(2, getTitle("Resistances & Immunities", Stats.resistancesAndImmunities));
			tabs.setTitleAt(3, getTitle("Touches", Stats.touches));
			tabs.setTitleAt(4, getTitle("Weapon Abilities", Stats.weaponAbilities));
			tabs.setTitleAt(5, getTitle("Favoured Enemies", Stats.favouredEnemies));
			tabs.setTitleAt(6, getTitle("Other", other));
		}
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifier()
	{
		return modifier;
	}
}

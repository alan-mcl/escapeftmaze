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

import static mclachlan.maze.data.v1.V1StatModifier.INDEX;

/**
 *
 */
public class StatModifierPanel extends JDialog implements ActionListener, ChangeListener
{
	StatModifier modifier;

	JButton ok, cancel, clear;
	Map<JSpinner, JLabel> labelMap = new HashMap<JSpinner, JLabel>();
	Map<String, JSpinner> fieldMap = new HashMap<String, JSpinner>();

	JTabbedPane tabs;
	JPanel regularModifiers, statistics, properties;
	private List<String> regularPlusResource;

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

		regularPlusResource = new ArrayList<String>();
		regularPlusResource.addAll(Stats.resourceModifiers);
		regularPlusResource.addAll(Stats.regularModifiers);

		regularModifiers = getTab(regularPlusResource);
		statistics = getTab(new ArrayList<String>(Stats.statistics));
		properties = getTab(Stats.propertiesModifiers);

		tabs.addTab(getRegularModifiersTabTitle(), regularModifiers);
		tabs.addTab(getStatisticsModifiersTabTitle(), statistics);
		tabs.addTab(getPropertiesModifiersTabTitle(), properties);

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
	private String getPropertiesModifiersTabTitle()
	{
		String s = "Properties";

		int i = countSetModifiers(Stats.propertiesModifiers);

		if (i > 0)
		{
			s += " ("+i+")";
		}

		return s;
	}

	/*-------------------------------------------------------------------------*/
	private String getStatisticsModifiersTabTitle()
	{
		String s = "Statistics";

		int i = countSetModifiers(Stats.statistics);

		if (i > 0)
		{
			s += " ("+i+")";
		}

		return s;
	}

	/*-------------------------------------------------------------------------*/
	private String getRegularModifiersTabTitle()
	{
		String s = "Regular";

		int i = countSetModifiers(regularPlusResource);

		if (i > 0)
		{
			s += " ("+i+")";
		}

		return s;
	}

	/*-------------------------------------------------------------------------*/
	private int countSetModifiers(List<String> modifiers)
	{
		int result = 0;

		for (String modifier : modifiers)
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
	private JPanel getTab(List<String> modifiers)
	{
		int max = modifiers.size();
		int cols = 4;

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
					String modifier = modifiers.get(count);
					
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
	private JLabel getModifierLabel(String mod, int modValue)
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
		for (int i = 0; i < INDEX.length; i++)
		{
			JSpinner field = fieldMap.get(INDEX[i]);

			if (field == null)
			{
				continue;
			}

			int value = (Integer)field.getValue();
			if (value != 0)
			{
				result.setModifier(INDEX[i], value);
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

	private void clear()
	{
		for (String modifier : INDEX)
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
			tabs.setTitleAt(0, getRegularModifiersTabTitle());
			tabs.setTitleAt(1, getStatisticsModifiersTabTitle());
			tabs.setTitleAt(2, getPropertiesModifiersTabTitle());
		}
	}
}

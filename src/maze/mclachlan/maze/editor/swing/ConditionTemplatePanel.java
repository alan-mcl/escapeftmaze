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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ConditionTemplatePanel extends EditorPanel
{
	private JCheckBox isCustom;
	private JTextField impl;

	private JTextField icon, displayName;
	private JTextField adjective;
	private JComboBox conditionEffect;
	private ValueComponent duration;
	private ValueComponent strength;
	private ValueComponent hpDamage, stamDamage, apDamage, mpDamage;
	private StatModifierComponent statModifier, bannerModifier;
	private JCheckBox scaleModifierWithStrength;
	private JCheckBox strengthWanes;
	private JComboBox exitCondition;
	private JSpinner exitConditionChance;
	private JComboBox exitSpellEffect;
	private RepeatedSpellEffectListPanel repeatedEffects;

	/*-------------------------------------------------------------------------*/
	public ConditionTemplatePanel()
	{
		super(SwingEditor.Tab.CONDITION_TEMPLATES);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		isCustom = new JCheckBox("Custom?");
		isCustom.addActionListener(this);

		impl = new JTextField(30);
		impl.addKeyListener(this);

		icon = new JTextField(30);
		icon.addKeyListener(this);

		displayName = new JTextField(30);
		displayName.addActionListener(this);
		displayName.addKeyListener(this);

		adjective = new JTextField(30);
		adjective.addKeyListener(this);

		conditionEffect = new JComboBox();
		conditionEffect.addActionListener(this);

		duration = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		strength = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		hpDamage = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		stamDamage = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		apDamage = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		mpDamage = new ValueComponent(SwingEditor.Tab.CONDITION_TEMPLATES);

		statModifier = new StatModifierComponent(SwingEditor.Tab.CONDITION_TEMPLATES);
		bannerModifier = new StatModifierComponent(SwingEditor.Tab.CONDITION_TEMPLATES);

		scaleModifierWithStrength = new JCheckBox("Scale Modifiers With Strength?");
		scaleModifierWithStrength.addActionListener(this);

		strengthWanes = new JCheckBox("Strength Wanes?");
		strengthWanes.addActionListener(this);

		exitCondition = new JComboBox(ConditionTemplate.ExitCondition.values());
		exitCondition.addActionListener(this);

		exitConditionChance = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		exitConditionChance.addChangeListener(this);

		exitSpellEffect = new JComboBox();
		exitSpellEffect.addItemListener(this);

		repeatedEffects = new RepeatedSpellEffectListPanel(
			"Repeated Spell Effects",
			this.dirtyFlag,
			1.0,
			0.25);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		editControls.add(isCustom, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(impl, gbc);

		gbc.gridy++;

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Display Name:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(displayName, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Icon:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(icon, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Adjective:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(adjective, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Condition Effect:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(conditionEffect, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Duration:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(duration, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Strength:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(strength, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Hit Points Damage:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(hpDamage, gbc);
		
		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Stamina Damage:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(stamDamage, gbc);
		
		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Action Points Damage:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(apDamage, gbc);
		
		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Magic Points Damage:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(mpDamage, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Stat Modifier:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(statModifier, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Banner Modifier:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(bannerModifier, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 3;
		editControls.add(scaleModifierWithStrength, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 3;
		editControls.add(strengthWanes, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Exit Condition:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(exitCondition, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Exit Condition Chance:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(exitConditionChance, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Exit Spell Effect:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(exitSpellEffect, gbc);

		gbc.weighty = 1.0;

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 2.0;
		gbc.gridwidth = 3;
		editControls.add(repeatedEffects, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>((Database.getInstance().getConditionTemplates().values()));
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CONDITION_TEMPLATES);

		ConditionTemplate ct = new ConditionTemplate(
			name,
			name,
			new ValueList(),
			new ValueList(),
			ConditionEffect.NONE,
			new StatModifier(),
			new StatModifier(),
			null,
			null,
			null,
			null,
			"",
			"",
			false,
			true,
			ConditionTemplate.ExitCondition.DURATION_EXPIRES,
			0,
			null,
			null);

		Database.getInstance().getConditionTemplates().put(name, ct);

		return ct;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CONDITION_TEMPLATES);

		ConditionTemplate current = Database.getInstance().getConditionTemplate(currentName);

		Database.getInstance().getConditionTemplates().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getConditionTemplates().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CONDITION_TEMPLATES);

		ConditionTemplate current = Database.getInstance().getConditionTemplate(currentName);

		ConditionTemplate ct = new ConditionTemplate(
			newName,
			current.getDisplayName(),
			current.getDuration(),
			current.getStrength(),
			current.getConditionEffect(),
			new StatModifier(current.getStatModifier()),
			new StatModifier(current.getBannerModifier()),
			current.getHitPointDamage(),
			current.getStaminaDamage(),
			current.getActionPointDamage(),
			current.getMagicPointDamage(),
			current.getIcon(),
			current.getAdjective(),
			current.isScaleModifierWithStrength(),
			current.isStrengthWanes(),
			current.getExitCondition(),
			current.getExitConditionChance(),
			current.getExitSpellEffect(),
			current.getRepeatedSpellEffects());

		Database.getInstance().getConditionTemplates().put(newName, ct);

		return ct;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CONDITION_TEMPLATES);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getConditionTemplates().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		ConditionTemplate ct = Database.getInstance().getConditionTemplate(name);

		if (ct.getImpl() != null)
		{
			isCustom.setSelected(true);
			checkCustom();
			impl.setText(ct.getImpl().getName());

			displayName.setText("");
			icon.setText("");
			adjective.setText("");
			conditionEffect.removeActionListener(this);
			conditionEffect.setSelectedItem(NONE);
			conditionEffect.addActionListener(this);
			duration.setValue(null);
			hpDamage.setValue(null);
			strength.setValue(null);
			statModifier.setModifier(null);
			bannerModifier.setModifier(null);
			scaleModifierWithStrength.setSelected(false);
			strengthWanes.setSelected(false);
		}
		else
		{
			isCustom.setSelected(false);
			checkCustom();
			impl.setText("");

			conditionEffect.removeActionListener(this);
			exitCondition.removeActionListener(this);
			exitConditionChance.removeChangeListener(this);
			exitSpellEffect.removeActionListener(this);

			displayName.setText(ct.getDisplayName());
			icon.setText(ct.getIcon());
			adjective.setText(ct.getAdjective());
			if (ct.getConditionEffect().equals(ConditionEffect.NONE))
			{
				conditionEffect.setSelectedItem(NONE);
			}
			else
			{
				conditionEffect.setSelectedItem(ct.getConditionEffect().getName());
			}
			duration.setValue(ct.getDuration());
			hpDamage.setValue(ct.getHitPointDamage());
			stamDamage.setValue(ct.getStaminaDamage());
			apDamage.setValue(ct.getActionPointDamage());
			mpDamage.setValue(ct.getMagicPointDamage());
			strength.setValue(ct.getStrength());
			statModifier.setModifier(ct.getStatModifier());
			bannerModifier.setModifier(ct.getBannerModifier());
			scaleModifierWithStrength.setSelected(ct.isScaleModifierWithStrength());
			strengthWanes.setSelected(ct.isStrengthWanes());
			exitCondition.setSelectedItem(ct.getExitCondition());
			exitConditionChance.setValue(ct.getExitConditionChance());
			repeatedEffects.refresh(ct.getRepeatedSpellEffects());
			if (ct.getExitSpellEffect() == null)
			{
				exitSpellEffect.setSelectedItem(NONE);
			}
			else
			{
				exitSpellEffect.setSelectedItem(ct.getExitSpellEffect());
			}

			conditionEffect.addActionListener(this);
			exitCondition.addActionListener(this);
			exitConditionChance.addChangeListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		if (name == null)
		{
			return null;
		}

		ConditionTemplate ct = Database.getInstance().getConditionTemplate(name);

		if (isCustom.isSelected())
		{
			try
			{
				ct = new ConditionTemplate(name, Class.forName(impl.getText()));
				Database.getInstance().getConditionTemplates().put(name,ct);
			}
			catch (ClassNotFoundException e)
			{
				throw new MazeException(e);
			}
		}
		else
		{
			ct.setAdjective(adjective.getText());
			String ceName = (String)conditionEffect.getSelectedItem();
			if (NONE.equals(ceName))
			{
				ct.setConditionEffect(ConditionEffect.NONE);
			}
			else
			{
				ct.setConditionEffect(Database.getInstance().getConditionEffect(ceName));
			}
			ct.setHitPointDamage(hpDamage.getValue());
			ct.setStaminaDamage(stamDamage.getValue());
			ct.setActionPointDamage(apDamage.getValue());
			ct.setMagicPointDamage(mpDamage.getValue());
			ct.setDuration(duration.getValue());
			ct.setDisplayName(displayName.getText());
			ct.setIcon(icon.getText());
			ct.setScaleModifierWithStrength(scaleModifierWithStrength.isSelected());
			ct.setStatModifier(statModifier.getModifier());
			ct.setBannerModifier(bannerModifier.getModifier());
			ct.setStrength(strength.getValue());
			ct.setStrengthWanes(strengthWanes.isSelected());
			ct.setExitCondition((ConditionTemplate.ExitCondition)exitCondition.getSelectedItem());
			ct.setExitConditionChance((Integer)exitConditionChance.getValue());
			ct.setExitSpellEffect(exitSpellEffect.getSelectedItem()==NONE?null: (String)exitSpellEffect.getSelectedItem());
			ct.setRepeatedSpellEffects(repeatedEffects.getRepeatedSpellEffects());
		}

		return ct;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(
			Database.getInstance().getConditionEffects().keySet());
		vec.insertElementAt(NONE, 0);
		Collections.sort(vec);
		this.conditionEffect.setModel(new DefaultComboBoxModel(vec));

		repeatedEffects.initForeignKeys();

		vec = new Vector<String>(Database.getInstance().getSpellEffects().keySet());
		Collections.sort(vec);
		vec.add(0, NONE);
		this.exitSpellEffect.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == isCustom)
		{
			checkCustom();
		}

		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/
	private void checkCustom()
	{
		setEnabledAllEditControls(!isCustom.isSelected());
		isCustom.setEnabled(true);
		impl.setEnabled(isCustom.isSelected());
	}
}

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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.NullActor;

/**
 *
 */
public class ConditionWidget extends JPanel implements ActionListener, ChangeListener, KeyListener
{
	private JComboBox conditionTemplate;
	private JSpinner duration;
	private JSpinner strength;
	private JSpinner castingLevel;
	private ValueComponent hpDamage, stamDamage, apDamage, mpDamage;
	private JComboBox type, subtype;
	private JTextField source;

	private JCheckBox isIdentified;
	private JCheckBox isStrengthIdentified;
	private JSpinner createdTurn;
	private JCheckBox isHostile;

	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public ConditionWidget(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		this.add(getEditControls());
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		Vector<String> conditionTemplatesVec = new Vector<String>(
			Database.getInstance().getConditionTemplates().keySet());
		Collections.sort(conditionTemplatesVec);
		conditionTemplatesVec.add(0, EditorPanel.NONE);

		conditionTemplate = new JComboBox(conditionTemplatesVec);
		conditionTemplate.addActionListener(this);

		type = new JComboBox(MagicSys.SpellEffectType.values());
		type.addActionListener(this);

		subtype = new JComboBox(MagicSys.SpellEffectSubType.values());
		subtype.addActionListener(this);

		duration = new JSpinner(new SpinnerNumberModel(1,1,99999,1));
		duration.addChangeListener(this);

		strength = new JSpinner(new SpinnerNumberModel(1,1,99999,1));
		strength.addChangeListener(this);

		castingLevel = new JSpinner(new SpinnerNumberModel(1,1,99999,1));
		castingLevel.addChangeListener(this);

		hpDamage = new ValueComponent(dirtyFlag);
		stamDamage = new ValueComponent(dirtyFlag);
		apDamage = new ValueComponent(dirtyFlag);
		mpDamage = new ValueComponent(dirtyFlag);

		source = new JTextField(30);
		source.addKeyListener(this);

		isIdentified = new JCheckBox("Identified?");
		isIdentified.addActionListener(this);

		isStrengthIdentified = new JCheckBox("Strength Identified?");
		isStrengthIdentified.addActionListener(this);

		isHostile = new JCheckBox("Hostile?");
		isHostile.addActionListener(this);

		createdTurn = new JSpinner(new SpinnerNumberModel(1L, 0L, 999999999L, 1L));
		createdTurn.addChangeListener(this);

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

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Template:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(conditionTemplate, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Type:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(type, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Subtype:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(subtype, gbc);

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
		editControls.add(new JLabel("Casting level:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(castingLevel, gbc);

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
		editControls.add(new JLabel("Source:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(source, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		editControls.add(new JLabel("Created Turn:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 2;
		editControls.add(createdTurn, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 3;
		editControls.add(isHostile, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 3;
		editControls.add(isIdentified, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 1.0;
		gbc.gridx++;
		gbc.gridwidth = 3;
		gbc.weighty = 1.0;
		editControls.add(isStrengthIdentified, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Condition c)
	{
		if (c == null)
		{
			return;
		}

		if (c.getTemplate() != null)
		{
			conditionTemplate.setSelectedItem(c.getTemplate().getName());
		}
		else
		{
			conditionTemplate.setSelectedItem(EditorPanel.NONE);
		}
		type.setSelectedItem(c.getType());
		subtype.setSelectedItem(c.getSubtype());

		if (c.getSource() == null)
		{
			source.setText("");
		}
		else
		{
			source.setText(c.getSource().getName());
		}

		isIdentified.setSelected(c.isIdentified());
		isStrengthIdentified.setSelected(c.isStrengthIdentified());
		createdTurn.setValue(c.getCreatedTurn());
		isHostile.setSelected(c.isHostile());

		duration.setValue(c.getDuration());
		hpDamage.setValue(c.getHitPointDamage());
		stamDamage.setValue(c.getStaminaDamage());
		apDamage.setValue(c.getActionPointDamage());
		mpDamage.setValue(c.getMagicPointDamage());
		castingLevel.setValue(c.getCastingLevel());
		strength.setValue(c.getStrength());
	}

	/*-------------------------------------------------------------------------*/
	public Condition getCondition()
	{
		ConditionTemplate template;
		if (conditionTemplate.getSelectedItem() == EditorPanel.NONE)
		{
			template = null;
		}
		else
		{
			String templateName = (String)conditionTemplate.getSelectedItem();
			template = Database.getInstance().getConditionTemplate(templateName);
		}

		UnifiedActor condSource = new NullActor(){
			@Override
			public String getName()
			{
				return ConditionWidget.this.source.getText();
			}
		};

		return new Condition(
			template,
			(Integer)(duration.getValue()),
			(Integer)(strength.getValue()),
			(Integer)(castingLevel.getValue()),
			hpDamage.getValue(),
			apDamage.getValue(),
			mpDamage.getValue(),
			stamDamage.getValue(),
			(MagicSys.SpellEffectType)type.getSelectedItem(),
			(MagicSys.SpellEffectSubType)subtype.getSelectedItem(),
			condSource,
			isIdentified.isSelected(),
			isStrengthIdentified.isSelected(),
			Math.round((Double)createdTurn.getValue()),
			isHostile.isSelected());
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		// need this here?
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void stateChanged(ChangeEvent e)
	{

	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	@Override
	public void keyPressed(KeyEvent e)
	{

	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void keyReleased(KeyEvent e)
	{

	}
}
